/**
 * file path: app/src/main/java/com/example/askchinna/ui/results/SummaryView.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.1
 */

package com.example.askchinna.ui.results

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewSummaryBinding
import android.graphics.PorterDuffColorFilter

/**
 * Custom view that displays a summary of the identification result,
 * including crop name, issue name, and confidence level.
 */
class SummaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val TAG = "SummaryView"
    
    // Direct initialization of binding
    private val binding: ViewSummaryBinding = ViewSummaryBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        try {
            // Load custom attributes if any
            attrs?.let {
                val typedArray = context.obtainStyledAttributes(it, R.styleable.SummaryView)
                try {
                    // Apply attributes
                    val textColor = typedArray.getColor(
                        R.styleable.SummaryView_summaryTextColor,
                        ContextCompat.getColor(context, R.color.text_primary)
                    )
                    val iconSize = typedArray.getDimensionPixelSize(
                        R.styleable.SummaryView_summaryIconSize,
                        resources.getDimensionPixelSize(R.dimen.icon_size_medium)
                    )

                    binding.textCropName.setTextColor(textColor)
                    binding.textIssueName.setTextColor(textColor)
                    binding.imageCropIcon.layoutParams.width = iconSize
                    binding.imageCropIcon.layoutParams.height = iconSize
                } finally {
                    typedArray.recycle()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SummaryView", e)
            throw e
        }
    }

    /**
     * Sets the data to be displayed in this view
     *
     * @param cropName Name of the crop
     * @param issueName Name of the identified issue
     * @param confidence Confidence level of the identification (0.0 to 1.0)
     */
    fun setData(cropName: String, issueName: String, confidence: Float) {
        try {
            if (cropName.isBlank() || issueName.isBlank()) {
                Log.e(TAG, "Invalid input data: cropName or issueName is blank")
                return
            }

            if (confidence !in 0.0f..1.0f) {
                Log.e(TAG, "Invalid confidence value: $confidence")
                return
            }

            binding.textCropName.text = cropName
            binding.textIssueName.text = issueName

            // Convert confidence to percentage and set progress
            val confidencePercent = (confidence * 100).toInt()
            binding.progressConfidence.progress = confidencePercent

            // Set confidence text
            binding.textConfidence.text = context.getString(
                R.string.confidence_percent,
                confidencePercent
            )

            // Set the crop icon based on the crop name
            setCropIcon(cropName)

            // Set color of confidence bar based on confidence level
            setConfidenceBarColor(confidence)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting data", e)
        }
    }

    /**
     * Sets the crop icon based on crop name
     */
    private fun setCropIcon(cropName: String) {
        try {
            val iconResId = when {
                cropName.contains("chili", ignoreCase = true) -> R.drawable.ic_chili
                cropName.contains("okra", ignoreCase = true) -> R.drawable.ic_okra
                cropName.contains("maize", ignoreCase = true) -> R.drawable.ic_maize
                cropName.contains("cotton", ignoreCase = true) -> R.drawable.ic_cotton
                cropName.contains("tomato", ignoreCase = true) -> R.drawable.ic_tomato
                cropName.contains("watermelon", ignoreCase = true) -> R.drawable.ic_watermelon
                cropName.contains("soybean", ignoreCase = true) -> R.drawable.ic_soybean
                cropName.contains("rice", ignoreCase = true) -> R.drawable.ic_rice
                cropName.contains("wheat", ignoreCase = true) -> R.drawable.ic_wheat
                cropName.contains("pigeon pea", ignoreCase = true) -> R.drawable.ic_pigeon_pea
                else -> R.drawable.ic_identification
            }

            binding.imageCropIcon.setImageResource(iconResId)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting crop icon", e)
            // Set default icon on error
            binding.imageCropIcon.setImageResource(R.drawable.ic_identification)
        }
    }

    /**
     * Sets the color of the confidence progress bar based on confidence level
     */
    private fun setConfidenceBarColor(confidence: Float) {
        try {
            val colorResId = when {
                confidence >= 0.7f -> R.color.confidence_high
                confidence >= 0.4f -> R.color.confidence_medium
                else -> R.color.confidence_low
            }

            val color = ContextCompat.getColor(context, colorResId)
            binding.progressConfidence.progressDrawable?.colorFilter = PorterDuffColorFilter(
                color,
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting confidence bar color", e)
            // Set default color on error
            val defaultColor = ContextCompat.getColor(context, R.color.confidence_medium)
            binding.progressConfidence.progressDrawable?.colorFilter = PorterDuffColorFilter(
                defaultColor,
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    /**
     * Resets the view to its initial state
     */
    fun reset() {
        try {
            binding.textCropName.text = ""
            binding.textIssueName.text = ""
            binding.textConfidence.text = ""
            binding.progressConfidence.progress = 0
            binding.imageCropIcon.setImageResource(R.drawable.ic_identification)
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting view", e)
        }
    }
}