/**
 * file path: app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.common

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewUsageLimitBinding

/**
 * Custom view for displaying usage limits
 * Shows current usage, remaining uses, and premium status
 */
class UsageLimitView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "UsageLimitView"
    }

    // Changed to direct initialization of non-nullable binding
    private var binding: ViewUsageLimitBinding = ViewUsageLimitBinding.inflate(
        LayoutInflater.from(context), this
    )

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
                        progressBar.progressTintList = ContextCompat.getColorStateList(context, progressColor)
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
     * Show error state
     * @param errorMessage Error message to display
     */
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

    // No need to set binding to null in onDetachedFromWindow since it's a val
}