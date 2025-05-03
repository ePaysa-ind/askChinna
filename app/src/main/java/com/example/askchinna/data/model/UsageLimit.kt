package com.example.askchinna.data.model

/**
 * file: app/src/main/java/com/example/askchinna/data/model/UsageLimit.kt
 *
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 1, 2025
 * Version: 1.1
 */
import java.util.Date

/**
 * Data class representing usage limits for a user
 * Used to track and enforce the 5 uses per 30 days limit
 */
data class UsageLimit(
    val usageCount: Int,
    val lastUpdated: Date,
    val isLimitReached: Boolean
)