/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.results

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.askchinna.R

/**
 * Custom view that displays a summary of the identification result,
 * including crop name, issue name, and confidence level.
 */
class SummaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val cropNameView: TextView
    private val issueNameView: TextView
    private val confidenceBar: ProgressBar
    private val confidenceTextView: TextView
    private val cropIconView: ImageView

    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.view_summary, this, true)

        // Initialize views
        cropNameView = findViewById(R.id.text_crop_name)
        issueNameView = findViewById(R.id.text_issue_name)
        confidenceBar = findViewById(R.id.progress_confidence)
        confidenceTextView = findViewById(R.id.text_confidence)
        cropIconView = findViewById(R.id.image_crop_icon)
    }

    /**
     * Sets the data to be displayed in this view
     *
     * @param cropName Name of the crop
     * @param issueName Name of the identified issue
     * @param confidence Confidence level of the identification (0.0 to 1.0)
     */
    fun setData(cropName: String, issueName: String, confidence: Float) {
        cropNameView.text = cropName
        issueNameView.text = issueName

        // Convert confidence to percentage and set progress
        val confidencePercent = (confidence * 100).toInt()
        confidenceBar.progress = confidencePercent

        // Set confidence text
        confidenceTextView.text = context.getString(
            R.string.confidence_percent,
            confidencePercent
        )

        // Set the crop icon based on the crop name
        setCropIcon(cropName)

        // Set color of confidence bar based on confidence level
        setConfidenceBarColor(confidence)
    }

    /**
     * Sets the crop icon based on crop name
     */
    private fun setCropIcon(cropName: String) {
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

        cropIconView.setImageResource(iconResId)
    }

    /**
     * Sets the color of the confidence progress bar based on confidence level
     */
    private fun setConfidenceBarColor(confidence: Float) {
        val colorResId = when {
            confidence >= 0.7f -> R.color.confidence_high
            confidence >= 0.4f -> R.color.confidence_medium
            else -> R.color.confidence_low
        }

        val color = ContextCompat.getColor(context, colorResId)
        confidenceBar.progressDrawable.setColorFilter(
            color,
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
}
