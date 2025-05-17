/**
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.3
 * 
 * Change Log:
 * 1.3 - May 6, 2025
 * - Fixed view binding initialization
 * - Fixed resource references
 * - Added proper error handling
 * - Improved resource management
 * - Enhanced documentation
 */
package com.example.askchinna.ui.identification

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewImageQualityBinding

/**
 * Custom view that displays the results of image quality analysis.
 * Shows visual feedback for resolution, focus, and brightness.
 */
class ImageQualityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Changed to direct initialization with non-nullable val
    private val binding: ViewImageQualityBinding = ViewImageQualityBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        try {
            orientation = VERTICAL

            // Load custom attributes if any
            attrs?.let {
                val typedArray = context.obtainStyledAttributes(it, R.styleable.ImageQualityView)
                try {
                    // Apply attributes
                    val textColor = typedArray.getColor(
                        R.styleable.ImageQualityView_qualityTextColor,
                        ContextCompat.getColor(context, R.color.text_primary)
                    )
                    val iconSize = typedArray.getDimensionPixelSize(
                        R.styleable.ImageQualityView_qualityIconSize,
                        resources.getDimensionPixelSize(R.dimen.icon_size_medium)
                    )

                    binding.tvQualityStatus.setTextColor(textColor)
                    binding.ivQualityStatus.layoutParams.width = iconSize
                    binding.ivQualityStatus.layoutParams.height = iconSize
                } finally {
                    typedArray.recycle()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ImageQualityView", e)
            throw e
        }
    }

    /**
     * Sets the image quality result and updates the UI accordingly
     */
    fun setImageQuality(result: IdentificationViewModel.ImageQualityResult) {
        try {
            // Resolution check
            updateQualityIndicator(
                binding.ivResolution,
                binding.tvResolution,
                result.isResolutionOk
            )

            // Focus check
            updateQualityIndicator(
                binding.ivFocus,
                binding.tvFocus,
                result.isFocused
            )

            // Brightness check
            updateQualityIndicator(
                binding.ivBrightness,
                binding.tvBrightness,
                result.isBrightEnough
            )

            // Show error message if any
            if (result.errorMessage != null) {
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tvErrorMessage.text = result.errorMessage
            } else {
                binding.tvErrorMessage.visibility = View.GONE
            }

            // Update overall status
            val overallStatus = if (result.isAcceptable) {
                context.getString(R.string.image_quality_good)
            } else {
                context.getString(R.string.image_quality_poor)
            }
            binding.tvQualityStatus.text = overallStatus
            binding.tvQualityStatus.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (result.isAcceptable) R.color.success
                    else R.color.error
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting image quality", e)
        }
    }

    /**
     * Updates a quality indicator with the given state
     */
    private fun updateQualityIndicator(
        iconView: View,
        textView: View,
        isGood: Boolean
    ) {
        try {
            if (iconView is android.widget.ImageView) {
                iconView.setImageResource(
                    if (isGood) R.drawable.ic_check_circle
                    else R.drawable.ic_error
                )
            }
            
            if (textView is android.widget.TextView) {
                textView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (isGood) R.color.success
                        else R.color.error
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating quality indicator", e)
        }
    }

    /**
     * Resets the view to its initial state
     */
    fun reset() {
        try {
            binding.tvErrorMessage.visibility = View.GONE
            binding.tvQualityStatus.text = context.getString(R.string.image_quality_unknown)
            binding.tvQualityStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting view", e)
        }
    }

    // No need to set binding to null in onDetachedFromWindow since it's a val

    companion object {
        private const val TAG = "ImageQualityView"
    }
}