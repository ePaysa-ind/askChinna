/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.example.askchinna.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random

/**
 * Custom view for displaying loading states with standardized UI
 * Optimized for low-end devices with minimal overhead
 */
class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val progressIndicator: CircularProgressIndicator
    private val loadingMessage: TextView
    private val loadingTip: TextView

    private var tipTimer: Timer? = null
    private val loadingTips = arrayOf(
        R.string.loading_tip,
        R.string.loading_tip_offline,
        R.string.loading_tip_connectivity,
        R.string.loading_tip_low_light,
        R.string.loading_tip_position
    )

    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.view_loading, this, true)

        // Initialize views
        progressIndicator = findViewById(R.id.progress_circular)
        loadingMessage = findViewById(R.id.text_loading_message)
        loadingTip = findViewById(R.id.text_loading_tip)

        // Set default orientation
        orientation = VERTICAL

        // Center content
        gravity = android.view.Gravity.CENTER

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
                loadingTip.visibility = if (showTip) VISIBLE else GONE

                // Set progress indicator size
                val size = typedArray.getDimensionPixelSize(
                    R.styleable.LoadingView_indicatorSize,
                    resources.getDimensionPixelSize(R.dimen.progress_size_medium)
                )
                progressIndicator.indicatorSize = size

                // Set progress indicator color
                val color = typedArray.getColor(
                    R.styleable.LoadingView_indicatorColor,
                    context.getColor(R.color.progress_primary)
                )
                progressIndicator.setIndicatorColor(color)
            } finally {
                typedArray.recycle()
            }
        }
    }

    /**
     * Sets the loading message to display
     *
     * @param message The loading message text
     */
    fun setLoadingMessage(message: String) {
        loadingMessage.text = message
    }

    /**
     * Shows or hides the loading tip
     *
     * @param show True to show, false to hide
     */
    fun showTip(show: Boolean) {
        loadingTip.visibility = if (show) VISIBLE else GONE
    }

    /**
     * Sets the loading tip text
     *
     * @param tipResId Resource ID of the tip string
     */
    fun setTip(tipResId: Int) {
        loadingTip.setText(tipResId)
    }

    /**
     * Starts showing rotating tips
     * The tips help users understand what might be happening during long loading times
     */
    fun startRotatingTips() {
        stopRotatingTips()

        showTip(true)

        tipTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    post {
                        // Show a random tip to keep the user engaged
                        val randomTipResId = loadingTips[Random.nextInt(loadingTips.size)]
                        setTip(randomTipResId)
                    }
                }
            }, 0, TIP_ROTATION_INTERVAL)
        }
    }

    /**
     * Stops showing rotating tips
     */
    fun stopRotatingTips() {
        tipTimer?.cancel()
        tipTimer = null
    }

    /**
     * Shows the loading view with the default loading message
     */
    fun showLoading() {
        visibility = VISIBLE
    }

    /**
     * Shows the loading view with a custom loading message
     *
     * @param message Custom loading message
     * @param showTips Whether to show rotating tips
     */
    fun showLoading(message: String, showTips: Boolean = false) {
        setLoadingMessage(message)

        if (showTips) {
            startRotatingTips()
        } else {
            stopRotatingTips()
            showTip(false)
        }

        visibility = VISIBLE
    }

    /**
     * Hides the loading view
     */
    fun hideLoading() {
        stopRotatingTips()
        visibility = GONE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRotatingTips()
    }

    companion object {
        // Interval for rotating tips in milliseconds (5 seconds)
        private const val TIP_ROTATION_INTERVAL = 5000L
    }
}
