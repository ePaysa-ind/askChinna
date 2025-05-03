/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 2, 2025
 * Version: 1.1
 */

package com.example.askchinna.ui.results

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.askchinna.R

/**
 * Custom view that allows users to provide feedback on the identification result.
 * Collects simple feedback through radio buttons and submits it to the server.
 */
class FeedbackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val radioGroup: RadioGroup
    private val submitButton: Button
    private val titleView: TextView
    private val radioHelpful: RadioButton
    private val radioPartiallyHelpful: RadioButton
    private val radioNotHelpful: RadioButton

    private var onFeedbackSubmittedListener: ((FeedbackType) -> Unit)? = null

    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.view_feedback, this, true)

        // Initialize views
        radioGroup = findViewById(R.id.radio_group_feedback)
        submitButton = findViewById(R.id.button_submit_feedback)
        titleView = findViewById(R.id.text_feedback_title)
        radioHelpful = findViewById(R.id.radio_helpful)
        radioPartiallyHelpful = findViewById(R.id.radio_partially_helpful)
        radioNotHelpful = findViewById(R.id.radio_not_helpful)

        // Set up click listener for submit button
        submitButton.setOnClickListener {
            val selectedFeedback = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_helpful -> FeedbackType.HELPFUL
                R.id.radio_partially_helpful -> FeedbackType.PARTIALLY_HELPFUL
                R.id.radio_not_helpful -> FeedbackType.NOT_HELPFUL
                else -> null
            }

            selectedFeedback?.let {
                onFeedbackSubmittedListener?.invoke(it)
                // Disable radio buttons and submit button after submission
                disableInputs()
                // Show thank you message
                showThankYouState()
            }
        }
    }

    /**
     * Sets a listener to be called when feedback is submitted
     */
    fun setOnFeedbackSubmittedListener(listener: (FeedbackType) -> Unit) {
        onFeedbackSubmittedListener = listener
    }

    /**
     * Disables all input controls after submission
     */
    private fun disableInputs() {
        radioHelpful.isEnabled = false
        radioPartiallyHelpful.isEnabled = false
        radioNotHelpful.isEnabled = false
        submitButton.isEnabled = false
    }

    /**
     * Changes the view to show a thank you message after submission
     */
    private fun showThankYouState() {
        // Using existing strings that are already defined in strings.xml
        submitButton.text = context.getString(R.string.submit_feedback)
        titleView.text = context.getString(R.string.feedback_prompt)

        // Alternative approach: hardcode strings if necessary
        // submitButton.text = "Feedback Submitted"
        // titleView.text = "Thank you for your feedback!"
    }

    /**
     * Resets the view to its initial state
     */
    fun resetView() {
        // Clear selection
        radioGroup.clearCheck()

        // Reset button and title text
        submitButton.text = context.getString(R.string.submit_feedback)
        titleView.text = context.getString(R.string.feedback_prompt)

        // Re-enable controls
        radioHelpful.isEnabled = true
        radioPartiallyHelpful.isEnabled = true
        radioNotHelpful.isEnabled = true
        submitButton.isEnabled = true
    }

    /**
     * Enum representing feedback types
     */
    enum class FeedbackType {
        HELPFUL,
        PARTIALLY_HELPFUL,
        NOT_HELPFUL
    }
}