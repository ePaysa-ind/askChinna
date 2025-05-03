/**
 * File: app/src/main/java/com/example/askchinna/data/model/IdentificationResult.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

/**
 * Represents the result of a crop pest/disease identification
 * Includes detailed information about the identified problem and recommended actions
 * Optimized for display to low-literacy users with icon-based communication
 */
@Parcelize
data class IdentificationResult(
    /**
     * Unique identifier for the result
     */
    val id: String = UUID.randomUUID().toString(),

    /**
     * ID of the crop that was analyzed
     */
    val cropId: String,

    /**
     * Name of the crop for display
     */
    val cropName: String,

    /**
     * URL of the uploaded image
     */
    val imageUrl: String,

    /**SummaryView.kt support**/
     val imagePath: String = "",
    /**
     * Name of the identified pest/disease
     */
    val problemName: String,

    /**
     * Detailed description of the identified problem
     */
    val description: String,

    /**
     * Severity level of the problem (1-3)
     * 1 = Low, 2 = Medium, 3 = High
     */
    val severity: Int,

    /**
     * Confidence level of the identification as a percentage (0-100)
     */
    val confidence: Float,

    /**
     * List of recommended actions to address the problem
     */
    val actions: List<Action>,

    /**
     * Timestamp when the identification was performed
     */
    val timestamp: Date = Date(),

    /**
     * ID of the user who performed the identification
     */
    val userId: String,

    /**
     * Optional type of the problem (e.g., "fungal", "bacterial", "viral", "deficiency")
     * Used for categorization and icon selection
     */
    val problemType: String? = null,

    /**
     * Optional scientific name of the identified pest/disease
     */
    val scientificName: String? = null,

    /**
     * Optional rating provided by user (1-5 stars)
     */
    val userRating: Int? = null,

    /**
     * Optional feedback comments from the user
     */
    val userFeedback: String? = null,

    /**
     * Flag indicating whether the result has been synced to cloud storage
     * Important for offline functionality in rural areas
     */
    val isSyncedToCloud: Boolean = false
) : Parcelable {

    /**
     * Determines if the identification is considered high confidence
     * @return true if confidence is 80% or higher
     */
    fun isHighConfidence(): Boolean = confidence >= 80.0f

    /**
     * Determines if the problem is considered severe
     * @return true if severity is High (3)
     */
    fun isSevereProblem(): Boolean = severity == 3

    /**
     * Gets the key actions that should be taken immediately
     * Filters for high priority actions
     * @return List of high priority actions
     */
    fun getKeyActions(): List<Action> = actions.filter { it.priority >= 3 }

    /**
     * Gets a short summary of the problem
     * Useful for displaying in list views or notifications
     * @return Short summary text
     */
    fun getShortSummary(): String {
        val severityText = when (severity) {
            1 -> "Low"
            2 -> "Medium"
            3 -> "High"
            else -> "Unknown"
        }

        return "$problemName (${severityText.lowercase()} severity)"
    }

    /**
     * Creates a condensed version of this result for sharing or displaying
     * in limited space interfaces
     * @return IdentificationResult with minimal information
     */
    fun toCondensedResult(): IdentificationResult {
        return copy(
            description = description.take(100) + if (description.length > 100) "..." else "",
            actions = actions.take(3),
            scientificName = null,
            userFeedback = null
        )
    }

    companion object {
        /**
         * Creates a placeholder result for UI testing or preview
         * @return Sample IdentificationResult
         */
        fun createPlaceholder(): IdentificationResult {
            return IdentificationResult(
                id = "placeholder_id",
                cropId = "tomato_id",
                cropName = "Tomato",
                imageUrl = "",
                problemName = "Early Blight",
                description = "Early blight is a fungal disease that affects tomato plants. It causes dark spots on leaves with concentric rings, leading to yellowing and drop of affected leaves.",
                severity = 2,
                confidence = 85.5f,
                actions = listOf(
                    Action(
                        actionType = "spray",
                        description = "Apply copper-based fungicide every 7-10 days"
                    ),
                    Action(
                        actionType = "remove",
                        description = "Remove and destroy infected leaves"
                    )
                ),
                userId = "user_id",
                problemType = "fungal"
            )
        }
    }
}