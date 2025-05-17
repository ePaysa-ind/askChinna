/**
 * file path: app/src/main/java/com/example/askchinna/ui/common/ErrorView.kt
 * Copyright © 2025 askChinna
 * Created: April 29, 2025
 * Updated: April 29, 2025
 * Version: 1.1
 */

package com.example.askchinna.ui.common

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewErrorBinding

/**
 * Custom view for displaying error states with standardized UI
 * Optimized for low-literacy users with icon-based communication
 */
class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: ViewErrorBinding? = null
    private var isInitialized = false
    private var onRetryClickListener: (() -> Unit)? = null

    init {
        try {
            orientation = VERTICAL
            gravity = android.view.Gravity.CENTER
            binding = ViewErrorBinding.inflate(LayoutInflater.from(context), this, true)

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
                    binding?.buttonRetry?.visibility = if (showRetryButton) View.VISIBLE else View.GONE

                    // Set custom error icon if provided
                    val iconResId = typedArray.getResourceId(
                        R.styleable.ErrorView_errorIcon,
                        R.drawable.ic_warning
                    )
                    binding?.imageError?.setImageResource(iconResId)
                } finally {
                    typedArray.recycle()
                }
            }

            // Set up click listener for retry button
            binding?.buttonRetry?.setOnClickListener {
                onRetryClickListener?.invoke()
            }

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ErrorView", e)
            throw e
        }
    }

    /**
     * Sets the error message to display
     */
    fun setErrorMessage(message: String) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            if (message.isBlank()) {
                Log.e(TAG, "Invalid error message: blank")
                return
            }

            binding?.textErrorMessage?.text = message
        } catch (e: Exception) {
            Log.e(TAG, "Error setting error message", e)
        }
    }

    /**
     * Sets the error icon to display
     */
    fun setErrorIcon(iconResId: Int) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            if (iconResId <= 0) {
                Log.e(TAG, "Invalid icon resource ID: $iconResId")
                return
            }

            binding?.imageError?.setImageResource(iconResId)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting error icon", e)
        }
    }

    /**
     * Sets the retry button text
     */
    fun setRetryButtonText(text: String) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            if (text.isBlank()) {
                Log.e(TAG, "Invalid retry button text: blank")
                return
            }

            binding?.buttonRetry?.text = text
        } catch (e: Exception) {
            Log.e(TAG, "Error setting retry button text", e)
        }
    }

    /**
     * Shows or hides the retry button
     */
    fun showRetryButton(show: Boolean) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            binding?.buttonRetry?.visibility = if (show) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing/hiding retry button", e)
        }
    }

    /**
     * Sets a listener for retry button clicks
     */
    fun setOnRetryClickListener(listener: () -> Unit) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            onRetryClickListener = listener
        } catch (e: Exception) {
            Log.e(TAG, "Error setting retry click listener", e)
        }
    }

    /**
     * Sets up the error view with the provided message and retry action
     */
    fun setup(message: String, retryAction: (() -> Unit)? = null) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            setErrorMessage(message)

            if (retryAction != null) {
                showRetryButton(true)
                setOnRetryClickListener(retryAction)
            } else {
                showRetryButton(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up error view", e)
        }
    }

    /**
     * Convenient method to set up a network error with standard message
     */
    fun setupNetworkError(retryAction: () -> Unit) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            setErrorIcon(R.drawable.ic_network_offline)
            setErrorMessage(context.getString(R.string.error_network))
            showRetryButton(true)
            setOnRetryClickListener(retryAction)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up network error", e)
        }
    }

    /**
     * Convenient method to set up a generic error with standard message
     */
    fun setupGenericError(retryAction: (() -> Unit)? = null) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "View not initialized")
                return
            }

            setErrorIcon(R.drawable.ic_warning)
            setErrorMessage(context.getString(R.string.error_unknown))

            if (retryAction != null) {
                showRetryButton(true)
                setOnRetryClickListener(retryAction)
            } else {
                showRetryButton(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up generic error", e)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
        onRetryClickListener = null
    }

    companion object {
        private const val TAG = "ErrorView"
    }
}