/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.results

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.askchinna.R

/**
 * Custom view that displays expandable detailed information
 * about the identified crop issue, with a collapsible body.
 */
class DetailExpandableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val contentView: TextView
    private val expandIcon: ImageView
    private val headerContainer: LinearLayout
    private val contentContainer: LinearLayout
    private val iconView: ImageView
    private val severityIcon: ImageView

    private var isExpanded = false
    private var animationDuration = 300L // Animation duration in milliseconds

    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.view_detail_expandable, this, true)

        // Initialize views
        titleView = findViewById(R.id.text_detail_title)
        contentView = findViewById(R.id.text_detail_content)
        expandIcon = findViewById(R.id.image_expand_collapse)
        headerContainer = findViewById(R.id.layout_header)
        contentContainer = findViewById(R.id.layout_expandable_content)
        iconView = findViewById(R.id.image_detail_icon)
        severityIcon = findViewById(R.id.image_severity)

        // Set up click listener for expanding/collapsing
        headerContainer.setOnClickListener {
            toggleExpanded()
        }

        // Initially collapse the content
        contentContainer.visibility = View.GONE
        expandIcon.setImageResource(R.drawable.ic_expand)
    }

    /**
     * Sets the title text for this detail view
     */
    fun setTitle(title: String) {
        titleView.text = title
    }

    /**
     * Sets the detailed content text
     */
    fun setContent(content: String) {
        contentView.text = content
    }

    /**
     * Sets the icon for this detail view
     * @param iconResId Resource ID of the icon to display
     */
    fun setIcon(iconResId: Int) {
        iconView.setImageResource(iconResId)
    }

    /**
     * Sets the severity level which determines the icon shown
     * @param severity Severity level (HIGH, MEDIUM, LOW)
     */
    fun setSeverity(severity: Severity) {
        val iconRes = when (severity) {
            Severity.HIGH -> R.drawable.ic_severity_high
            Severity.MEDIUM -> R.drawable.ic_severity_medium
            Severity.LOW -> R.drawable.ic_severity_low
        }
        severityIcon.setImageResource(iconRes)
    }

    /**
     * Sets the initial expansion state of the view
     * @param expanded Whether the view should be expanded initially
     */
    fun setExpanded(expanded: Boolean) {
        if (expanded != isExpanded) {
            toggleExpanded(false) // Toggle without animation
        }
    }

    /**
     * Toggles the expanded/collapsed state with animation
     * @param animate Whether to animate the transition
     */
    private fun toggleExpanded(animate: Boolean = true) {
        isExpanded = !isExpanded

        // Update the expand/collapse icon
        expandIcon.setImageResource(
            if (isExpanded) R.drawable.ic_collapse else R.drawable.ic_expand
        )

        if (animate) {
            if (isExpanded) {
                // Expand with animation
                contentContainer.visibility = View.VISIBLE
                contentContainer.alpha = 0f
                contentContainer.animate()
                    .alpha(1f)
                    .setDuration(animationDuration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            } else {
                // Collapse with animation
                contentContainer.animate()
                    .alpha(0f)
                    .setDuration(animationDuration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        contentContainer.visibility = View.GONE
                    }
                    .start()
            }
        } else {
            // No animation, just set visibility
            contentContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
        }
    }

    /**
     * Enum representing severity levels
     */
    enum class Severity {
        HIGH, MEDIUM, LOW
    }
}