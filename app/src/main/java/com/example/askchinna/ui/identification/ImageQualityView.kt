/**
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.ui.identification

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewImageQualityBinding

/**
 * Custom view to display image quality analysis results.
 * Shows visual feedback on image resolution, focus, and brightness.
 */
class ImageQualityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewImageQualityBinding

    init {
        orientation = VERTICAL

        // Inflate layout
        binding = ViewImageQualityBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    /**
     * Set image quality analysis results and update the UI
     * @param imageQuality The result of image quality analysis
     */
    fun setImageQuality(imageQuality: IdentificationViewModel.ImageQualityResult) {
        // Update overall quality status
        binding.tvQualityStatus.apply {
            val statusText = if (imageQuality.isAcceptable) {
                context.getString(R.string.image_quality_good)
            } else {
                context.getString(R.string.image_quality_poor)
            }

            val statusColor = if (imageQuality.isAcceptable) {
                ContextCompat.getColor(context, R.color.colorSuccess)
            } else {
                ContextCompat.getColor(context, R.color.colorWarning)
            }

            text = statusText
            setTextColor(statusColor)
        }

        // Update quality icon
        binding.ivQualityStatus.apply {
            val iconRes = if (imageQuality.isAcceptable) {
                R.drawable.ic_severity_low
            } else {
                R.drawable.ic_severity_medium
            }

            setImageResource(iconRes)

            val tintColor = if (imageQuality.isAcceptable) {
                ContextCompat.getColor(context, R.color.colorSuccess)
            } else {
                ContextCompat.getColor(context, R.color.colorWarning)
            }

            setColorFilter(tintColor)
        }

        // Update individual quality metrics
        updateQualityMetric(
            binding.ivResolution,
            binding.tvResolution,
            imageQuality.isResolutionOk,
            R.string.image_resolution_good,
            R.string.image_resolution_poor
        )

        updateQualityMetric(
            binding.ivFocus,
            binding.tvFocus,
            imageQuality.isFocused,
            R.string.image_focus_good,
            R.string.image_focus_poor
        )

        updateQualityMetric(
            binding.ivBrightness,
            binding.tvBrightness,
            imageQuality.isBrightEnough,
            R.string.image_brightness_good,
            R.string.image_brightness_poor
        )

        // Show error message if any
        imageQuality.errorMessage?.let { message ->
            binding.tvErrorMessage.apply {
                text = message
                visibility = VISIBLE
            }
        } ?: run {
            binding.tvErrorMessage.visibility = GONE
        }
    }

    /**
     * Update a specific quality metric UI
     */
    private fun updateQualityMetric(
        iconView: android.widget.ImageView,
        textView: android.widget.TextView,
        isGood: Boolean,
        goodStringRes: Int,
        poorStringRes: Int
    ) {
        // Set icon
        iconView.apply {
            val iconRes = if (isGood) {
                R.drawable.ic_severity_low
            } else {
                R.drawable.ic_warning
            }

            setImageResource(iconRes)

            val tintColor = if (isGood) {
                ContextCompat.getColor(context, R.color.colorSuccess)
            } else {
                ContextCompat.getColor(context, R.color.colorWarning)
            }

            setColorFilter(tintColor)
        }

        // Set text
        textView.apply {
            val stringRes = if (isGood) goodStringRes else poorStringRes
            text = context.getString(stringRes)

            val textColor = if (isGood) {
                ContextCompat.getColor(context, R.color.colorSuccess)
            } else {
                ContextCompat.getColor(context, R.color.colorWarning)
            }

            setTextColor(textColor)
        }
    }
}