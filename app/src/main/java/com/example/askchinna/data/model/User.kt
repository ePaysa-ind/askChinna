/**
 * file path: app/src/main/java/com/example/askchinna/data/model/User.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.1
 */

package com.example.askchinna.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName

/**
 * Represents a user in the application.
 * Contains user information and preferences.
 */
data class User(
    /**
     * Unique identifier for the user.
     */
    @SerializedName("uid")
    val uid: String,

    /**
     * User's mobile number.
     */
    @SerializedName("mobileNumber")
    val mobileNumber: String,

    /**
     * User's display name.
     */
    @SerializedName("displayName")
    val displayName: String,

    /**
     * User's verification status.
     */
    @SerializedName("isVerified")
    val isVerified: Boolean = false,

    /**
     * User's usage count.
     */
    @SerializedName("usageCount")
    val usageCount: Int = 0,

    /**
     * User's preferred language.
     */
    @SerializedName("preferredLanguage")
    val preferredLanguage: String = "en",

    /**
     * User's last login timestamp.
     */
    @SerializedName("lastLogin")
    val lastLogin: Long = System.currentTimeMillis(),

    /**
     * User's account creation timestamp.
     */
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis()
) {
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
            "uid" to uid,
            "mobileNumber" to mobileNumber,
            "displayName" to displayName,
            "isVerified" to isVerified,
            "usageCount" to usageCount,
            "preferredLanguage" to preferredLanguage,
            "lastLogin" to lastLogin,
            "createdAt" to createdAt
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
                isVerified = data["isVerified"] as? Boolean ?: false,
                usageCount = (data["usageCount"] as? Long)?.toInt() ?: 0,
                preferredLanguage = data["preferredLanguage"] as? String ?: "en",
                lastLogin = (data["lastLogin"] as? Timestamp)?.seconds ?: System.currentTimeMillis(),
                createdAt = (data["createdAt"] as? Timestamp)?.seconds ?: System.currentTimeMillis()
            )
        }
    }
}