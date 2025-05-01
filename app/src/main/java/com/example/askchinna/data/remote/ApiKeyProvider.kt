/*
 * File: com/example/askchinna/data/remote/ApiKeyProvider.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.data.remote

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.askchinna.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure provider for API keys used in the application.
 * This class handles the secure storage and retrieval of API keys
 * using Android's EncryptedSharedPreferences.
 *
 * It is designed specifically for low-end devices with proper fallback mechanisms
 * in case of encryption failures.
 */
@Singleton
class ApiKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "ApiKeyProvider"

    companion object {
        private const val PREF_FILE_NAME = "askchinna_secure_keys"
        private const val GEMINI_API_KEY = "gemini_api_key"
        private const val FIREBASE_API_KEY = "firebase_api_key"
        private const val API_KEY_VERSION = "api_key_version"
        private const val CURRENT_VERSION = "1.0.0"
    }

    /**
     * Gets the Gemini API key from secure storage or BuildConfig as fallback.
     *
     * @return The Gemini API key string
     * @throws IOException If there's an I/O error accessing SharedPreferences
     */
    suspend fun getGeminiApiKey(): String = withContext(Dispatchers.IO) {
        try {
            val encryptedPrefs = getEncryptedSharedPreferences()

            // Check if we need to initialize the key
            if (!encryptedPrefs.contains(GEMINI_API_KEY) ||
                encryptedPrefs.getString(API_KEY_VERSION, "") != CURRENT_VERSION) {
                // Initialize with the key from BuildConfig
                Log.d(TAG, "Initializing Gemini API key in secure storage")
                encryptedPrefs.edit()
                    .putString(GEMINI_API_KEY, BuildConfig.GEMINI_API_KEY)
                    .putString(API_KEY_VERSION, CURRENT_VERSION)
                    .apply()
            }

            return@withContext encryptedPrefs.getString(GEMINI_API_KEY, "") ?: ""
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Security exception when accessing Gemini API key", e)
            // Fallback to BuildConfig in case of encryption failure
            return@withContext BuildConfig.GEMINI_API_KEY
        } catch (e: IOException) {
            Log.e(TAG, "I/O exception when accessing Gemini API key", e)
            // Fallback to BuildConfig in case of I/O failure
            return@withContext BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error when accessing Gemini API key", e)
            // Fallback to BuildConfig for any other error
            return@withContext BuildConfig.GEMINI_API_KEY
        }
    }

    /**
     * Gets the Firebase API key from secure storage or BuildConfig as fallback.
     *
     * @return The Firebase API key string
     * @throws IOException If there's an I/O error accessing SharedPreferences
     */
    suspend fun getFirebaseApiKey(): String = withContext(Dispatchers.IO) {
        try {
            val encryptedPrefs = getEncryptedSharedPreferences()

            // Check if we need to initialize the key
            if (!encryptedPrefs.contains(FIREBASE_API_KEY) ||
                encryptedPrefs.getString(API_KEY_VERSION, "") != CURRENT_VERSION) {
                // Initialize with the key from BuildConfig
                Log.d(TAG, "Initializing Firebase API key in secure storage")
                encryptedPrefs.edit()
                    .putString(FIREBASE_API_KEY, BuildConfig.FIREBASE_API_KEY)
                    .putString(API_KEY_VERSION, CURRENT_VERSION)
                    .apply()
            }

            return@withContext encryptedPrefs.getString(FIREBASE_API_KEY, "") ?: ""
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Security exception when accessing Firebase API key", e)
            // Fallback to BuildConfig in case of encryption failure
            return@withContext BuildConfig.FIREBASE_API_KEY
        } catch (e: IOException) {
            Log.e(TAG, "I/O exception when accessing Firebase API key", e)
            // Fallback to BuildConfig in case of I/O failure
            return@withContext BuildConfig.FIREBASE_API_KEY
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error when accessing Firebase API key", e)
            // Fallback to BuildConfig for any other error
            return@withContext BuildConfig.FIREBASE_API_KEY
        }
    }

    /**
     * Updates the Gemini API key in secure storage.
     * This is useful for dynamic API key updates without requiring app updates.
     *
     * @param newApiKey The new API key to store
     * @return True if update succeeded, false otherwise
     */
    suspend fun updateGeminiApiKey(newApiKey: String): Boolean = withContext(Dispatchers.IO) {
        if (newApiKey.isBlank()) {
            Log.e(TAG, "Attempted to update Gemini API key with blank value")
            return@withContext false
        }

        try {
            val encryptedPrefs = getEncryptedSharedPreferences()
            encryptedPrefs.edit()
                .putString(GEMINI_API_KEY, newApiKey)
                .putString(API_KEY_VERSION, CURRENT_VERSION)
                .apply()
            Log.d(TAG, "Gemini API key updated successfully")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update Gemini API key", e)
            return@withContext false
        }
    }

    /**
     * Creates and returns an instance of EncryptedSharedPreferences.
     * This uses Android's secure encryption to protect API keys.
     *
     * @return EncryptedSharedPreferences instance
     * @throws GeneralSecurityException If there's a security error creating the preferences
     * @throws IOException If there's an I/O error creating the preferences
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    private fun getEncryptedSharedPreferences() = EncryptedSharedPreferences.create(
        context,
        PREF_FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}