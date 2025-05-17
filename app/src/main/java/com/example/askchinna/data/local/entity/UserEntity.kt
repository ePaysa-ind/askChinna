/**
 * File: app/src/main/java/com/example/askchinna/data/local/entity/UserEntity.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Updated: May 6, 2025
 * Version: 1.2
 * 
 * Change Log:
 * 1.2 - May 6, 2025
 * - Updated to match User model properties
 * - Added isVerified field
 * - Added preferredLanguage field
 * - Added lastLogin field
 * - Renamed id to uid
 * - Renamed name to displayName
 * - Renamed phoneNumber to mobileNumber
 */

package com.example.askchinna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity class representing a user in the local database
 * 
 * @property uid Unique identifier for the user
 * @property displayName User's display name
 * @property mobileNumber User's mobile number in E.164 format
 * @property isVerified Whether the user is verified
 * @property usageCount Number of identifications used
 * @property preferredLanguage User's preferred language
 * @property lastLogin Last time user logged in
 * @property createdAt Date when the user was created
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val displayName: String,
    val mobileNumber: String,
    val isVerified: Boolean = false,
    val usageCount: Int = 0,
    val preferredLanguage: String = "en",
    val lastLogin: Date = Date(),
    val createdAt: Date = Date()
) {
    init {
        require(uid.isNotBlank()) { "User ID cannot be blank" }
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(mobileNumber.matches(Regex("^\\+?[1-9]\\d{1,14}$"))) { "Invalid mobile number format" }
        require(usageCount >= 0) { "Usage count cannot be negative" }
    }

    /**
     * Validates if the user entity has valid data
     * @return true if all required fields are valid
     */
    fun isValid(): Boolean {
        return uid.isNotBlank() &&
               displayName.isNotBlank() &&
               mobileNumber.matches(Regex("^\\+?[1-9]\\d{1,14}$")) &&
               usageCount >= 0 &&
               lastLogin.before(Date()) &&
               createdAt.before(Date())
    }

    /**
     * Checks if the user has reached their usage limit
     * @param maxUses Maximum number of allowed uses
     * @return true if usage count is at or above the limit
     */
    fun hasReachedUsageLimit(maxUses: Int): Boolean {
        return usageCount >= maxUses
    }

    /**
     * Checks if the usage count should be reset based on the last update
     * @param resetPeriodDays Number of days after which usage should reset
     * @return true if usage count should be reset
     */
    fun shouldResetUsage(resetPeriodDays: Int): Boolean {
        val now = Date()
        val diffInMillis = now.time - lastLogin.time
        val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
        return diffInDays >= resetPeriodDays
    }
}