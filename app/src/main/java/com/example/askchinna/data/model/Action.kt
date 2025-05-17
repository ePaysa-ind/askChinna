/**
 * File: app/src/main/java/com/example/askchinna/data/model/Action.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 13, 2025
 * Version: 1.2
 */

package com.example.askchinna.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import com.google.gson.annotations.SerializedName

/**
 * Represents a recommended action to address identified crop pest/disease
 * Used to display action plans in a visual-first format for low-literacy users
 */
@Parcelize
data class Action(
    /**
     * Unique identifier for the action.
     */
    @SerializedName("id")
    val id: String,

    /**
     * Title of the action.
     */
    @SerializedName("title")
    val title: String,

    /**
     * Description of the action.
     */
    @SerializedName("description")
    val description: String,

    /**
     * Priority level of the action (1-5).
     */
    @SerializedName("priority")
    val priority: Int,

    /**
     * Category of the action.
     */
    @SerializedName("category")
    val category: ActionCategory,

    /**
     * Status of the action.
     */
    @SerializedName("status")
    val status: ActionStatus = ActionStatus.PENDING,

    /**
     * Timestamp when the action was created.
     */
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Timestamp when the action was last updated.
     */
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    /**
     * Timestamp when the action was completed.
     */
    @SerializedName("completedAt")
    val completedAt: Long? = null,

    /**
     * Additional notes for the action.
     */
    @SerializedName("notes")
    val notes: String? = null,

    /**
     * Resources needed for the action.
     */
    @SerializedName("resources")
    val resources: List<String> = emptyList(),

    /**
     * Steps to complete the action.
     */
    @SerializedName("steps")
    val steps: @RawValue List<ActionStep> = emptyList()
) : Parcelable {

    /**
     * Predefined action types as constants
     * Used for consistent type references
     */
    companion object
}

/**
 * Represents a category of action.
 */
enum class ActionCategory {
    PEST_CONTROL,
    PRUNING,
    MONITORING,

}

/**
 * Represents the status of an action.
 */
enum class ActionStatus {
    PENDING,

}

/**
 * Represents a step in an action.
 */
data class ActionStep(
    /**
     * Unique identifier for the step.
     */
    @SerializedName("id")
    val id: String,

    /**
     * Description of the step.
     */
    @SerializedName("description")
    val description: String,

    /**
     * Whether the step is completed.
     */
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false,

    /**
     * Order of the step in the action.
     */
    @SerializedName("order")
    val order: Int,

    /**
     * Estimated time to complete the step in minutes.
     */
    @SerializedName("estimatedTimeMinutes")
    val estimatedTimeMinutes: Int = 0,

    /**
     * Additional notes for the step.
     */
    @SerializedName("notes")
    val notes: String? = null
)