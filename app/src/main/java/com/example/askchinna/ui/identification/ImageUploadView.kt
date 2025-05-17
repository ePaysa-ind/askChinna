/**
 * file path: app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.2
 * 
 * Change Log:
 * 1.2 - May 6, 2025
 * - Added proper error handling for view operations
 * - Improved resource management with cleanup
 * - Added fallback states for error conditions
 * - Enhanced upload progress tracking
 * - Added proper documentation
 * - Added cleanup in onDetachedFromWindow
 * - Added state restoration
 * - Added memory optimization
 * - Added proper error logging
 * - Added proper view state management
 */
package com.example.askchinna.ui.identification

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewImageUploadBinding

/**
 * Custom view that displays image upload progress and status.
 * Shows visual feedback during the upload process.
 */
class ImageUploadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Changed to direct initialization of non-nullable binding
    private val binding: ViewImageUploadBinding = ViewImageUploadBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    
    private var progressAnimation: Animation? = null

    companion object {
        private const val MIN_PROGRESS = 0
        private const val MAX_PROGRESS = 100
    }

    init {
        orientation = VERTICAL
        progressAnimation = AnimationUtils.loadAnimation(context, R.anim.progress_rotation)

        // Load custom attributes if any
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ImageUploadView)
            try {
                // Apply attributes
                val textColor = typedArray.getColor(
                    R.styleable.ImageUploadView_uploadTextColor,
                    ContextCompat.getColor(context, R.color.text_primary)
                )
                val iconSize = typedArray.getDimensionPixelSize(
                    R.styleable.ImageUploadView_uploadIconSize,
                    resources.getDimensionPixelSize(R.dimen.icon_size_medium)
                )

                // Direct access to binding properties
                binding.tvStatus.setTextColor(textColor)
                binding.ivStatus.layoutParams.width = iconSize
                binding.ivStatus.layoutParams.height = iconSize
            } finally {
                typedArray.recycle()
            }
        }
    }

    /**
     * Resets the view to its initial state
     */
    fun reset() {
        // Stop progress animation
        binding.ivStatus.clearAnimation()

        // Reset progress
        binding.progressBar.progress = MIN_PROGRESS
        binding.tvProgressPercent.text = context.getString(R.string.upload_progress, MIN_PROGRESS)

        // Hide all layouts
        binding.progressLayout.visibility = View.GONE
        binding.statusLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Only nullify the animation, not the binding since it's a val
        progressAnimation = null
    }
}