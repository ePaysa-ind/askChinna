package com.example.askchinna.ui.home

/**
 * app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 13, 2025
 * Version: 1.2
 * 
 * Changes:
 * - Made timerManager non-nullable
 * - Added state restoration
 * - Added sessionTimeoutMinutes usage
 */

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewSessionTimerBinding

/**
 * Custom view for displaying session timer
 */
@Suppress("DEPRECATION")
class SessionTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = "SessionTimerView"
    
    // Direct initialization of binding - prevents "lateinit not initialized" issues
    private val binding: ViewSessionTimerBinding = ViewSessionTimerBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    
    private var blinkAnimation: Animation? = null
    private lateinit var timerManager: SessionTimerManager
    private var sessionTimeoutMinutes: Int = 0
    private var onSessionExpiredListener: (() -> Unit)? = null
    private var isPaused: Boolean = false

    init {
        Log.d(TAG, "Initializing SessionTimerView")
        
        // Setup blinking animation for warning
        blinkAnimation = AlphaAnimation(1.0f, 0.2f).apply {
            duration = 500
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }

        // Load custom attributes if any
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SessionTimerView)
            try {
                // Apply attributes
                val normalTextColor = typedArray.getColor(
                    R.styleable.SessionTimerView_timerNormalTextColor,
                    ContextCompat.getColor(context, R.color.timer_normal_text)
                )
                typedArray.getColor(
                    R.styleable.SessionTimerView_timerWarningTextColor,
                    ContextCompat.getColor(context, R.color.timer_warning_text)
                )
                typedArray.getColor(
                    R.styleable.SessionTimerView_timerCriticalColor,
                    ContextCompat.getColor(context, R.color.timer_critical)
                )
                typedArray.getColor(
                    R.styleable.SessionTimerView_timerCriticalTextColor,
                    ContextCompat.getColor(context, R.color.timer_critical_text)
                )
                val criticalTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.SessionTimerView_timerCriticalTextSize,
                    resources.getDimensionPixelSize(R.dimen.text_size_large)
                )

                // Apply styles to views through binding
                binding.progressBarTimer.progressTintList = ContextCompat.getColorStateList(context, R.color.timer_normal)
                binding.tvTimeRemaining.setTextColor(normalTextColor)
                binding.tvTimeRemaining.textSize = criticalTextSize.toFloat()
            } finally {
                typedArray.recycle()
            }
        }
    }

    /**
     * Sets the timer manager for this view
     * @param manager The SessionTimerManager instance
     * @throws IllegalStateException if manager is null
     */
    fun setTimerManager(manager: SessionTimerManager) {
        this.timerManager = manager
    }

    /**
     * Update timer display
     *
     * @param formattedTime Time formatted as MM:SS
     * @param percentage Percentage of time remaining (0-100)
     */
    fun updateTimer(formattedTime: String, percentage: Int) {
        try {
            binding.tvTimeRemaining.text = formattedTime
            binding.progressBarTimer.progress = percentage

            // Change color based on remaining time
            when {
                percentage <= 20 -> {
                    // Critical time - red
                    binding.progressBarTimer.progressTintList = ContextCompat.getColorStateList(context, R.color.timer_critical)
                    binding.tvTimeRemaining.setTextColor(ContextCompat.getColor(context, R.color.timer_critical_text))
                    showExpirationWarning(true)
                }
                percentage <= 50 -> {
                    // Warning time - yellow/orange
                    binding.progressBarTimer.progressTintList = ContextCompat.getColorStateList(context, R.color.timer_warning)
                    binding.tvTimeRemaining.setTextColor(ContextCompat.getColor(context, R.color.timer_warning_text))
                    showExpirationWarning(false)
                }
                else -> {
                    // Normal time - green
                    binding.progressBarTimer.progressTintList = ContextCompat.getColorStateList(context, R.color.timer_normal)
                    binding.tvTimeRemaining.setTextColor(ContextCompat.getColor(context, R.color.timer_normal_text))
                    showExpirationWarning(false)
                }
            }

            // Check for session expiration
            if (percentage <= 0) {
                onSessionExpiredListener?.invoke()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating timer", e)
        }
    }

    /**
     * Show/hide expiration warning with blinking animation
     *
     * @param show Whether to show warning
     */
    fun showExpirationWarning(show: Boolean) {
        try {
            if (show) {
                binding.layoutWarning.visibility = View.VISIBLE
                binding.tvWarning.startAnimation(blinkAnimation)
                binding.ivWarningIcon.startAnimation(blinkAnimation)
            } else {
                binding.tvWarning.clearAnimation()
                binding.layoutWarning.visibility = View.GONE
                binding.ivWarningIcon.clearAnimation()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing expiration warning", e)
        }
    }

    /**
     * Set pause/resume state
     *
     * @param isPaused Whether timer is paused
     */
    private fun setPaused(isPaused: Boolean) {
        try {
            this.isPaused = isPaused
            binding.tvPaused.visibility = if (isPaused) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error setting paused state", e)
        }
    }

    /**
     * Set the session timeout in minutes
     * @param minutes Timeout duration in minutes
     */
    fun setSessionTimeoutMinutes(minutes: Int) {
        require(minutes > 0) { "Session timeout must be greater than 0" }
        sessionTimeoutMinutes = minutes
    }

    /**
     * Set the listener for session expiration
     * @param listener Callback to be invoked when session expires
     */
    fun setOnSessionExpiredListener(listener: () -> Unit) {
        onSessionExpiredListener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return Bundle().apply {
            putParcelable("superState", superState)
            putBoolean("isPaused", isPaused)
            putInt("sessionTimeoutMinutes", sessionTimeoutMinutes)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("superState"))
            isPaused = state.getBoolean("isPaused", false)
            sessionTimeoutMinutes = state.getInt("sessionTimeoutMinutes", 0)
            setPaused(isPaused)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            binding.tvWarning.clearAnimation()
            binding.ivWarningIcon.clearAnimation()
            blinkAnimation?.cancel()
            blinkAnimation = null
            if (::timerManager.isInitialized) {
                timerManager.cleanup()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up animations", e)
        }
    }
}