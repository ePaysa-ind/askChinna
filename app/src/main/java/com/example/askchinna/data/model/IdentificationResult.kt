/**
 * File: app/src/main/java/com/example/askchinna/data/model/IdentificationResult.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.1
 */

package com.example.askchinna.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import com.google.gson.annotations.SerializedName

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
    @SerializedName("id")
    val id: String,

    /**
     * ID of the crop that was analyzed
     */
    @SerializedName("cropId")
    val cropId: String,

    /**
     * Name of the crop for display
     */
    @SerializedName("cropName")
    val cropName: String,

    /**
     * URL of the uploaded image
     */
    @SerializedName("imageUrl")
    val imageUrl: String,

    /**SummaryView.kt support**/
    @SerializedName("imagePath")
    val imagePath: String = "",

    /**
     * Name of the identified pest/disease
     */
    @SerializedName("problemName")
    val problemName: String,

    /**
     * Detailed description of the identified problem
     */
    @SerializedName("description")
    val description: String,

    /**
     * Severity level of the problem (1-3)
     * 1 = Low, 2 = Medium, 3 = High
     */
    @SerializedName("severity")
    val severity: Int,

    /**
     * Confidence level of the identification as a percentage (0-100)
     */
    @SerializedName("confidence")
    val confidence: Float,

    /**
     * List of recommended actions to address the problem
     */
    @SerializedName("actions")
    val actions: List<Action>,

    /**
     * Timestamp when the identification was performed
     */
    @SerializedName("timestamp")
    val timestamp: Date = Date(),

    /**
     * ID of the user who performed the identification
     */
    @SerializedName("userId")
    val userId: String,

    /**
     * Optional type of the problem (e.g., "fungal", "bacterial", "viral", "deficiency")
     * Used for categorization and icon selection
     */
    @SerializedName("problemType")
    val problemType: String? = null,

    /**
     * Optional scientific name of the identified pest/disease
     */
    @SerializedName("scientificName")
    val scientificName: String? = null,

    /**
     * Optional rating provided by user (1-5 stars)
     */
    @SerializedName("userRating")
    val userRating: Int? = null,

    /**
     * Optional feedback comments from the user
     */
    @SerializedName("userFeedback")
    val userFeedback: String? = null,

    /**
     * Flag indicating whether the result has been synced to cloud storage
     * Important for offline functionality in rural areas
     */
    @SerializedName("isSyncedToCloud")
    val isSyncedToCloud: Boolean = false
) : Parcelable {

    companion object
}

