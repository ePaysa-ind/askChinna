package com.example.askchinna.ui.home

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewUsageLimitBinding
import android.util.Log

/**
 * Custom view for displaying usage limit information.
 */
class UsageLimitView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ViewUsageLimitBinding
    private var isInitialized = false

    init {
        try {
            // Initialize view binding
            binding = ViewUsageLimitBinding.bind(this)

            // Load custom attributes if any
            attrs?.let {
                val typedArray = context.obtainStyledAttributes(it, R.styleable.UsageLimitView)
                try {
                    // Apply attributes
                    val textColor = typedArray.getColor(
                        R.styleable.UsageLimitView_usageLimitTextColor,
                        ContextCompat.getColor(context, R.color.text_primary)
                    )
                    val progressColor = typedArray.getColor(
                        R.styleable.UsageLimitView_usageLimitProgressColor,
                        ContextCompat.getColor(context, R.color.colorPrimary)
                    )

                    binding.apply {
                        tvTitle.setTextColor(textColor)
                        tvCurrentCount.setTextColor(textColor)
                        tvRemainingCount.setTextColor(textColor)
                        tvMaxCount.setTextColor(textColor)
                        // Fix: Create ColorStateList from the RGB int value
                        progressBar.progressTintList = ColorStateList.valueOf(progressColor)
                    }
                } finally {
                    typedArray.recycle()
                }
            }

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing UsageLimitView", e)
            throw e
        }
    }

    /**
     * Updates the view to show either a premium ("unlimited") or free usage state.
     *
     * @param currentCount    How many identifications have been used
     * @param remainingCount  How many remain
     * @param maxCount        The free‑tier maximum
     * @param isPremium       True for premium/unlimited users
     */
    fun setUsageLimit(
        currentCount: Int,
        remainingCount: Int,
        maxCount: Int,
        isPremium: Boolean
    ) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            binding.apply {
                if (isPremium) {
                    // Premium users always see "unlimited"
                    progressBar.max = 100
                    progressBar.progress = 50

                    tvCurrentCount.text = context.getString(R.string.unlimited_usage)
                    tvRemainingCount.text = context.getString(R.string.unlimited)

                    tvPremiumStatus.visibility = View.VISIBLE
                    tvRemainingCount.setTextColor(
                        ContextCompat.getColor(context, R.color.premium)
                    )
                    // Fix: Create ColorStateList directly from the color value
                    progressBar.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.premium)
                    )
                } else {
                    // Free users see actual counts
                    progressBar.max = maxCount
                    progressBar.progress = currentCount

                    tvCurrentCount.text = currentCount.toString()
                    tvRemainingCount.text = remainingCount.toString()
                    tvMaxCount.text = "/ $maxCount"

                    tvPremiumStatus.visibility = View.GONE
                    // Optionally set color based on remainingCount
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting usage limit", e)
        }
    }

    /** Shows or hides the loading indicator in this view. */
    fun showLoading(isLoading: Boolean) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            binding.apply {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                tvTitle.visibility = if (isLoading) View.GONE else View.VISIBLE
                tvCurrentCount.visibility = if (isLoading) View.GONE else View.VISIBLE
                tvRemainingCount.visibility = if (isLoading) View.GONE else View.VISIBLE
                tvMaxCount.visibility = if (isLoading) View.GONE else View.VISIBLE
                tvPremiumStatus.visibility = if (isLoading) View.GONE else View.VISIBLE
                tvError.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading state", e)
        }
    }

    /** Displays an error message over this view. */
    fun showError(errorMessage: String) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            binding.apply {
                tvError.text = errorMessage
                tvError.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                tvTitle.visibility = View.GONE
                tvCurrentCount.visibility = View.GONE
                tvRemainingCount.visibility = View.GONE
                tvMaxCount.visibility = View.GONE
                tvPremiumStatus.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error state", e)
        }
    }

    /** Clears any error message and shows main content again. */
    fun hideError() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            binding.apply {
                tvError.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                tvTitle.visibility = View.VISIBLE
                tvCurrentCount.visibility = View.VISIBLE
                tvRemainingCount.visibility = View.VISIBLE
                tvMaxCount.visibility = View.VISIBLE
                tvPremiumStatus.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding error state", e)
        }
    }

    companion object {
        private const val TAG = "UsageLimitView"
    }
}