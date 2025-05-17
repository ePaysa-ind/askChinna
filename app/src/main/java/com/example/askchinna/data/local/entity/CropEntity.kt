/**
 * File: app/src/main/java/com/example/askchinna/data/local/entity/CropEntity.kt
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
 * - Added proper state management
 * - Added proper resource validation
 */

package com.example.askchinna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a crop in the local database
 * 
 * @property id Unique identifier for the crop
 * @property name Display name of the crop
 * @property scientificName Scientific name of the crop
 * @property iconResName Resource name for the crop's icon
 */
@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val scientificName: String,
    val iconResName: String
) {
    init {
        require(id.isNotBlank()) { "Crop ID cannot be blank" }
        require(name.isNotBlank()) { "Crop name cannot be blank" }
        require(scientificName.isNotBlank()) { "Scientific name cannot be blank" }
        require(iconResName.isNotBlank()) { "Icon resource name cannot be blank" }
        require(iconResName.matches(Regex("^[a-z][a-z0-9_]*$"))) { "Invalid icon resource name format" }
    }

    /**
     * Validates if the crop entity has valid data
     * @return true if all required fields are valid
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               name.isNotBlank() &&
               scientificName.isNotBlank() &&
               iconResName.isNotBlank() &&
               iconResName.matches(Regex("^[a-z][a-z0-9_]*$"))
    }

    /**
     * Checks if this is one of the supported crops
     * @return true if this is a supported crop
     */
    fun isSupportedCrop(): Boolean {
        return SUPPORTED_CROP_IDS.contains(id)
    }

    companion object {
        /**
         * List of supported crop IDs
         */
        val SUPPORTED_CROP_IDS = setOf(
            "chili",
            "okra",
            "maize",
            "cotton",
            "tomato",
            "watermelon",
            "soybean",
            "rice",
            "wheat",
            "pigeon_pea"
        )
    }
}
