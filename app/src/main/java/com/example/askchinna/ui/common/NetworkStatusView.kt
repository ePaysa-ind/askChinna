/**
 * File: app/src/main/java/com/example/askchinna/ui/common/NetworkStatusView.kt
 * Copyright © 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 * Description: Network status indicator for the top bar
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
import com.example.askchinna.databinding.ViewNetworkStatusBinding
import com.example.askchinna.util.NetworkState

/**
 * Custom view that displays the current network connectivity status
 */
class NetworkStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: ViewNetworkStatusBinding? = null
    private var isInitialized = false
    private val TAG = "NetworkStatusView"

    init {
        try {
            // Inflate layout
            binding = ViewNetworkStatusBinding.inflate(LayoutInflater.from(context), this, true)

            // Handle custom attributes
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.NetworkStatusView,
                0, 0
            ).apply {
                try {
                    // Set text color
                    val defaultTextColor = ContextCompat.getColor(context, R.color.text_primary)
                    val textColor = getColor(R.styleable.NetworkStatusView_textColor, defaultTextColor)
                    binding?.textNetworkStatus?.setTextColor(textColor)

                    // Set icon size
                    // Provide a default size in case the dimension resource doesn't exist
                    val defaultSize = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
                    val iconSize = getDimensionPixelSize(
                        R.styleable.NetworkStatusView_iconSize,
                        defaultSize
                    )
                    binding?.imageNetworkStatus?.layoutParams?.width = iconSize
                    binding?.imageNetworkStatus?.layoutParams?.height = iconSize
                    binding?.imageNetworkStatus?.requestLayout()
                } finally {
                    recycle()
                }
            }

            // Set up retry button if it exists
            binding?.buttonRetryConnection?.setOnClickListener {
                Log.d(TAG, "Retry button clicked")
                // The parent activity should handle retry logic
                (context as? OnRetryConnectionListener)?.onRetryConnection()
            }

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing view", e)
        }
    }

    /**
     * Set the network status and update UI accordingly
     */
    fun setNetworkStatus(isAvailable: Boolean) {
        if (!isInitialized) {
            Log.w(TAG, "View not initialized")
            return
        }

        try {
            binding?.apply {
                // Update text
                textNetworkStatus.text = context.getString(
                    if (isAvailable) R.string.network_available
                    else R.string.network_unavailable
                )

                // Update icon
                imageNetworkStatus.setImageResource(
                    if (isAvailable) R.drawable.ic_network_available
                    else R.drawable.ic_network_unavailable
                )

                // Update text color
                textNetworkStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (isAvailable) R.color.network_available
                        else R.color.network_unavailable
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting network status", e)
        }
    }

    /**
     * Update network status based on NetworkState
     */
    fun updateNetwork(state: NetworkState) {
        if (!isInitialized) {
            Log.w(TAG, "View not initialized")
            return
        }

        Log.d(TAG, "Network state updated: $state")
        
        binding?.apply {
            when (state) {
                NetworkState.Offline -> {
                    this@NetworkStatusView.visibility = View.VISIBLE
                    textNetworkStatus.text = context.getString(R.string.network_unavailable)
                    imageNetworkStatus.setImageResource(R.drawable.ic_network_unavailable)
                    textNetworkStatus.setTextColor(ContextCompat.getColor(context, R.color.network_unavailable))
                    buttonRetryConnection.visibility = View.VISIBLE
                }
                NetworkState.WiFi, NetworkState.MobileData, NetworkState.MeteredMobileData -> {
                    this@NetworkStatusView.visibility = View.GONE
                    Log.d(TAG, "Network connected - hiding status view")
                }
                else -> {
                    this@NetworkStatusView.visibility = View.GONE
                    Log.d(TAG, "Unknown network state - hiding status view")
                }
            }
        }
    }

    /**
     * Interface for retry connection callback
     */
    interface OnRetryConnectionListener {
        fun onRetryConnection()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }
}