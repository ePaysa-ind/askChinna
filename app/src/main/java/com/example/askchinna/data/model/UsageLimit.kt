package com.example.askchinna.data.model

/**
 * app/src/main/java/com/askchinna/data/model/UsageLimit.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


/**
 * Data class representing usage limits for a user
 *
 * @property currentCount Current number of identifications used in period
 * @property remainingCount Remaining number of identifications allowed
 * @property isLimitReached Whether user has reached their usage limit
 * @property role User role (free/tester/premium)
 */
data class UsageLimit(
    val currentCount: Int,
    val remainingCount: Int,
    val isLimitReached: Boolean,
    val role: String
)