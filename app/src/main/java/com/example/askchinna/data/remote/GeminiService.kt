package com.example.askchinna.data.remote

import android.util.Base64
import android.util.Log
import com.example.askchinna.util.NetworkExceptionHandler
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * File: app/src/main/java/com/example/askchinna/data/remote/GeminiService.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.4
 * Description: Service class for interacting with the Google Gemini AI API.
 *              Handles text generation requests and responses with production‑grade reliability.
 *              Enforces rate‑limits, retries with exponential back‑off, safety filtering,
 *              in‑memory encryption of the API key, and full logging. Intended for India only.
 */
@Singleton
class GeminiService @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider,
    private val networkExceptionHandler: NetworkExceptionHandler
) {
    private val tag = "GeminiService"

    // Rate‑limit tracking
    private val requestCounter = AtomicInteger(0)
    private val maxRequestsPerMinute = 10
    private var lastResetTime = System.currentTimeMillis()

    // Safety filters: block above MEDIUM_AND_ABOVE for several harm categories
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
    )

    // Generation parameters using DSL builder
    private val generationConfig = generationConfig {
        temperature = 0.7f
        topK = 40
        topP = 0.95f
        candidateCount = 1
        maxOutputTokens = 2048
    }

    // In‑memory AES key & encrypted API key store
    private val encryptionKey = "AskChinnaSecureKey".toByteArray().copyOf(16)
    private var encryptedApiKey: String? = null

    /**
     * Lazily builds a GenerativeModel, decrypting the key if stored,
     * otherwise fetching & encrypting it on first use.
     */
    private suspend fun getGenerativeModel(): GenerativeModel = withContext(Dispatchers.IO) {
        val apiKey = encryptedApiKey?.let { decryptString(it, encryptionKey) } ?: run {
            val fresh = apiKeyProvider.getGeminiApiKey()
            encryptedApiKey = encryptString(fresh, encryptionKey)
            fresh
        }

        GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = apiKey,
            generationConfig = generationConfig,
            safetySettings = safetySettings
        )
    }

    /**
     * Generate text for [prompt], with up to [maxRetries] attempts,
     * exponential back‑off, rate‑limit, and mandatory [userConsentProvided].
     */
    suspend fun generateContent(
        prompt: String,
        maxRetries: Int = 3,
        userConsentProvided: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!userConsentProvided) {
            return@withContext Result.failure(
                IllegalStateException("User consent required for AI processing.")
            )
        }

        if (!checkRateLimit()) {
            return@withContext Result.failure(
                IOException("Rate limit exceeded; please try again later.")
            )
        }

        var lastException: Exception? = null
        for (attempt in 0..maxRetries) {
            if (attempt > 0) {
                val backoffMs = (2.0.pow(attempt.toDouble()) * 1000).toLong().coerceAtMost(10000)
                Log.w(tag, "Retry #$attempt in ${'$'}backoffMs ms")
                delay(backoffMs)
            }

            try {
                val model = getGenerativeModel()
                val response = model.generateContent(prompt)

                getErrorFromResponse(response)?.let { reason ->
                    return@withContext Result.failure(IOException(reason))
                }

                val text = response.text
                return@withContext if (!text.isNullOrBlank()) {
                    Log.d(tag, "Generated content: ${'$'}{text.take(50)}…")
                    Result.success(text)
                } else {
                    Log.e(tag, "Empty response received")
                    Result.failure(IOException("No content generated"))
                }
            } catch (e: SocketTimeoutException) {
                Log.w(tag, "Timeout on attempt ${'$'}{attempt + 1}/$maxRetries", e)
                lastException = e
            } catch (e: UnknownHostException) {
                Log.e(tag, "Network connectivity issue", e)
                return@withContext Result.failure(IOException("Check internet connection"))
            } catch (e: Exception) {
                val message = networkExceptionHandler.handle(e)
                Log.e(tag, "Error on attempt ${'$'}{attempt + 1}/$maxRetries: ${'$'}message", e)
                lastException = e
                if (e.message?.contains("400") == true) {
                    return@withContext Result.failure(e)
                }
            }
        }

        Result.failure(lastException ?: IOException("Failed after ${'$'}maxRetries retries"))
    }

    /**
     * Begin a multi‑turn chat session (or null if no consent).
     */
    suspend fun startChat(userConsentProvided: Boolean = false) =
        if (userConsentProvided) getGenerativeModel().startChat() else null

    /**
     * Inspect the safety feedback; returns a block reason or null.
     */
    private fun getErrorFromResponse(response: GenerateContentResponse): String? {
        val reason = response.promptFeedback?.blockReason
        return if (reason != null) {
            "Content blocked due to: ${'$'}reason"
        } else {
            null
        }
    }

    /**
     * Enforce ≤ maxRequestsPerMinute within any rolling 60s window.
     */
    private fun checkRateLimit(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastResetTime > 60000) {
            requestCounter.set(0)
            lastResetTime = now
        }
        return requestCounter.incrementAndGet() <= maxRequestsPerMinute
    }

    private fun encryptString(input: String, key: ByteArray): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
        return Base64.encodeToString(cipher.doFinal(input.toByteArray(StandardCharsets.UTF_8)), Base64.DEFAULT)
    }

    private fun decryptString(encrypted: String, key: ByteArray): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
        val decoded = Base64.decode(encrypted, Base64.DEFAULT)
        return String(cipher.doFinal(decoded), StandardCharsets.UTF_8)
    }

    companion object {
        const val SERVICE_VERSION = "1.4"
        const val INTENDED_REGION = "India"
    }
}
