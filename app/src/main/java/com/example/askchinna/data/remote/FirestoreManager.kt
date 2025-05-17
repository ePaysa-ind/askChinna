/**
 * File: app/src/main/java/com/example/askchinna/data/remote/FirestoreManager.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.3
 */
package com.example.askchinna.data.remote

import android.util.Log
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.User
import com.example.askchinna.util.NetworkExceptionHandler
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirestoreManager"

/**
 * Manager class for Firestore operations related to user data
 */
@Singleton
class FirestoreManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val prefsManager: SharedPreferencesManager,
    private val networkExceptionHandler: NetworkExceptionHandler
) {
    companion object {
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Get user by ID from Firestore with offline support
     * @param userId Firebase user ID
     * @param source Data source (SERVER, CACHE, or DEFAULT)
     * @return UIState with user data or error
     */
    suspend fun getUser(userId: String, source: Source = Source.DEFAULT): UIState<User> {
        if (userId.isBlank()) {
            return UIState.Error("Invalid user ID")
        }

        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get(source)
                .await()

            if (document.exists()) {
                val userData = document.data
                if (userData != null) {
                    val user = User.fromMap(userId, userData)

                    // Save user locally for offline use
                    prefsManager.saveUser(user)

                    UIState.Success(user)
                } else {
                    Log.e(TAG, "User data is null for ID: $userId")
                    UIState.Error("User data is null")
                }
            } else {
                Log.w(TAG, "User document does not exist for ID: $userId")
                UIState.Error("User document does not exist")
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error getting user: $userId", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user: $userId", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        }
    }

    /**
     * Get existing user or create a new one if not found
     * @param user Basic user information
     * @return UIState with user data or error
     */
    suspend fun getOrCreateUser(user: User): UIState<User> {
        if (!user.isValid()) {
            return UIState.Error("Invalid user data")
        }

        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .get()
                .await()

            if (document.exists()) {
                // User exists, get data
                val userData = document.data
                if (userData != null) {
                    val existingUser = User.fromMap(user.uid, userData)

                    // Save user locally for offline use
                    prefsManager.saveUser(existingUser)

                    UIState.Success(existingUser)
                } else {
                    Log.e(TAG, "User data is null for ID: ${user.uid}")
                    UIState.Error("User data is null")
                }
            } else {
                // User doesn't exist, create new
                val newUser = user.copy(
                    createdAt = System.currentTimeMillis(),
                    lastLogin = System.currentTimeMillis(),
                    usageCount = 0,
                    isVerified = true,
                    preferredLanguage = "en"
                )

                // Create user in Firestore
                createUser(newUser)
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error getting/creating user: ${user.uid}", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting/creating user: ${user.uid}", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        }
    }

    /**
     * Create a new user in Firestore with validation
     * @param user User to create
     * @return UIState with created user or error
     */
    suspend fun createUser(user: User): UIState<User> {
        if (!user.isValid()) {
            return UIState.Error("Invalid user data")
        }

        return try {
            // Create map of user data for Firestore
            val userData = user.toMap()

            // Save user to Firestore
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(userData)
                .await()

            // Save user locally for offline use
            prefsManager.saveUser(user)

            UIState.Success(user)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error creating user: ${user.uid}", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user: ${user.uid}", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        }
    }

    /**
     * Update user's last login timestamp
     * @param userId User ID to update
     * @return UIState with success or error
     */
    suspend fun updateLastLogin(userId: String): UIState<Unit> {
        if (userId.isBlank()) {
            return UIState.Error("Invalid user ID")
        }

        return try {
            val updates = mapOf(
                "lastLogin" to Timestamp.now()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(updates, SetOptions.merge())
                .await()

            UIState.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error updating last login: $userId", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last login: $userId", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        }
    }

    /**
     * Update user's usage count with validation
     * @param userId User ID to update
     * @param usageCount New usage count
     * @return UIState with updated count or error
     */
    suspend fun updateUsageCount(userId: String, usageCount: Int): UIState<Int> {
        if (userId.isBlank()) {
            return UIState.Error("Invalid user ID")
        }

        if (usageCount < 0) {
            return UIState.Error("Invalid usage count")
        }

        return try {
            val updates = mapOf(
                "usageCount" to usageCount
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(updates, SetOptions.merge())
                .await()

            // Update local user data
            val localUser = prefsManager.getUser()
            if (localUser != null) {
                prefsManager.saveUser(localUser.copy(usageCount = usageCount))
            }

            UIState.Success(usageCount)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error updating usage count: $userId", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating usage count: $userId", e)
            val error = networkExceptionHandler.handle(e)
            UIState.Error(error)
        }
    }
}