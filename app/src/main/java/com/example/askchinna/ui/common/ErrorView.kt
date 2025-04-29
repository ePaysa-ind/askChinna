/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.askchinna.R

/**
 * Custom view for displaying error states with standardized UI
 * Optimized for low-literacy users with icon-based communication
 */
class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val errorIcon: ImageView
    private val errorMessage: TextView
    private val retryButton: Button

    private var onRetryClickListener: (() -> Unit)? = null

    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.view_error, this, true)

        // Initialize views
        errorIcon = findViewById(R.id.image_error)
        errorMessage = findViewById(R.id.text_error_message)
        retryButton = findViewById(R.id.button_retry)

        // Set default orientation
        orientation = VERTICAL

        // Center content
        gravity = android.view.Gravity.CENTER

        // Apply custom attributes if provided
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ErrorView, 0, 0)
            try {
                // Set error message if provided
                val message = typedArray.getString(R.styleable.ErrorView_errorMessage)
                if (!message.isNullOrEmpty()) {
                    setErrorMessage(message)
                }

                // Set retry button visibility
                val showRetryButton = typedArray.getBoolean(R.styleable.ErrorView_showRetryButton, true)
                retryButton.visibility = if (showRetryButton) VISIBLE else GONE

                // Set custom error icon if provided
                val iconResId = typedArray.getResourceId(
                    R.styleable.ErrorView_errorIcon,
                    R.drawable.ic_warning
                )
                errorIcon.setImageResource(iconResId)
            } finally {
                typedArray.recycle()
            }
        }

        // Set up click listener for retry button
        retryButton.setOnClickListener {
            onRetryClickListener?.invoke()
        }
    }

    /**
     * Sets the error message to display
     *
     * @param message The error message text
     */
    fun setErrorMessage(message: String) {
        errorMessage.text = message
    }

    /**
     * Sets the error icon to display
     *
     * @param iconResId Resource ID of the icon
     */
    fun setErrorIcon(iconResId: Int) {
        errorIcon.setImageResource(iconResId)
    }

    /**
     * Sets the retry button text
     *
     * @param text Button text
     */
    fun setRetryButtonText(text: String) {
        retryButton.text = text
    }

    /**
     * Shows or hides the retry button
     *
     * @param show True to show, false to hide
     */
    fun showRetryButton(show: Boolean) {
        retryButton.visibility = if (show) VISIBLE else GONE
    }

    /**
     * Sets a listener for retry button clicks
     *
     * @param listener Callback to invoke on retry button click
     */
    fun setOnRetryClickListener(listener: () -> Unit) {
        onRetryClickListener = listener
    }

    /**
     * Sets up the error view with the provided message and retry action
     *
     * @param message Error message to display
     * @param retryAction Action to perform on retry button click
     */
    fun setup(message: String, retryAction: (() -> Unit)? = null) {
        setErrorMessage(message)

        if (retryAction != null) {
            showRetryButton(true)
            setOnRetryClickListener(retryAction)
        } else {
            showRetryButton(false)
        }
    }

    /**
     * Convenient method to set up a network error with standard message
     *
     * @param retryAction Action to perform on retry button click
     */
    fun setupNetworkError(retryAction: () -> Unit) {
        setErrorIcon(R.drawable.ic_network_offline)
        setErrorMessage(context.getString(R.string.error_network))
        showRetryButton(true)
        setOnRetryClickListener(retryAction)
    }

    /**
     * Convenient method to set up a generic error with standard message
     *
     * @param retryAction Action to perform on retry button click
     */
    fun setupGenericError(retryAction: (() -> Unit)? = null) {
        setErrorIcon(R.drawable.ic_warning)
        setErrorMessage(context.getString(R.string.error_unknown))

        if (retryAction != null) {
            showRetryButton(true)
            setOnRetryClickListener(retryAction)
        } else {
            showRetryButton(false)
        }
    }
}