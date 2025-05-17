/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.2
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
import com.example.askchinna.databinding.ViewFeedbackBinding

/**
 * Custom view that allows users to provide feedback
 * about the identification results.
 */
class FeedbackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val TAG = "FeedbackView"
    
    // Direct initialization of binding
    private val binding: ViewFeedbackBinding = ViewFeedbackBinding.inflate(
        LayoutInflater.from(context), this
    )

    private var onFeedbackSubmitted: ((feedbackType: FeedbackType) -> Unit)? = null

    init {
        try {
            // Load custom attributes if any
            attrs?.let {
                val typedArray = context.obtainStyledAttributes(it, R.styleable.FeedbackView)
                try {
                    // Apply attributes
                    val textColor = typedArray.getColor(
                        R.styleable.FeedbackView_feedbackTextColor,
                        ContextCompat.getColor(context, R.color.text_primary)
                    )
                    typedArray.getDimensionPixelSize(
                        R.styleable.FeedbackView_feedbackIconSize,
                        resources.getDimensionPixelSize(R.dimen.icon_size_medium)
                    )

                    // Use binding directly
                    binding.textFeedbackTitle.setTextColor(textColor)
                    binding.radioHelpful.setTextColor(textColor)
                    binding.radioPartiallyHelpful.setTextColor(textColor)
                    binding.radioNotHelpful.setTextColor(textColor)
                } finally {
                    typedArray.recycle()
                }
            }

            // Set up submit button click listener
            binding.buttonSubmitFeedback.setOnClickListener {
                try {
                    handleFeedbackSubmission()
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling feedback submission", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FeedbackView", e)
            throw e
        }
    }

    /**
     * Sets the title for this feedback view
     */
    fun setTitle(title: String) {
        try {
            if (title.isBlank()) {
                Log.e(TAG, "Invalid title: blank")
                return
            }

            binding.textFeedbackTitle.text = title
        } catch (e: Exception) {
            Log.e(TAG, "Error setting title", e)
        }
    }

    /**
     * Sets the callback to be invoked when feedback is submitted
     */
    fun setOnFeedbackSubmittedListener(listener: (feedbackType: FeedbackType) -> Unit) {
        try {
            onFeedbackSubmitted = listener
        } catch (e: Exception) {
            Log.e(TAG, "Error setting feedback listener", e)
        }
    }

    /**
     * Handles the feedback submission process
     */
    private fun handleFeedbackSubmission() {
        try {
            val feedbackType = when (binding.radioGroupFeedback.checkedRadioButtonId) {
                R.id.radioHelpful -> FeedbackType.HELPFUL
                R.id.radioPartiallyHelpful -> FeedbackType.PARTIALLY_HELPFUL
                R.id.radioNotHelpful -> FeedbackType.NOT_HELPFUL
                else -> {
                    Log.e(TAG, "No feedback option selected")
                    return
                }
            }

            // Call the listener if set
            onFeedbackSubmitted?.invoke(feedbackType)

            // Show thank you message
            showThankYouMessage()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling feedback submission", e)
        }
    }

    /**
     * Shows the thank you message and resets the form
     */
    private fun showThankYouMessage() {
        try {
            // Hide input elements
            binding.radioGroupFeedback.visibility = View.GONE
            binding.buttonSubmitFeedback.visibility = View.GONE

            // Show thank you message
            binding.textThankYou.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing thank you message", e)
        }
    }

    /**
     * Resets the view to its initial state
     */
    fun reset() {
        try {
            binding.textFeedbackTitle.text = ""
            binding.radioGroupFeedback.clearCheck()
            binding.radioGroupFeedback.visibility = View.VISIBLE
            binding.buttonSubmitFeedback.visibility = View.VISIBLE
            binding.textThankYou.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting view", e)
        }
    }

    // No need to handle binding in onDetachedFromWindow since it's val, not lateinit var
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up any resources if needed
    }
}