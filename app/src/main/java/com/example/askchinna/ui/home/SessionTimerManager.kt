package com.example.askchinna.ui.home
/**
 * app/src/main/java/com/askchinna/ui/home/SessionTimerManager.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.askchinna.data.local.SharedPreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages session timing for enforcing 10-minute maximum sessions
 */
@Singleton
class SessionTimerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager
) {
    companion object {
        private const val SESSION_DURATION_MILLIS = 10 * 60 * 1000L // 10 minutes in milliseconds
        private const val COUNTDOWN_INTERVAL = 1000L // 1 second
    }

    private var countDownTimer: CountDownTimer? = null

    // LiveData for remaining time in milliseconds
    private val _remainingTimeMillis = MutableLiveData<Long>()
    val remainingTimeMillis: LiveData<Long> = _remainingTimeMillis

    // LiveData for timer state (running/paused)
    private val _isTimerRunning = MutableLiveData<Boolean>()
    val isTimerRunning: LiveData<Boolean> = _isTimerRunning

    // LiveData for session expiration
    private val _isSessionExpired = MutableLiveData<Boolean>()
    val isSessionExpired: LiveData<Boolean> = _isSessionExpired

    /**
     * Start or resume the session timer
     */
    fun startTimer() {
        // First check if we have a saved session in progress
        val savedTimeMillis = sharedPreferencesManager.getSessionRemainingTime()

        // Calculate remaining time
        val currentTimeMillis = System.currentTimeMillis()
        val sessionStartMillis = sharedPreferencesManager.getSessionStartTime()
        val elapsedMillis = if (sessionStartMillis > 0) currentTimeMillis - sessionStartMillis else 0

        // Calculate remaining time (either from saved value or by calculating elapsed time)
        val remainingMillis = if (savedTimeMillis > 0) {
            savedTimeMillis
        } else if (elapsedMillis > 0 && elapsedMillis < SESSION_DURATION_MILLIS) {
            SESSION_DURATION_MILLIS - elapsedMillis
        } else {
            SESSION_DURATION_MILLIS
        }

        // If timer already expired, notify and don't start
        if (remainingMillis <= 0) {
            _remainingTimeMillis.value = 0L
            _isSessionExpired.value = true
            _isTimerRunning.value = false
            return
        }

        // Cancel any existing timer
        countDownTimer?.cancel()

        // Start new timer with remaining time
        countDownTimer = object : CountDownTimer(remainingMillis, COUNTDOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTimeMillis.value = millisUntilFinished
                // Save current time to preferences
                sharedPreferencesManager.saveSessionRemainingTime(millisUntilFinished)
            }

            override fun onFinish() {
                _remainingTimeMillis.value = 0L
                _isSessionExpired.value = true
                _isTimerRunning.value = false
                // Clear session data when expired
                sharedPreferencesManager.clearSessionData()
            }
        }.start()

        // Save session start time if starting fresh
        if (sessionStartMillis <= 0) {
            sharedPreferencesManager.saveSessionStartTime(currentTimeMillis)
        }

        _isTimerRunning.value = true
        _isSessionExpired.value = false
    }

    /**
     * Pause the session timer
     */
    fun pauseTimer() {
        countDownTimer?.cancel()
        _isTimerRunning.value = false

        // Save the current state
        _remainingTimeMillis.value?.let {
            sharedPreferencesManager.saveSessionRemainingTime(it)
        }
    }

    /**
     * Reset the session timer
     */
    fun resetTimer() {
        countDownTimer?.cancel()
        _remainingTimeMillis.value = SESSION_DURATION_MILLIS
        _isTimerRunning.value = false
        _isSessionExpired.value = false

        // Clear session data
        sharedPreferencesManager.clearSessionData()
    }

    /**
     * Format remaining time as MM:SS
     * @return String Formatted time
     */
    fun getFormattedTimeRemaining(): String {
        val millis = _remainingTimeMillis.value ?: 0L
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Get remaining time as percentage (for progress visualization)
     * @return Int Percentage of time remaining
     */
    fun getRemainingTimePercentage(): Int {
        val millis = _remainingTimeMillis.value ?: 0L
        return ((millis.toFloat() / SESSION_DURATION_MILLIS) * 100).toInt()
    }

    /**
     * Check if session is about to expire (less than 1 minute left)
     * @return Boolean True if session is about to expire
     */
    fun isSessionAboutToExpire(): Boolean {
        val millis = _remainingTimeMillis.value ?: 0L
        return millis > 0 && millis < 60 * 1000L
    }
}
