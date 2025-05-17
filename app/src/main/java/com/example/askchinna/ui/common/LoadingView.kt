/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: April 29, 2025
 * Version: 1.1
 */

package com.example.askchinna.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewLoadingBinding
import java.util.Timer

/**
 * Custom view for displaying loading states with standardized UI
 * Optimized for low-end devices with minimal overhead
 */
class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: ViewLoadingBinding? = null
    private var tipTimer: Timer? = null

    init {
        orientation = VERTICAL
        gravity = android.view.Gravity.CENTER
        binding = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)

        // Apply custom attributes if provided
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.LoadingView, 0, 0)
            try {
                // Set loading message if provided
                val message = typedArray.getString(R.styleable.LoadingView_loadingMessage)
                if (!message.isNullOrEmpty()) {
                    setLoadingMessage(message)
                }

                // Set loading tip visibility
                val showTip = typedArray.getBoolean(R.styleable.LoadingView_showTip, false)
                binding?.textLoadingTip?.visibility = if (showTip) View.VISIBLE else View.GONE

                // Set progress indicator size
                val size = typedArray.getDimensionPixelSize(
                    R.styleable.LoadingView_indicatorSize,
                    resources.getDimensionPixelSize(R.dimen.progress_size_medium)
                )
                binding?.progressCircular?.indicatorSize = size

                // Set progress indicator color
                val color = typedArray.getColor(
                    R.styleable.LoadingView_indicatorColor,
                    context.getColor(R.color.progress_primary)
                )
                binding?.progressCircular?.setIndicatorColor(color)
            } finally {
                typedArray.recycle()
            }
        }
    }

    /**
     * Sets the loading message to display
     */
    private fun setLoadingMessage(message: String) {
        binding?.textLoadingMessage?.text = message
    }

    /**
     * Stops showing rotating tips
     */
    private fun stopRotatingTips() {
        tipTimer?.cancel()
        tipTimer = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRotatingTips()
        binding = null
    }
    companion object
}
