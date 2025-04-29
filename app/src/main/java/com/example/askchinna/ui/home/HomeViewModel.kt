package com.example.askchinna.ui.home

/**
 * app/src/main/java/com/askchinna/ui/home/HomeViewModel.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askchinna.data.model.UIState
import com.askchinna.data.model.UsageLimit
import com.askchinna.data.model.User
import com.askchinna.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the home screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionTimerManager: SessionTimerManager
) : ViewModel() {

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
     */
    fun loadUserData() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { state ->
                _userData.value = state
            }
        }
    }

    /**
     * Check usage limit for current user
     */
    fun checkUsageLimit() {
        viewModelScope.launch {
            userRepository.checkUsageLimit().collect { state ->
                _usageLimit.value = state
            }
        }
    }

    /**
     * Increment usage count for current user
     */
    fun incrementUsageCount() {
        viewModelScope.launch {
            userRepository.incrementUsageCount().collect { state ->
                _usageLimit.value = state
            }
        }
    }

    /**
     * Start session timer
     */
    fun startSessionTimer() {
        sessionTimerManager.startTimer()
    }

    /**
     * Pause session timer
     */
    fun pauseSessionTimer() {
        sessionTimerManager.pauseTimer()
    }

    /**
     * Reset session timer
     */
    fun resetSessionTimer() {
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
     */
    fun logout() {
        userRepository.logoutUser()
        sessionTimerManager.resetTimer()
    }

    /**
     * Check if user can perform an identification
     * @return Boolean True if user can perform an identification
     */
    fun canPerformIdentification(): Boolean {
        val usageLimitValue = _usageLimit.value

        // If we're still loading or had an error, assume they can't identify
        if (usageLimitValue !is UIState.Success) {
            return false
        }

        // Check if user has reached their limit
        return !usageLimitValue.data.isLimitReached
    }

    /**
     * Get number of remaining identifications
     * @return Int Number of remaining identifications
     */
    fun getRemainingIdentifications(): Int {
        val usageLimitValue = _usageLimit.value

        return if (usageLimitValue is UIState.Success) {
            usageLimitValue.data.remainingCount
        } else {
            0
        }
    }
}