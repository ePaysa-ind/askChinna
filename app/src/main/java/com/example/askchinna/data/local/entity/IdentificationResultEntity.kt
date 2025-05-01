/**
 * File: app/src/main/java/com/example/askchinna/data/local/entity/IdentificationResultEntity.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Version: 1.0
 */

package com.example.askchinna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

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
)