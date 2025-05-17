// file path: app/src/main/java/com/example/askchinna/data/local/SharedPreferencesManager.kt
// created by Chinna on 2023-10-01
// version 1.2
// This file is part of AskChinna.

package com.example.askchinna.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.core.content.edit
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * Manages local storage using SharedPreferences and EncryptedSharedPreferences.
 * Handles user data, session information, and sensitive data storage.
 *
 * @property context Application context for accessing SharedPreferences
 */
@Singleton
class SharedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE_NAME = "askchinna_prefs"
        private const val ENCRYPTED_PREFS_FILE_NAME = "askchinna_encrypted_prefs"

        // Session keys
        private const val KEY_SESSION_START_TIME = "session_start_time"
        private const val KEY_AUTH_STATE = "auth_state"

        // Usage tracking keys
        private const val KEY_USAGE_COUNT = "usage_count"
        private const val KEY_USAGE_RESET_DATE = "usage_reset_date"

        // User data keys
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_MOBILE = "user_mobile"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DISPLAY_NAME_TEMP = "temp_display_name"

        // Encrypted keys
        private const val KEY_API_KEY = "api_key"
        private const val KEY_AUTH_TOKEN = "auth_token"

        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        // Constants
        const val MAX_MONTHLY_IDENTIFICATIONS = 5
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    private var encryptedPrefs: SharedPreferences? = null
    private val TAG = "SharedPreferencesManager"
    
    private fun getEncryptedPrefs(): SharedPreferences {
        if (encryptedPrefs == null) {
            try {
                // Check Android version for encryption support
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val keyGenParameterSpec = androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
                    val mainKeyAlias = androidx.security.crypto.MasterKeys.getOrCreate(keyGenParameterSpec)

                    encryptedPrefs = EncryptedSharedPreferences.create(
                        ENCRYPTED_PREFS_FILE_NAME,
                        mainKeyAlias,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    Log.d(TAG, "Encrypted preferences initialized successfully")
                } else {
                    // Fall back to regular prefs for older Android versions
                    Log.d(TAG, "Android version < M, using regular preferences")
                    encryptedPrefs = prefs
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create encrypted preferences, falling back to regular prefs", e)
                
                // Check if this is a keyset corruption error
                if (e.message?.contains("invalid tag") == true || 
                    e.cause?.message?.contains("invalid tag") == true) {
                    Log.w(TAG, "Detected corrupted keyset, attempting to recreate...")
                    
                    try {
                        // Delete the corrupted encrypted preferences file
                        val encryptedPrefsFile = context.getSharedPreferences(ENCRYPTED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
                        encryptedPrefsFile.edit().clear().apply()
                        
                        // Delete the physical files if they exist
                        val prefsDir = context.filesDir.parentFile?.resolve("shared_prefs")
                        val encryptedFile = prefsDir?.resolve("$ENCRYPTED_PREFS_FILE_NAME.xml")
                        val keysetFile = prefsDir?.resolve("${ENCRYPTED_PREFS_FILE_NAME}_keyset__")
                        
                        encryptedFile?.delete()
                        keysetFile?.delete()
                        
                        // Try creating again with fresh files
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            val keyGenParameterSpec = androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
                            val mainKeyAlias = androidx.security.crypto.MasterKeys.getOrCreate(keyGenParameterSpec)
                            
                            encryptedPrefs = EncryptedSharedPreferences.create(
                                ENCRYPTED_PREFS_FILE_NAME,
                                mainKeyAlias,
                                context,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                            )
                            Log.d(TAG, "Successfully recreated encrypted preferences after corruption")
                        } else {
                            encryptedPrefs = prefs
                        }
                    } catch (recreateError: Exception) {
                        Log.e(TAG, "Failed to recreate encrypted preferences, using regular prefs", recreateError)
                        encryptedPrefs = prefs
                    }
                } else {
                    // Other types of errors, just fallback
                    encryptedPrefs = prefs
                }
            }
        }
        return encryptedPrefs!!
    }

    /**
     * Initialize the SharedPreferencesManager.
     * Attempts to set up encrypted preferences, falls back to regular if fails.
     */
    fun initialize() {
        try {
            // Try to initialize encrypted preferences
            val encPrefs = getEncryptedPrefs()
            encPrefs.edit().putString("init_test", "test").apply()
            encPrefs.getString("init_test", null)
            encPrefs.edit().remove("init_test").apply()
            Log.d(TAG, "Encrypted preferences initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize encrypted preferences, using regular prefs as fallback", e)
            // Don't throw exception, just log and continue
        }
    }

    // Session Management
    fun saveSessionStartTime(timeMillis: Long) =
        prefs.edit { putLong(KEY_SESSION_START_TIME, timeMillis) }

    fun getSessionStartTime(): Long =
        prefs.getLong(KEY_SESSION_START_TIME, 0L)

    // Auth State
    fun saveAuthState(isAuthenticated: Boolean) {
        prefs.edit { putBoolean(KEY_AUTH_STATE, isAuthenticated) }
    }

    fun getAuthState(): Boolean {
        return prefs.getBoolean(KEY_AUTH_STATE, false)
    }

    private fun getUsageCount(): Int =
        prefs.getInt(KEY_USAGE_COUNT, 0)

    // User Data
    fun saveUser(user: User) {
        prefs.edit {
            putString(KEY_USER_ID, user.uid)
            putString(KEY_USER_MOBILE, user.mobileNumber)
            putString(KEY_USER_NAME, user.displayName)
        }
    }

    fun getUser(): User? {
        val uid = prefs.getString(KEY_USER_ID, null) ?: return null
        val mobile = prefs.getString(KEY_USER_MOBILE, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""

        return User(
            uid = uid,
            mobileNumber = mobile,
            displayName = name,
            isVerified = true,
            usageCount = getUsageCount()
        )
    }

    // Display Name Temporary Storage
    fun saveDisplayName(displayName: String) {
        prefs.edit { putString(KEY_DISPLAY_NAME_TEMP, displayName) }
    }

    fun getDisplayName(): String {
        return prefs.getString(KEY_DISPLAY_NAME_TEMP, "") ?: ""
    }

    fun clearDisplayName() {
        prefs.edit { remove(KEY_DISPLAY_NAME_TEMP) }
    }

    // Usage Limit
    fun saveUsageLimit(usageLimit: UsageLimit) {
        prefs.edit {
            putInt(KEY_USAGE_COUNT, usageLimit.usageCount)
            putLong(KEY_USAGE_RESET_DATE, usageLimit.lastUpdated.time)
        }
    }

    fun getUsageLimit(): UsageLimit? {
        val count = prefs.getInt(KEY_USAGE_COUNT, 0)
        val lastUpdated = prefs.getLong(KEY_USAGE_RESET_DATE, 0L)
        if (lastUpdated == 0L) return null

        return UsageLimit(
            usageCount = count,
            lastUpdated = Date(lastUpdated),
            isLimitReached = count >= MAX_MONTHLY_IDENTIFICATIONS
        )
    }

    fun saveAuthToken(token: String) {
        try {
            getEncryptedPrefs().edit().putString(KEY_AUTH_TOKEN, token).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save auth token", e)
            // Don't throw, just log
        }
    }

    fun getAuthToken(): String? {
        return try {
            getEncryptedPrefs().getString(KEY_AUTH_TOKEN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get auth token", e)
            null
        }
    }

    /**
     * Checks if onboarding has been completed
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Sets onboarding completion status
     */
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    /**
     * Clear all stored data.
     * This should be called on logout.
     */
    fun clearAll() {
        try {
            prefs.edit().clear().apply()
            encryptedPrefs?.edit()?.clear()?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing preferences", e)
        }
    }

    /**
     * Cleanup resources.
     */
    fun cleanup() {
        // No cleanup needed for SharedPreferences
    }
}

/**
 * Exception thrown when there's a security-related error in SharedPreferencesManager.
 */
class SecurityException(message: String, cause: Throwable? = null) : Exception(message, cause)