package com.example.askchinna.data.model

/**
 * app/src/main/java/com/askchinna/data/model/User.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a user in the application.
 * Uses mobile number as primary identifier instead of email.
 */
data class User(
    @DocumentId
    val uid: String = "",

    // Mobile number with country code (India +91)
    val mobileNumber: String = "",

    // User's display name (optional)
    val displayName: String = "",

    // Track registration date
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    // Track last login date
    @ServerTimestamp
    val lastLogin: Timestamp? = null,

    // Track usage for limits (5 uses per 30 days)
    val usageCount: Int = 0,

    // When usage count was last reset
    val usageResetDate: Timestamp? = null,

    // Whether user account is verified
    val isVerified: Boolean = false,

    // Which language user prefers (for minimal text UI)
    val preferredLanguage: String = "en",

    // For offline authentication
    val authToken: String = ""
) {
    /**
     * Check if user has reached usage limit (5 uses per 30 days)
     */
    fun hasReachedUsageLimit(): Boolean {
        return usageCount >= 5
    }

    /**
     * Checks if this is a valid user object with necessary data
     */
    fun isValid(): Boolean {
        return uid.isNotEmpty() && mobileNumber.isNotEmpty()
    }

    /**
     * Create a map representation for Firestore storage
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "mobileNumber" to mobileNumber,
            "displayName" to displayName,
            "createdAt" to createdAt,
            "lastLogin" to lastLogin,
            "usageCount" to usageCount,
            "usageResetDate" to usageResetDate,
            "isVerified" to isVerified,
            "preferredLanguage" to preferredLanguage
            // Note: authToken is not stored in Firestore for security
        )
    }

    companion object {
        /**
         * Create User object from Firestore data map
         */
        fun fromMap(uid: String, data: Map<String, Any?>): User {
            return User(
                uid = uid,
                mobileNumber = data["mobileNumber"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                createdAt = data["createdAt"] as? Timestamp,
                lastLogin = data["lastLogin"] as? Timestamp,
                usageCount = (data["usageCount"] as? Long)?.toInt() ?: 0,
                usageResetDate = data["usageResetDate"] as? Timestamp,
                isVerified = data["isVerified"] as? Boolean ?: false,
                preferredLanguage = data["preferredLanguage"] as? String ?: "en"
            )
        }
    }
}