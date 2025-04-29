/**
 * File: app/src/main/java/com/example/askchinna/data/model/Action.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a recommended action to address identified crop pest/disease
 * Used to display action plans in a visual-first format for low-literacy users
 */
@Parcelize
data class Action(
    /**
     * Type of action to take - used for selecting appropriate icon
     * Supported types include: "spray", "remove", "monitor", "fertilize", "water"
     */
    val actionType: String,

    /**
     * Detailed description of the action to be taken
     * Written in simple language for easy understanding
     */
    val description: String,

    /**
     * Optional time frame for completing the action
     * e.g., "Immediately", "Within 3 days", "Weekly"
     */
    val timeFrame: String? = null,

    /**
     * Optional severity level (1-3) indicating importance of the action
     * 1 = Low priority, 2 = Medium priority, 3 = High priority
     */
    val priority: Int = 2,

    /**
     * Optional additional information for the action
     * Can include dosage information, tools needed, etc.
     */
    val additionalInfo: String? = null
) : Parcelable