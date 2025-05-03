/**
 * File: app/src/main/java/com/example/askchinna/util/SessionManager.kt
 * Copyright © 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.1.1
 * Description: Manages user session, authentication state, and usage limits.
 */

package com.example.askchinna.util

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.model.User
import com.example.askchinna.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager
) {
    companion object {
        private const val TAG = "SessionManager"
        private const val DEFAULT_SESSION_TIMEOUT_MINUTES = 10
    }

    private val _isSessionActive       = MutableStateFlow(false)
    val isSessionActive: Flow<Boolean> = _isSessionActive

    private val _sessionStartTime       = MutableLiveData<Long>()
    val sessionStartTime: LiveData<Long> = _sessionStartTime

    private val _timeRemainingSeconds   = MutableLiveData<Int>()
    val timeRemainingSeconds: LiveData<Int> = _timeRemainingSeconds

    private val _currentUser            = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser

    private val _usageLimit             = MutableStateFlow<UsageLimit?>(null)
    val usageLimit: Flow<UsageLimit?> = _usageLimit

    init {
        Log.d(TAG, "Initializing SessionManager and loading stored session data")
        loadSessionData()
    }

    /**
     * Starts a new user session.
     */
    fun startSession(user: User, usageLimit: UsageLimit) {
        val now = System.currentTimeMillis()
        Log.i(TAG, "Starting session for user=${user.uid} at $now")
        _sessionStartTime.value = now
        _isSessionActive.value  = true
        _currentUser.value      = user
        _usageLimit.value       = usageLimit
        updateTimeRemaining()

        sharedPreferencesManager.saveSessionStartTime(now)
        sharedPreferencesManager.saveAuthState(true)
        sharedPreferencesManager.saveUser(user)
        sharedPreferencesManager.saveUsageLimit(usageLimit)
    }

    /**
     * Ends the current user session.
     */
    fun endSession() {
        Log.i(TAG, "Ending session")
        _isSessionActive.value       = false
        _sessionStartTime.value      = 0L
        _timeRemainingSeconds.value  = 0

        sharedPreferencesManager.saveSessionStartTime(0L)
        sharedPreferencesManager.saveAuthState(false)
    }

    /**
     * Checks if the session has expired and handles it.
     * @return true if session expired and was ended
     */
    fun checkAndHandleSessionExpiry(): Boolean {
        if (!_isSessionActive.value) return false
        val start = _sessionStartTime.value ?: 0L
        if (start <= 0L) return false

        val expired = DateTimeUtils.hasSessionExpired(start)
        if (expired) {
            Log.w(TAG, "Session expired; ending session")
            endSession()
            return true
        }
        updateTimeRemaining()
        return false
    }

    /**
     * Updates timeRemainingSeconds LiveData based on DEFAULT_SESSION_TIMEOUT_MINUTES.
     */
    private fun updateTimeRemaining() {
        val start = _sessionStartTime.value ?: 0L
        if (start <= 0L) {
            _timeRemainingSeconds.value = 0
            return
        }

        val elapsedSec = TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis() - start)
            .toInt()
        val remainSec = (DEFAULT_SESSION_TIMEOUT_MINUTES * 60) - elapsedSec
        _timeRemainingSeconds.value = remainSec.coerceAtLeast(0)

        Log.d(TAG, "Time remaining (sec) = ${_timeRemainingSeconds.value}")
    }

    /**
     * Loads saved session data (auth, start time, user, usage) from prefs.
     */
    private fun loadSessionData() {
        val isAuthed = sharedPreferencesManager.getAuthState()
        Log.d(TAG, "loadSessionData: isAuthed=$isAuthed")
        if (!isAuthed) {
            endSession()
            return
        }

        val storedStart = sharedPreferencesManager.getSessionStartTime()
        val storedUser  = sharedPreferencesManager.getUser()
        val storedLimit = sharedPreferencesManager.getUsageLimit()
        Log.d(TAG, "Stored start=$storedStart, user=$storedUser, limit=$storedLimit")

        if (storedStart > 0L && storedUser != null && storedLimit != null) {
            _sessionStartTime.value = storedStart
            _currentUser.value      = storedUser
            _usageLimit.value       = storedLimit

            if (!DateTimeUtils.hasSessionExpired(storedStart)) {
                Log.d(TAG, "Restored active session")
                _isSessionActive.value = true
                updateTimeRemaining()
            } else {
                Log.w(TAG, "Restored session has expired")
                endSession()
            }
        } else {
            endSession()
        }
    }

    /**
     * Updates only the user in session and persists it.
     */
    fun updateUser(user: User) {
        Log.d(TAG, "Updating current user to ${user.uid}")
        _currentUser.value = user
        sharedPreferencesManager.saveUser(user)
    }

    /**
     * Updates the usage limit in memory and prefs.
     */
    fun updateUsageLimit(limit: UsageLimit) {
        Log.d(TAG, "Updating usage limit: $limit")
        _usageLimit.value = limit
        sharedPreferencesManager.saveUsageLimit(limit)
    }

    /**
     * Checks if the user has exceeded their monthly usage limit.
     */
    fun hasExceededUsageLimit(): Boolean {
        val limit = _usageLimit.value ?: return false
        val exceeded = limit.usageCount >= Constants.MAX_MONTHLY_IDENTIFICATIONS
        Log.d(TAG, "hasExceededUsageLimit = $exceeded")
        return exceeded
    }

    /**
     * Increments the usage count (new Date) and persists the updated UsageLimit.
     * Returns the new UsageLimit.
     */
    fun incrementUsageCount(): UsageLimit {
        val current = _usageLimit.value
            ?: UsageLimit(0, Date(), isLimitReached = false)

        val withinWindow = DateTimeUtils.isWithinLastNDays(
            current.lastUpdated, Constants.DAYS_IN_MONTH
        )

        val newCount = if (withinWindow) {
            current.usageCount + 1
        } else {
            1
        }
        val newDate = Date()
        val newLimitReached = newCount >= Constants.MAX_MONTHLY_IDENTIFICATIONS
        val next = UsageLimit(newCount, newDate, newLimitReached)

        Log.i(TAG, "incrementUsageCount: from=${current.usageCount} to=$newCount, reached=$newLimitReached")
        updateUsageLimit(next)
        return next
    }

    /**
     * Returns how many identifications the user has left in the current window.
     */
    fun getIdentificationsLeft(): Int {
        val current = _usageLimit.value
        val left = if (current != null && DateTimeUtils.isWithinLastNDays(
                current.lastUpdated, Constants.DAYS_IN_MONTH
            )) {
            (Constants.MAX_MONTHLY_IDENTIFICATIONS - current.usageCount)
                .coerceAtLeast(0)
        } else {
            Constants.MAX_MONTHLY_IDENTIFICATIONS
        }
        Log.d(TAG, "getIdentificationsLeft = $left")
        return left
    }

    /**
     * @return true if the user is currently authenticated and session active.
     */
    fun isAuthenticated(): Boolean {
        val auth = sharedPreferencesManager.getAuthState() && _currentUser.value != null
        Log.d(TAG, "isAuthenticated = $auth")
        return auth
    }

    /**
     * @return the current user, or null if none.
     */
    fun getCurrentUser(): User? = _currentUser.value

    /**
     * Detect if we’re within the last minute of session expiry.
     */
    fun isSessionTimeoutApproaching(): Boolean {
        val remain = _timeRemainingSeconds.value ?: 0
        val approaching = remain in 1..60
        Log.d(TAG, "isSessionTimeoutApproaching = $approaching")
        return approaching
    }
}
