package com.example.askchinna.data.local

/**
 * app/src/main/java/com/askchinna/data/local/SharedPreferencesManager.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages SharedPreferences for saving user and session data
 */
@Singleton
class SharedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE_NAME = "askchinna_prefs"
        private const val ENCRYPTED_PREFS_FILE_NAME = "askchinna_secure_prefs"

        // Regular preferences keys
        private const val KEY_SESSION_START_TIME = "session_start_time"
        private const val KEY_SESSION_REMAINING_TIME = "session_remaining_time"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_LAST_CROP_SELECTION = "last_crop_selection"

        // Encrypted preferences keys
        private const val KEY_API_KEY = "api_key"
    }

    // Regular SharedPreferences
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_FILE_NAME, Context.MODE_PRIVATE
    )

    // Encrypted SharedPreferences for sensitive data
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Session Management

    /**
     * Save session start time
     * @param timeMillis Time in milliseconds
     */
    fun saveSessionStartTime(timeMillis: Long) {
        prefs.edit().putLong(KEY_SESSION_START_TIME, timeMillis).apply()
    }

    /**
     * Get session start time
     * @return Long Time in milliseconds
     */
    fun getSessionStartTime(): Long {
        return prefs.getLong(KEY_SESSION_START_TIME, 0L)
    }

    /**
     * Save session remaining time
     * @param timeMillis Time in milliseconds
     */
    fun saveSessionRemainingTime(timeMillis: Long) {
        prefs.edit().putLong(KEY_SESSION_REMAINING_TIME, timeMillis).apply()
    }

    /**
     * Get session remaining time
     * @return Long Time in milliseconds
     */
    fun getSessionRemainingTime(): Long {
        return prefs.getLong(KEY_SESSION_REMAINING_TIME, 0L)
    }

    /**
     * Clear session data
     */
    fun clearSessionData() {
        prefs.edit()
            .remove(KEY_SESSION_START_TIME)
            .remove(KEY_SESSION_REMAINING_TIME)
            .apply()
    }

    // API Key Management

    /**
     * Save Gemini API key
     * @param apiKey API key
     */
    fun saveApiKey(apiKey: String) {
        encryptedPrefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    /**
     * Get Gemini API key
     * @return String API key
     */
    fun getApiKey(): String {
        return encryptedPrefs.getString(KEY_API_KEY, "") ?: ""
    }

    // App Settings

    /**
     * Set onboarding completed
     * @param completed Whether onboarding is completed
     */
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    /**
     * Check if onboarding is completed
     * @return Boolean True if onboarding is completed
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Save selected language
     * @param languageCode Language code
     */
    fun saveSelectedLanguage(languageCode: String) {
        prefs.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).apply()
    }

    /**
     * Get selected language
     * @return String Language code
     */
    fun getSelectedLanguage(): String {
        return prefs.getString(KEY_SELECTED_LANGUAGE, "en") ?: "en"
    }

    /**
     * Save last crop selection
     * @param cropId Crop ID
     */
    fun saveLastCropSelection(cropId: String) {
        prefs.edit().putString(KEY_LAST_CROP_SELECTION, cropId).apply()
    }

    /**
     * Get last crop selection
     * @return String Crop ID
     */
    fun getLastCropSelection(): String? {
        return prefs.getString(KEY_LAST_CROP_SELECTION, null)
    }

    /**
     * Clear all preferences (for logout)
     */
    fun clearAllPreferences() {
        prefs.edit().clear().apply()
        // Don't clear encrypted prefs - we want to keep the API key
    }
}