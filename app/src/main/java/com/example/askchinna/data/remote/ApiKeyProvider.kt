package com.example.askchinna.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * File: app/src/main/java/com/example/askchinna/data/remote/ApiKeyProvider.kt
 * Copyright © 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 6, 2025
 * Version: 1.3
 * Description: Provides API keys for Gemini and Firebase.
 *              - Gemini key is fetched from ENV or BuildConfig.
 *              - Firebase key is retrieved at runtime via FirebaseApp.
 */
@Singleton
class ApiKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "ApiKeyProvider"

    companion object {
        private const val ENV_GEMINI_API_KEY = "GEMINI_API_KEY"
    }

    /**
     * Returns the Gemini API key, preferring environment variables over BuildConfig.
     */
    suspend fun getGeminiApiKey(): String = withContext(Dispatchers.IO) {
        System.getenv(ENV_GEMINI_API_KEY)
            .takeIf { !it.isNullOrBlank() }
            ?.also { Log.d(tag, "Using Gemini API key from ENV") }
            ?: run {
                val key = com.example.askchinna.BuildConfig.GEMINI_API_KEY
                Log.d(tag, "Using Gemini API key from BuildConfig")
                key
            }
    }

    /**
     * Returns the Firebase API key from the initialized FirebaseApp settings.
     */
    fun getFirebaseApiKey(): String {
        val apiKey = FirebaseApp.getInstance().options.apiKey
        Log.d(tag, "Retrieved Firebase API key from FirebaseApp options")
        return apiKey
    }
}
