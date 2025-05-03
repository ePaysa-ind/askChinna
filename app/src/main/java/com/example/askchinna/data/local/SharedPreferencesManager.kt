// file path: app/src/main/java/com/example/askchinna/data/local/SharedPreferencesManager.kt
// created by Chinna on 2023-10-01
// version 1.2
// This file is part of AskChinna.

package com.example.askchinna.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.model.User
import com.example.askchinna.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE_NAME           = "askchinna_prefs"

        // Basic prefs keys
        private const val KEY_SESSION_START_TIME     = "session_start_time"
        private const val KEY_SESSION_REMAINING_TIME = "session_remaining_time"
        private const val KEY_ONBOARDING_COMPLETED   = "onboarding_completed"
        private const val KEY_SELECTED_LANGUAGE      = "selected_language"
        private const val KEY_LAST_CROP_SELECTION    = "last_crop_selection"

        // User profile keys
        private const val KEY_DISPLAY_NAME           = "display_name"
        private const val KEY_LOCAL_USER_ID          = "local_user_id"
        private const val KEY_LOCAL_USER_MOBILE      = "local_user_mobile"
        private const val KEY_LOCAL_USER_DISPLAY_NAME= "local_user_display_name"
        private const val KEY_LOCAL_USER_IS_VERIFIED = "local_user_is_verified"
        private const val KEY_LOCAL_USER_USAGE_COUNT = "local_user_usage_count"

        // "Secure" keys
        private const val KEY_API_KEY                = "api_key"
        private const val KEY_AUTH_TOKEN             = "auth_token"

        // Session/auth & usage‑tracking keys
        private const val KEY_AUTH_STATE             = "auth_state"
        private const val KEY_USAGE_COUNT            = "usage_count"
        private const val KEY_USAGE_RESET_DATE       = "usage_reset_date"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    //–– Session timing ––
    fun saveSessionStartTime(timeMillis: Long) =
        prefs.edit { putLong(KEY_SESSION_START_TIME, timeMillis) }

    fun getSessionStartTime(): Long =
        prefs.getLong(KEY_SESSION_START_TIME, 0L)

    fun saveSessionRemainingTime(timeMillis: Long) =
        prefs.edit { putLong(KEY_SESSION_REMAINING_TIME, timeMillis) }

    fun getSessionRemainingTime(): Long =
        prefs.getLong(KEY_SESSION_REMAINING_TIME, 0L)

    //–– Onboarding & language ––
    fun setOnboardingCompleted(completed: Boolean) =
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, completed) }

    fun isOnboardingCompleted(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun saveSelectedLanguage(code: String) =
        prefs.edit { putString(KEY_SELECTED_LANGUAGE, code) }

    fun getSelectedLanguage(): String =
        prefs.getString(KEY_SELECTED_LANGUAGE, "en") ?: "en"

    //–– Last crop ––
    fun saveLastCropSelection(cropId: String) =
        prefs.edit { putString(KEY_LAST_CROP_SELECTION, cropId) }

    fun getLastCropSelection(): String? =
        prefs.getString(KEY_LAST_CROP_SELECTION, null)

    //–– Display name ––
    fun saveDisplayName(name: String) =
        prefs.edit { putString(KEY_DISPLAY_NAME, name) }

    fun getDisplayName(): String =
        prefs.getString(KEY_DISPLAY_NAME, "") ?: ""

    fun clearDisplayName() =
        prefs.edit { remove(KEY_DISPLAY_NAME) }

    //–– Local user storage ––
    fun saveLocalUser(user: User) {
        prefs.edit {
            putString(KEY_LOCAL_USER_ID, user.uid)
            putString(KEY_LOCAL_USER_MOBILE, user.mobileNumber)
            putString(KEY_LOCAL_USER_DISPLAY_NAME, user.displayName)
            putBoolean(KEY_LOCAL_USER_IS_VERIFIED, user.isVerified)
            putInt(KEY_LOCAL_USER_USAGE_COUNT, user.usageCount)
        }
    }

    fun getLocalUser(): User {
        val uid      = prefs.getString(KEY_LOCAL_USER_ID, "") ?: ""
        val mobile   = prefs.getString(KEY_LOCAL_USER_MOBILE, "") ?: ""
        val display  = prefs.getString(KEY_LOCAL_USER_DISPLAY_NAME, "") ?: ""
        val verified = prefs.getBoolean(KEY_LOCAL_USER_IS_VERIFIED, false)
        val usage    = prefs.getInt(KEY_LOCAL_USER_USAGE_COUNT, 0)

        // Create User object with only the fields we have in SharedPreferences,
        // leaving Firebase-specific fields as their defaults
        return User(
            uid = uid,
            mobileNumber = mobile,
            displayName = display,
            isVerified = verified,
            usageCount = usage
        )
    }

    //–– Auth state ––
    fun saveAuthState(isAuthenticated: Boolean) =
        prefs.edit { putBoolean(KEY_AUTH_STATE, isAuthenticated) }

    fun getAuthState(): Boolean =
        prefs.getBoolean(KEY_AUTH_STATE, false)

    //–– Convenience ––
    fun saveUser(user: User) = saveLocalUser(user)

    fun getUser(): User? {
        val uid = prefs.getString(KEY_LOCAL_USER_ID, "") ?: ""
        return if (uid.isBlank()) null else getLocalUser()
    }

    //–– Usage limit persistence ––
    fun saveUsageLimit(limit: UsageLimit) {
        prefs.edit {
            putInt(KEY_USAGE_COUNT, limit.usageCount)
            putLong(KEY_USAGE_RESET_DATE, limit.lastUpdated.time)
        }
    }

    fun getUsageLimit(): UsageLimit? {
        if (!prefs.contains(KEY_USAGE_COUNT) || !prefs.contains(KEY_USAGE_RESET_DATE)) {
            return null
        }
        val count       = prefs.getInt(KEY_USAGE_COUNT, 0)
        val resetMillis = prefs.getLong(KEY_USAGE_RESET_DATE, 0L)
        val resetDate   = Date(resetMillis)
        val isLimitReached = count >= Constants.MAX_MONTHLY_IDENTIFICATIONS
        return UsageLimit(count, resetDate, isLimitReached)
    }

    //–– API key & token ––
    fun saveApiKey(apiKey: String) =
        prefs.edit { putString(KEY_API_KEY, apiKey) }

    fun getApiKey(): String =
        prefs.getString(KEY_API_KEY, "") ?: ""

    fun saveAuthToken(token: String) =
        prefs.edit { putString(KEY_AUTH_TOKEN, token) }

    fun getAuthToken(): String =
        prefs.getString(KEY_AUTH_TOKEN, "") ?: ""

    fun clearAuthToken() =
        prefs.edit { remove(KEY_AUTH_TOKEN) }

    //–– Clear all on logout ––
    fun clearAll() =
        prefs.edit { clear() }
}
