/**
 * File: app/src/main/java/com/example/askchinna/data/local/entity/IdentificationResultEntity.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Updated: May 6, 2025
 * Version: 1.1
 *
 * Change Log:
 * 1.1 - May 6, 2025
 * - Added input validation
 * - Enhanced documentation with KDoc
 * - Added data validation methods
 * - Added proper null safety
 * - Added proper date handling
 * - Added proper state management
 * - Added proper list validation
 */

package com.example.askchinna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity class representing an identification result in the local database
 *
 * @property id Unique identifier for the result
 * @property userId ID of the user who made the identification
 * @property cropId ID of the identified crop
 * @property cropName Name of the identified crop
 * @property imageUrl URL of the analyzed image
 * @property problemName Name of the identified problem
 * @property description Detailed description of the problem
 * @property severity Severity level (1-3)
 * @property confidence Confidence score (0-100)
 * @property actionsList List of recommended actions
 * @property timestamp When the identification was made
 * @property feedbackRating User's rating of the result (1-5)
 * @property feedbackComments User's comments about the result
 * @property isSyncedToCloud Whether the result is synced to cloud
 */
@Entity(tableName = "identification_results")
data class IdentificationResultEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val cropId: String,
    val cropName: String,
    val imageUrl: String,
    val problemName: String,
    val description: String,
    val severity: Int,
    val confidence: Float,
    val actionsList: List<String>,
    val timestamp: Date,
    val feedbackRating: Int? = null,
    val feedbackComments: String? = null,
    val isSyncedToCloud: Boolean = false
) {
    init {
        require(id.isNotBlank()) { "Result ID cannot be blank" }
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(cropId.isNotBlank()) { "Crop ID cannot be blank" }
        require(cropName.isNotBlank()) { "Crop name cannot be blank" }
        require(imageUrl.isNotBlank()) { "Image URL cannot be blank" }
        require(problemName.isNotBlank()) { "Problem name cannot be blank" }
        require(description.isNotBlank()) { "Description cannot be blank" }
        require(severity in 1..3) { "Severity must be between 1 and 3" }
        require(confidence in 0f..100f) { "Confidence must be between 0 and 100" }
        require(actionsList.isNotEmpty()) { "Actions list cannot be empty" }
        require(feedbackRating == null || feedbackRating in 1..5) { "Feedback rating must be between 1 and 5" }
    }

    /**
     * Validates if the identification result has valid data
     * @return true if all required fields are valid
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
                userId.isNotBlank() &&
                cropId.isNotBlank() &&
                cropName.isNotBlank() &&
                imageUrl.isNotBlank() &&
                problemName.isNotBlank() &&
                description.isNotBlank() &&
                severity in 1..3 &&
                confidence in 0f..100f &&
                actionsList.isNotEmpty() &&
                (feedbackRating == null || feedbackRating in 1..5) &&
                timestamp.before(Date())
    }

    // The explicit getter methods have been removed to avoid platform declaration clash
    // Kotlin automatically generates getters for all properties, making these redundant
}