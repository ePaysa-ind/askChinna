package com.example.askchinna.ui.home

/**
 * app/src/main/java/com/askchinna/ui/home/SessionTimerView.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.constraintlayout.widget.ConstraintLayout
import com.askchinna.R
import com.askchinna.databinding.ViewSessionTimerBinding

/**
 * Custom view for displaying session timer
 */
class SessionTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSessionTimerBinding
    private var blinkAnimation: Animation? = null

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewSessionTimerBinding.inflate(inflater, this, true)

        // Setup blinking animation for warning
        blinkAnimation = AlphaAnimation(1.0f, 0.2f).apply {
            duration = 500
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }

        // Load custom attributes if any
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SessionTimerView)

            // Apply attributes

            typedArray.recycle()
        }
    }

    /**
     * Update timer display
     *
     * @param formattedTime Time formatted as MM:SS
     * @param percentage Percentage of time remaining (0-100)
     */
    fun updateTimer(formattedTime: String, percentage: Int) {
        binding.tvTimeRemaining.text = formattedTime
        binding.progressBarTimer.progress = percentage

        // Change color based on remaining time
        when {
            percentage <= 20 -> {
                // Critical time - red
                binding.progressBarTimer.progressTintList = context.getColorStateList(R.color.timer_critical)
                binding.tvTimeRemaining.setTextColor(context.getColor(R.color.timer_critical_text))
            }
            percentage <= 50 -> {
                // Warning time - yellow/orange
                binding.progressBarTimer.progressTintList = context.getColorStateList(R.color.timer_warning)
                binding.tvTimeRemaining.setTextColor(context.getColor(R.color.timer_warning_text))
            }
            else -> {
                // Normal time - green
                binding.progressBarTimer.progressTintList = context.getColorStateList(R.color.timer_normal)
                binding.tvTimeRemaining.setTextColor(context.getColor(R.color.timer_normal_text))
            }
        }
    }

    /**
     * Show/hide expiration warning with blinking animation
     *
     * @param show Whether to show warning
     */
    fun showExpirationWarning(show: Boolean) {
        if (show) {
            binding.tvWarning.visibility = View.VISIBLE
            binding.tvWarning.startAnimation(blinkAnimation)
            binding.ivWarningIcon.visibility = View.VISIBLE
            binding.ivWarningIcon.startAnimation(blinkAnimation)
        } else {
            binding.tvWarning.clearAnimation()
            binding.tvWarning.visibility = View.GONE
            binding.ivWarningIcon.clearAnimation()
            binding.ivWarningIcon.visibility = View.GONE
        }
    }

    /**
     * Set pause/resume state
     *
     * @param isPaused Whether timer is paused
     */
    fun setPaused(isPaused: Boolean) {
        if (isPaused) {
            binding.tvPaused.visibility = View.VISIBLE
        } else {
            binding.tvPaused.visibility = View.GONE
        }
    }
}