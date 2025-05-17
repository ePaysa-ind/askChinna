/**
 * file path: app/src/main/java/com/example/askchinna/ui/results/DetailExpandableView.kt
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
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewDetailExpandableBinding

/**
 * Custom view that displays expandable detailed information
 * about the identified crop issue, with a collapsible body.
 */
class DetailExpandableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewDetailExpandableBinding
    private var isExpanded = false
    private var onExpandListener: ((Boolean) -> Unit)? = null

    companion object {
        private const val TAG = "DetailExpandableView"
    }

    init {
        orientation = VERTICAL
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_detail_expandable,
            this,
            true
        )
        binding.isExpanded = isExpanded
        setupClickListeners()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Find the nearest lifecycle owner
        var parent = parent
        while (parent != null) {
            if (parent is androidx.lifecycle.LifecycleOwner) {
                binding.lifecycleOwner = parent
                break
            }
            parent = parent.parent
        }
    }

    override fun onDetachedFromWindow() {
        binding.lifecycleOwner = null
        super.onDetachedFromWindow()
    }

    private fun setupClickListeners() {
        binding.layoutHeader.setOnClickListener {
            toggleExpansion()
        }
    }

    fun setOnExpandListener(listener: (Boolean) -> Unit) {
        onExpandListener = listener
    }

    /**
     * Sets the title text for this detail view
     */
    fun setTitle(title: String) {
        try {
            if (title.isBlank()) {
                Log.e(TAG, "Invalid title: blank")
                return
            }

            binding.textDetailTitle.text = title
        } catch (e: Exception) {
            Log.e(TAG, "Error setting title", e)
        }
    }

    /**
     * Sets the detailed content text
     */
    fun setContent(content: String) {
        try {
            if (content.isBlank()) {
                Log.e(TAG, "Invalid content: blank")
                return
            }

            binding.textDetailContentExpanded.text = content
        } catch (e: Exception) {
            Log.e(TAG, "Error setting content", e)
        }
    }

    /**
     * Sets the icon for this detail view
     * @param resourceId Resource ID of the icon to display
     */
    fun setIcon(resourceId: Int) {
        try {
            binding.imageDetailIcon.setImageResource(resourceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting icon", e)
        }
    }

    /**
     * Sets the severity level which determines the icon shown
     * @param severity The severity level (HIGH, MEDIUM, LOW)
     */
    fun setSeverity(severity: Severity) {
        try {
            val iconResId = when (severity) {
                Severity.HIGH -> R.drawable.ic_severity_high
                Severity.MEDIUM -> R.drawable.ic_severity_medium
                Severity.LOW -> R.drawable.ic_severity_low
            }
            binding.imageSeverity.setImageResource(iconResId)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting severity", e)
        }
    }

    /**
     * Sets the severity level which determines the icon shown
     * @param resourceId Resource ID of the icon to display
     */
    fun setSeverityIcon(resourceId: Int) {
        try {
            binding.imageSeverity.setImageResource(resourceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting severity icon", e)
        }
    }

    /**
     * Toggles the expanded/collapsed state with animation
     */
    private fun toggleExpansion() {
        isExpanded = !isExpanded
        binding.isExpanded = isExpanded
        onExpandListener?.invoke(isExpanded)
    }

    /**
     * Resets the view to its initial state
     */
    fun reset() {
        try {
            binding.textDetailTitle.text = ""
            binding.textDetailContentExpanded.text = ""
            binding.imageDetailIcon.setImageResource(R.drawable.ic_warning)
            binding.imageSeverity.setImageResource(R.drawable.ic_severity_medium)
            binding.imageExpandCollapse.setImageResource(R.drawable.ic_expand)
            isExpanded = false
            binding.isExpanded = isExpanded
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting view", e)
        }
    }

    /**
     * Enum representing severity levels
     */
    enum class Severity {
        HIGH, MEDIUM, LOW
    }
}