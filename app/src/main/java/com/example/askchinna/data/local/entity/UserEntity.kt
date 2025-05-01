/**
 * File: app/src/main/java/com/example/askchinna/data/local/entity/UserEntity.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Version: 1.0
 */

package com.example.askchinna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phoneNumber: String,
    val registrationDate: Date,
    val usageCount: Int = 0,
    val lastUsageUpdate: Date = Date()
)