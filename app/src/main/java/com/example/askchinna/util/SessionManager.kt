/**
 * File: app/src/main/java/com/example/askchinna/util/SessionManager.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.util

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user session, authentication state, and usage limits
 * Optimized for low-end devices with efficient state management
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val dateTimeUtils: DateTimeUtils
) {
    companion object {
        private const val TAG = "SessionManager"
        private const val DEFAULT_SESSION_TIMEOUT_MINUTES = 10
    }

    // Session state
    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: Flow<Boolean> = _isSessionActive

    private val _sessionStartTime = MutableLiveData<Long>()
    val sessionStartTime: LiveData<Long> = _sessionStartTime

    private val _timeRemainingSeconds = MutableLiveData<Int>()
    val timeRemainingSeconds: LiveData<Int> = _timeRemainingSeconds

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser

    private val _usageLimit = MutableStateFlow<UsageLimit?>(null)
    val usageLimit: Flow<UsageLimit?> = _usageLimit

    init {
        // Load session data if exists
        loadSessionData()
    }

    /**
     * Start a new session
     * @param user Current user
     * @param usageLimit Current usage limit
     */
    fun startSession(user: User, usageLimit: UsageLimit) {
        val currentTime = System.currentTimeMillis()

        // Store session data
        _sessionStartTime.value = currentTime
        _isSessionActive.value = true
        _currentUser.value = user
        _usageLimit.value = usageLimit

        // Initialize time remaining
        updateTimeRemaining()

        // Save to shared preferences
        sharedPreferencesManager.saveSessionStartTime(currentTime)
        sharedPreferencesManager.saveUser(user)
        sharedPreferencesManager.saveUsageLimit(usageLimit)
        sharedPreferencesManager.saveAuthState(true)
    }

    /**
     * End current session
     */
    fun endSession() {
        _isSessionActive.value = false
        _sessionStartTime.value = 0
        _timeRemainingSeconds.value = 0

        // Clear session data from preferences
        sharedPreferencesManager.saveSessionStartTime(0)
        sharedPreferencesManager.saveAuthState(false)
    }

    /**
     * Check if session has expired and end it if needed
     * @return true if session expired and was ended, false otherwise
     */
    fun checkAndHandleSessionExpiry(): Boolean {
        if (!_isSessionActive.value) return false

        val startTime = _sessionStartTime.value ?: 0
        if (startTime <= 0) return false

        val hasExpired = dateTimeUtils.hasSessionExpired(startTime)

        if (hasExpired) {
            endSession()
            return true
        }

        updateTimeRemaining()
        return false
    }

    /**
     * Update time remaining in current session
     */
    fun updateTimeRemaining() {
        val startTime = _sessionStartTime.value ?: 0
        if (startTime <= 0) {
            _timeRemainingSeconds.value = 0
            return
        }

        val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime).toInt()
        val remainingSeconds = (DEFAULT_SESSION_TIMEOUT_MINUTES * 60) - elapsedSeconds

        _timeRemainingSeconds.value = if (remainingSeconds < 0) 0 else remainingSeconds
    }

    /**
     * Load session data from shared preferences
     */
    private fun loadSessionData() {
        val isAuthenticated = sharedPreferencesManager.getAuthState()

        if (isAuthenticated) {
            val storedStartTime = sharedPreferencesManager.getSessionStartTime()
            val storedUser = sharedPreferencesManager.getUser()
            val storedUsageLimit = sharedPreferencesManager.getUsageLimit()

            if (storedStartTime > 0 && storedUser != null) {
                _sessionStartTime.value = storedStartTime
                _currentUser.value = storedUser
                _usageLimit.value = storedUsageLimit

                // Check if session is still valid
                if (!dateTimeUtils.hasSessionExpired(storedStartTime)) {
                    _isSessionActive.value = true
                    updateTimeRemaining()
                } else {
                    // Session expired while app was closed
                    endSession()
                }
            } else {
                // Invalid session data
                endSession()
            }
        }
    }

    /**
     * Update user information
     * @param user Updated user information
     */
    fun updateUser(user: User) {
        _currentUser.value = user
        sharedPreferencesManager.saveUser(user)
    }

    /**
     * Update usage limit information
     * @param usageLimit Updated usage limit
     */
    fun updateUsageLimit(usageLimit: UsageLimit) {
        _usageLimit.value = usageLimit
        sharedPreferencesManager.saveUsageLimit(usageLimit)
    }

    /**
     * Check if user has exceeded monthly usage limit
     * @return true if limit exceeded, false otherwise
     */
    fun hasExceededUsageLimit(): Boolean {
        val limit = _usageLimit.value ?: return false
        return limit.usageCount >= Constants.MAX_MONTHLY_IDENTIFICATIONS
    }

    /**
     * Increment usage count
     * @return Updated usage limit
     */
    fun incrementUsageCount(): UsageLimit {
        val currentLimit = _usageLimit.value ?: UsageLimit(0, Date())

        // Check if we need to reset the counter (new month)
        val updatedLimit = if (dateTimeUtils.isWithinLastNDays(currentLimit.lastUpdated, Constants.DAYS_IN_MONTH)) {
            // Still within the same 30-day period
            currentLimit.copy(usageCount = currentLimit.usageCount + 1, lastUpdated = Date())
        } else {
            // Reset for a new 30-day period
            UsageLimit(1, Date())
        }

        updateUsageLimit(updatedLimit)
        return updatedLimit
    }

    /**
     * Get number of identifications left in current period
     * @return Number of identifications left
     */
    fun getIdentificationsLeft(): Int {
        val currentLimit = _usageLimit.value ?: return Constants.MAX_MONTHLY_IDENTIFICATIONS

        // If last updated is older than 30 days, reset to max
        return if (dateTimeUtils.isWithinLastNDays(currentLimit.lastUpdated, Constants.DAYS_IN_MONTH)) {
            (Constants.MAX_MONTHLY_IDENTIFICATIONS - currentLimit.usageCount).coerceAtLeast(0)
        } else {
            Constants.MAX_MONTHLY_IDENTIFICATIONS
        }
    }

    /**
     * Check if user is authenticated
     * @return true if authenticated, false otherwise
     */
    fun isAuthenticated(): Boolean {
        return sharedPreferencesManager.getAuthState() && _currentUser.value != null
    }

    /**
     * Get current user information
     * @return Current user or null if not authenticated
     */
    fun getCurrentUser(): User? {
        return _currentUser.value
    }

    /**
     * Check if session timeout is approaching (less than 1 minute)
     * @return true if timeout is approaching, false otherwise
     */
    fun isSessionTimeoutApproaching(): Boolean {
        val remaining = _timeRemainingSeconds.value ?: 0
        return remaining in 1..60
    }
}