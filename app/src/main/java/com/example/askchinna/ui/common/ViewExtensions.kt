/**
 * File: app/src/main/java/com/example/askchinna/ui/common/ViewExtensions.kt
 * Copyright © 2025 askChinna
 * Created: May 3, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.common

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.askchinna.R

/**
 * Extension function to set error message and button action for error view
 */
fun View.setError(message: String, buttonText: String, onButtonClick: () -> Unit) {
    // Find the error message TextView
    val tvErrorMessage = this.findViewById<TextView>(R.id.text_error_message)

    // Find the retry button
    val btnRetry = this.findViewById<Button>(R.id.button_retry)

    // Set the error message
    tvErrorMessage.text = message

    // Set the button text and click listener
    btnRetry.text = buttonText
    btnRetry.setOnClickListener { onButtonClick() }
}