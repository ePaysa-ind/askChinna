package com.example.askchinna.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.askchinna.util.Constants
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * app/src/main/java/com/example/askchinna/ui/home/SessionTimerManager.kt
 * Copyright © 2025 askChinna
 * Created: May 3, 2025
 * Version: 1.2
 *
 * Manages a countdown-based session timer. Exposes LiveData for
 * remaining time, running state, and expiration.
 */
@Singleton
class SessionTimerManager @Inject constructor() {
    // total session duration in ms
    private val initialMillis = Constants.MAX_SESSION_DURATION_MINUTES * 60_000L

    private val _remainingTime = MutableLiveData(initialMillis)
    val remainingTimeMillis: LiveData<Long> = _remainingTime

    private val _isRunning = MutableLiveData(false)

    private val _isExpired = MutableLiveData(false)
    val isSessionExpired: LiveData<Boolean> = _isExpired

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /** Starts or resumes the countdown. No‐op if already running. */
    fun startTimer() {
        try {
            if (_isRunning.value == true) return
            _isExpired.value = false
            _isRunning.value = true

            timerJob = scope.launch {
                try {
                    while ((_remainingTime.value ?: 0L) > 0 && _isRunning.value == true) {
                        delay(1_000L)
                        val next = (_remainingTime.value ?: 0L) - 1_000L
                        _remainingTime.value = next.coerceAtLeast(0L)
                    }
                    if ((_remainingTime.value ?: 0L) == 0L) {
                        _isExpired.value = true
                    }
                } catch (e: Exception) {
                    handleError("Timer error", e)
                } finally {
                    _isRunning.value = false
                }
            }
        } catch (e: Exception) {
            handleError("Failed to start timer", e)
        }
    }

    /** Pauses the countdown. */
    fun pauseTimer() {
        try {
            timerJob?.cancel()
            _isRunning.value = false
        } catch (e: Exception) {
            handleError("Failed to pause timer", e)
        }
    }

    /** Resets the timer to initial duration. */
    fun resetTimer() {
        try {
            pauseTimer()
            _remainingTime.value = initialMillis
            _isExpired.value = false
        } catch (e: Exception) {
            handleError("Failed to reset timer", e)
        }
    }

    /** Cleans up resources when the manager is no longer needed. */
    fun cleanup() {
        try {
            pauseTimer()
            scope.cancel()
        } catch (e: Exception) {
            handleError("Failed to cleanup timer", e)
        }
    }

    /**
     * Formats remaining time as "MM:SS".
     */
    fun getFormattedTimeRemaining(): String {
        return try {
            val millis = _remainingTime.value ?: 0L
            val totalSec = millis / 1000
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            handleError("Failed to format time", e)
            "00:00"
        }
    }

    /**
     * Returns remaining time as percentage [0..100].
     */
    fun getRemainingTimePercentage(): Int {
        return try {
            val curr = _remainingTime.value ?: 0L
            ((curr * 100) / initialMillis).toInt()
        } catch (e: Exception) {
            handleError("Failed to calculate time percentage", e)
            0
        }
    }

    /**
     * True if less than one minute remains.
     */
    fun isSessionAboutToExpire(): Boolean {
        return try {
            val curr = _remainingTime.value ?: 0L
            curr in 1..60_000L
        } catch (e: Exception) {
            handleError("Failed to check session expiration", e)
            false
        }
    }

    /**
     * Handle errors in the timer manager
     * Logs the error and updates state if needed
     */
    private fun handleError(message: String, e: Exception) {
        android.util.Log.e("SessionTimerManager", message, e)
        try {
            _isRunning.value = false
            _isExpired.value = true
        } catch (e: Exception) {
            android.util.Log.e("SessionTimerManager", "Failed to update error state", e)
        }
    }
}
