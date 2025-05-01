/**
 * File: app/src/main/java/com/example/askchinna/data/local/entity/CropEntity.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Version: 1.0
 */

package com.example.askchinna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val scientificName: String,
    val iconResName: String
)
