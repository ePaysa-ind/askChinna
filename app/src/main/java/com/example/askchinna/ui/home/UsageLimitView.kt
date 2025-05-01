package com.example.askchinna.ui.home

/**
 * app/src/main/java/com/askchinna/ui/home/UsageLimitView.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.askchinna.R
import com.askchinna.databinding.ViewUsageLimitBinding

/**
 * Custom view for displaying usage limit information
 */
class UsageLimitView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewUsageLimitBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewUsageLimitBinding.inflate(inflater, this, true)

        // Load custom attributes if any
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.UsageLimitView)

            // Apply attributes

            typedArray.recycle()
        }
    }

    /**
     * Set usage limit data
     *
     * @param currentCount Current identification count
     * @param remainingCount Remaining identification count
     * @param maxCount Maximum identification count
     * @param isPremium Whether user is premium/tester
     */
    fun setUsageLimit(currentCount: Int, remainingCount: Int, maxCount: Int, isPremium: Boolean) {
        // For premium users, show unlimited
        if (isPremium) {
            binding.progressBarUsage.max = 100
            binding.progressBarUsage.progress = 50 // Always show half full for premium

            binding.tvUsageCount.text = context.getString(R.string.unlimited_usage)
            binding.tvRemainingCount.text = context.getString(R.string.unlimited)

            // Hide warning and change colors
            binding.ivWarning.visibility = View.GONE
            binding.tvRemainingCount.setTextColor(context.getColor(R.color.premium_text))
            binding.progressBarUsage.progressTintList = context.getColorStateList(R.color.premium_progress)

            return
        }

        // For free users, show count and progress
        binding.progressBarUsage.max = maxCount
        binding.progressBarUsage.progress = currentCount

        binding.tvUsageCount.text = context.getString(
            R.string.usage_count,
            currentCount,
            maxCount
        )

        binding.tvRemainingCount.text = context.getString(
            R.string.remaining_count,
            remainingCount
        )

        // Show warning if low on identifications
        if (remainingCount <= 1) {
            binding.ivWarning.visibility = View.VISIBLE
            binding.tvRemainingCount.setTextColor(context.getColor(R.color.warning_text))
            binding.progressBarUsage.progressTintList = context.getColorStateList(R.color.warning_progress)
        } else {
            binding.ivWarning.visibility = View.GONE
            binding.tvRemainingCount.setTextColor(context.getColor(R.color.normal_text))
            binding.progressBarUsage.progressTintList = context.getColorStateList(R.color.normal_progress)
        }
    }

    /**
     * Show loading state
     *
     * @param isLoading Whether loading is in progress
     */
    fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBarLoading.visibility = View.VISIBLE
            binding.layoutContent.visibility = View.GONE
        } else {
            binding.progressBarLoading.visibility = View.GONE
            binding.layoutContent.visibility = View.VISIBLE
        }
    }

    /**
     * Show error message
     *
     * @param errorMessage Error message
     */
    fun showError(errorMessage: String) {
        binding.tvError.text = errorMessage
        binding.tvError.visibility = View.VISIBLE
        binding.layoutContent.visibility = View.GONE
    }

    /**
     * Hide error message
     */
    fun hideError() {
        binding.tvError.visibility = View.GONE
        binding.layoutContent.visibility = View.VISIBLE
    }
}