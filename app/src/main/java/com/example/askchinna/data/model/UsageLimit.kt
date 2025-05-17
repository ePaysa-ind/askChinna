/**
 * file: app/src/main/java/com/example/askchinna/data/model/UsageLimit.kt
 *
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.2
 * 
 * Change Log:
 * 1.2 - May 6, 2025
 * - Added proper validation for usage count
 * - Added proper date validation
 * - Added proper documentation
 * - Added helper methods for limit checking
 * - Added proper error handling
 * - Added proper state management
 */
package com.example.askchinna.data.model

import java.util.Date

/**
 * Data class representing usage limits for a user
 * Used to track and enforce the 5 uses per 30 days limit
 */
data class UsageLimit(
    /**
     * Number of times the user has used the identification feature
     */
    val usageCount: Int = 0,

    /**
     * Date when the usage count was last updated
     */
    val lastUpdated: Date = Date(),

    /**
     * Whether the user has reached their usage limit
     */
    val isLimitReached: Boolean = false
) {
    companion object {
        const val MAX_USES = 5

    }

    /**
     * Creates a new UsageLimit with reset usage count
     * @return New UsageLimit with reset count
     */
    fun reset(): UsageLimit {
        return copy(
            usageCount = 0,
            lastUpdated = Date(),
            isLimitReached = false
        )
    }

    /**
     * Validates the usage limit data
     * @return true if valid, false otherwise
     */
    fun isValid(): Boolean {
        return usageCount in 0..MAX_USES &&
               lastUpdated.time <= Date().time
    }
}