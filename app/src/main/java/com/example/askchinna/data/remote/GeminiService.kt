package com.example.askchinna.data.remote
/**
 * GeminiService.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import com.example.askchinna.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.nio.charset.StandardCharsets
import kotlin.math.pow

/**
 * Service class for interacting with the Google Gemini AI API.
 * Handles text generation requests and responses with production-grade reliability.
 * This service is intended for use only within India.
 */
class GeminiService {
    private val TAG = "GeminiService"

    // Track API usage for rate limiting
    private val requestCounter = AtomicInteger(0)
    private val maxRequestsPerMinute = 10 // Adjust based on your API tier
    private var lastResetTime = System.currentTimeMillis()

    // Safety settings for content generation
    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM)
    )

    // Configuration for the generation model
    private val generationConfig = generationConfig {
        temperature = 0.7f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 2048
    }

    // API key security - use encryption for storing key in memory
    private val encryptionKey = "AskChinnaSecureKey".toByteArray().copyOf(16) // 128-bit key
    private val encryptedApiKey by lazy {
        encryptString(BuildConfig.GEMINI_API_KEY, encryptionKey)
    }

    // Initialize the generative model with decrypted API key
    private val generativeModel by lazy {
        val decryptedApiKey = decryptString(encryptedApiKey, encryptionKey)
        GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = decryptedApiKey,
            generationConfig = generationConfig,
            safetySettings = safetySettings
        )
    }

    /**
     * Generates a response for the given prompt with retry logic.
     *
     * @param prompt The user's prompt/question to the AI
     * @param maxRetries Number of times to retry in case of failure
     * @param userConsentProvided Whether the user has consented to data processing
     * @return The AI's response as a String, or error message if the request fails
     */
    suspend fun generateContent(
        prompt: String,
        maxRetries: Int = 3,
        userConsentProvided: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!userConsentProvided) {
            return@withContext Result.failure(IllegalStateException(
                "User consent for AI processing is required as per Indian IT rules"))
        }

        // Check and enforce rate limits
        if (!checkRateLimit()) {
            return@withContext Result.failure(IOException(
                "Rate limit exceeded. Please try again later."))
        }

        var lastException: Exception? = null

        for (attempt in 0..maxRetries) {
            if (attempt > 0) {
                // Exponential backoff
                val backoffTime = (2.0.pow(attempt.toDouble()) * 1000).toLong().coerceAtMost(10000)
                delay(backoffTime)
            }

            try {
                val response = generativeModel.generateContent(prompt)

                // Check for content filtering blocks
                val errorMessage = getErrorFromResponse(response)
                if (errorMessage != null) {
                    return@withContext Result.failure(IOException(errorMessage))
                }

                return@withContext if (response.text != null) {
                    Log.d(TAG, "Content generated successfully")
                    Result.success(response.text ?: "No response generated")
                } else {
                    Log.e(TAG, "Failed to generate content: Empty response")
                    Result.failure(IOException("No response text was generated"))
                }
            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "Network timeout on attempt ${attempt+1}/$maxRetries", e)
                lastException = e
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Network connectivity issue", e)
                return@withContext Result.failure(IOException("Please check your internet connection"))
            } catch (e: Exception) {
                Log.e(TAG, "Error generating content on attempt ${attempt+1}/$maxRetries", e)
                lastException = e

                // Don't retry on client errors
                if (e.message?.contains("400") == true) {
                    return@withContext Result.failure(e)
                }
            }
        }

        return@withContext Result.failure(lastException ?: IOException("Failed to generate content after $maxRetries retries"))
    }

    /**
     * Starts a chat session with the Gemini model.
     * Use this for multi-turn conversations.
     *
     * @param userConsentProvided Whether the user has consented to data processing
     * @return A chat session that can be used for continued conversation, or null if consent not provided
     */
    fun startChat(userConsentProvided: Boolean = false) =
        if (userConsentProvided) generativeModel.startChat() else null

    /**
     * Handles potential errors from the Gemini API response.
     *
     * @param response The response from the Gemini API
     * @return A human-readable error message if there's an error, null otherwise
     */
    private fun getErrorFromResponse(response: GenerateContentResponse): String? {
        val promptFeedback = response.promptFeedback
        return if (promptFeedback != null && !promptFeedback.blockReason.isNullOrEmpty()) {
            "Content was blocked due to: ${promptFeedback.blockReason}"
        } else {
            null
        }
    }

    /**
     * Checks and enforces rate limits for API calls.
     *
     * @return true if request can proceed, false if rate limited
     */
    private fun checkRateLimit(): Boolean {
        val currentTime = System.currentTimeMillis()

        // Reset counter if a minute has passed
        if (currentTime - lastResetTime > 60000) {
            requestCounter.set(0)
            lastResetTime = currentTime
        }

        val currentCount = requestCounter.incrementAndGet()
        return currentCount <= maxRequestsPerMinute
    }

    /**
     * Encrypts a string using AES encryption.
     *
     * @param input The string to encrypt
     * @param key The encryption key
     * @return The encrypted string in Base64 format
     */
    private fun encryptString(input: String, key: ByteArray): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(input.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    /**
     * Decrypts a string using AES decryption.
     *
     * @param encryptedInput The encrypted string in Base64 format
     * @param key The decryption key
     * @return The decrypted string
     */
    private fun decryptString(encryptedInput: String, key: ByteArray): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedBytes = Base64.decode(encryptedInput, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    companion object {
        const val SERVICE_VERSION = "1.0.0"
        const val INTENDED_REGION = "India"  // Explicitly mark intended region
    }
}