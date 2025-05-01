/**
 * File: app/src/main/java/com/example/askchinna/ui/home/HomeViewModel.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.model.User
import com.example.askchinna.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the home screen
 * Handles user data, usage limits, and session timer management
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionTimerManager: SessionTimerManager
) : ViewModel() {
    private val TAG = "HomeViewModel"

    // User data
    private val _userData = MutableLiveData<UIState<User>>()
    val userData: LiveData<UIState<User>> = _userData

    // Usage limit data
    private val _usageLimit = MutableLiveData<UIState<UsageLimit>>()
    val usageLimit: LiveData<UIState<UsageLimit>> = _usageLimit

    // Session time remaining
    val sessionTimeRemaining: LiveData<Long> = sessionTimerManager.remainingTimeMillis

    // Session timer state
    val isSessionTimerRunning: LiveData<Boolean> = sessionTimerManager.isTimerRunning

    // Session expiration state
    val isSessionExpired: LiveData<Boolean> = sessionTimerManager.isSessionExpired

    init {
        loadUserData()
        checkUsageLimit()
    }

    /**
     * Load user data from repository
     * Updates UI state with loading, success, or error states
     */
    fun loadUserData() {
        viewModelScope.launch {
            _userData.value = UIState.Loading()
            try {
                userRepository.getCurrentUser()
                    .catch { e ->
                        Log.e(TAG, "Error loading user data", e)
                        _userData.value = UIState.Error(
                            message = e.message ?: "Failed to load user data",
                            retryAction = { loadUserData() }
                        )
                    }
                    .collect { user ->
                        Log.d(TAG, "User data loaded successfully: ${user.name}")
                        _userData.value = UIState.Success(user)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading user data", e)
                _userData.value = UIState.Error(
                    message = "Unexpected error: ${e.message}",
                    retryAction = { loadUserData() }
                )
            }
        }
    }

    /**
     * Check usage limit for current user
     * Updates UI state with loading, success, or error states
     */
    fun checkUsageLimit() {
        viewModelScope.launch {
            _usageLimit.value = UIState.Loading()
            try {
                userRepository.checkUsageLimit()
                    .catch { e ->
                        Log.e(TAG, "Error checking usage limit", e)
                        _usageLimit.value = UIState.Error(
                            message = e.message ?: "Failed to check usage limit",
                            retryAction = { checkUsageLimit() }
                        )
                    }
                    .collect { limit ->
                        Log.d(TAG, "Usage limit checked: ${limit.remainingCount}/${limit.maxCount}")
                        _usageLimit.value = UIState.Success(limit)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error checking usage limit", e)
                _usageLimit.value = UIState.Error(
                    message = "Unexpected error: ${e.message}",
                    retryAction = { checkUsageLimit() }
                )
            }
        }
    }

    /**
     * Increment usage count for current user
     * Updates UI state with loading, success, or error states
     */
    fun incrementUsageCount() {
        viewModelScope.launch {
            try {
                userRepository.incrementUsageCount()
                    .catch { e ->
                        Log.e(TAG, "Error incrementing usage count", e)
                        _usageLimit.value = UIState.Error(
                            message = e.message ?: "Failed to update usage count",
                            retryAction = { incrementUsageCount() }
                        )
                    }
                    .collect { limit ->
                        Log.d(TAG, "Usage count incremented: ${limit.remainingCount}/${limit.maxCount}")
                        _usageLimit.value = UIState.Success(limit)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error incrementing usage count", e)
                _usageLimit.value = UIState.Error(
                    message = "Unexpected error: ${e.message}",
                    retryAction = { incrementUsageCount() }
                )
            }
        }
    }

    /**
     * Start session timer
     * Logs the operation for debugging purposes
     */
    fun startSessionTimer() {
        Log.d(TAG, "Starting session timer")
        sessionTimerManager.startTimer()
    }

    /**
     * Pause session timer
     * Logs the operation for debugging purposes
     */
    fun pauseSessionTimer() {
        Log.d(TAG, "Pausing session timer")
        sessionTimerManager.pauseTimer()
    }

    /**
     * Reset session timer
     * Logs the operation for debugging purposes
     */
    fun resetSessionTimer() {
        Log.d(TAG, "Resetting session timer")
        sessionTimerManager.resetTimer()
    }

    /**
     * Get formatted time remaining in session
     * @return String Formatted time (MM:SS)
     */
    fun getFormattedTimeRemaining(): String {
        return sessionTimerManager.getFormattedTimeRemaining()
    }

    /**
     * Get remaining time as percentage
     * @return Int Percentage of time remaining
     */
    fun getRemainingTimePercentage(): Int {
        return sessionTimerManager.getRemainingTimePercentage()
    }

    /**
     * Check if session is about to expire
     * @return Boolean True if session is about to expire
     */
    fun isSessionAboutToExpire(): Boolean {
        return sessionTimerManager.isSessionAboutToExpire()
    }

    /**
     * Logout current user
     * Resets the session timer and logs the operation
     */
    fun logout() {
        Log.d(TAG, "Logging out user")
        userRepository.logoutUser()
        sessionTimerManager.resetTimer()
    }

    /**
     * Check if user can perform an identification
     * Verifies user hasn't reached their usage limit
     * @return Boolean True if user can perform an identification
     */
    fun canPerformIdentification(): Boolean {
        val usageLimitValue = _usageLimit.value

        // If we're still loading or had an error, assume they can't identify
        if (usageLimitValue !is UIState.Success) {
            Log.w(TAG, "Cannot determine if identification is possible, usage limit state: ${usageLimitValue?.javaClass?.simpleName}")
            return false
        }

        val canIdentify = !usageLimitValue.data.isLimitReached
        Log.d(TAG, "Can perform identification: $canIdentify")
        return canIdentify
    }

    /**
     * Get number of remaining identifications
     * @return Int Number of remaining identifications
     */
    fun getRemainingIdentifications(): Int {
        val usageLimitValue = _usageLimit.value

        val remaining = if (usageLimitValue is UIState.Success) {
            usageLimitValue.data.remainingCount
        } else {
            0
        }

        Log.d(TAG, "Remaining identifications: $remaining")
        return remaining
    }
}