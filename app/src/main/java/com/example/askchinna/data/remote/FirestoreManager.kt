package com.example.askchinna.data.remote
/**
 * app/src/main/java/com/askchinna/data/remote/FirestoreManager.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */

import com.askchinna.data.local.SharedPreferencesManager
import com.askchinna.data.model.UIState
import com.askchinna.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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
        private const val IDENTIFICATION_HISTORY_COLLECTION = "identification_history"
    }

    /**
     * Get user by ID from Firestore
     * @param userId Firebase user ID
     */
    suspend fun getUser(userId: String): UIState<User> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val userData = document.data
                if (userData != null) {
                    val user = User.fromMap(userId, userData)

                    // Save user locally for offline use
                    prefsManager.saveLocalUser(user)

                    UIState.Success(user)
                } else {
                    UIState.Error("User data is null")
                }
            } else {
                UIState.Error("User document does not exist")
            }
        } catch (e: FirebaseFirestoreException) {
            val error = networkExceptionHandler.handleFirestoreException(e)
            UIState.Error(error)
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleException(e)
            UIState.Error(error)
        }
    }

    /**
     * Get existing user or create a new one if not found
     * @param user Basic user information
     */
    suspend fun getOrCreateUser(user: User): UIState<User> {
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
                    prefsManager.saveLocalUser(existingUser)

                    UIState.Success(existingUser)
                } else {
                    UIState.Error("User data is null")
                }
            } else {
                // User doesn't exist, create new
                val newUser = user.copy(
                    createdAt = Timestamp.now(),
                    lastLogin = Timestamp.now(),
                    usageCount = 0,
                    usageResetDate = Timestamp.now()
                )

                // Create user in Firestore
                createUser(newUser)
            }
        } catch (e: FirebaseFirestoreException) {
            val error = networkExceptionHandler.handleFirestoreException(e)
            UIState.Error(error)
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleException(e)
            UIState.Error(error)
        }
    }

    /**
     * Create a new user in Firestore
     * @param user User to create
     */
    suspend fun createUser(user: User): UIState<User> {
        return try {
            // Create map of user data for Firestore
            val userData = user.toMap()

            // Save user to Firestore
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(userData)
                .await()

            // Save user locally for offline use
            prefsManager.saveLocalUser(user)

            UIState.Success(user)
        } catch (e: FirebaseFirestoreException) {
            val error = networkExceptionHandler.handleFirestoreException(e)
            UIState.Error(error)
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleException(e)
            UIState.Error(error)
        }
    }

    /**
     * Update user's last login timestamp
     * @param userId User ID to update
     */
    suspend fun updateLastLogin(userId: String): UIState<Unit> {
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
            val error = networkExceptionHandler.handleFirestoreException(e)
            UIState.Error(error)
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleException(e)
            UIState.Error(error)
        }
    }

    /**
     * Update user's usage count
     * @param userId User ID to update
     * @param usageCount New usage count
     */
    suspend fun updateUsageCount(userId: String, usageCount: Int): UIState<Int> {
        return try {
            val updates = mapOf(
                "usageCount" to usageCount
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(updates, SetOptions.merge())
                .await()

            // Update local user data
            val localUser = prefsManager.getLocalUser()
            if (localUser.isValid()) {
                prefsManager.saveLocalUser(localUser.copy(usageCount = usageCount))
            }

            UIState.Success(usageCount)
        } catch (e: FirebaseFirestoreException) {
            val error = networkExceptionHandler.handleFirestoreException(e)
            UIState.Error(error)
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleException(e)
            UIState.Error(error)
        }
    }

    /**
     * Update usage tracking information
     * @param userId User ID to update
     * @param usageCount Current usage count
     * @param resetDate Date when count was last reset
     */
    suspend fun updateUsageTracking(
        userId: String,
        usageCount: Int,
        resetDate: Timestamp
    ): UIState<Unit> {
        return try {
            val updates = mapOf(
                "usageCount" to usageCount,
                "usageResetDate" to resetDate
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(updates, SetOptions.merge())
                .await()

            // Update local user data
            val localUser = prefsManager.getLocalUser()
            if (localUser.isValid()) {
                prefsManager.saveLocalUser(localUser.copy(
                    usageCount = usageCount,
                    usageResetDate = resetDate
                ))
            }

            UIState.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            val error = networkExceptionHandler.handleFirestoreException(e)
            UIState.Error(error)
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleException(e)
            UIState.Error(error)
        }
    }

    /**
     * Find user by mobile number
     * @param mobileNumber Mobile number to search for
     */
    suspend fun findUserByMobile(mobileNumber: String): UIState<User?> {
        return try {
            val queryResult = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("mobileNumber", mobileNumber)
                .limit(1)
                .get()
                .await()

            if (!queryResult.isEmpty) {
                val document = queryResult.documents[0]
                val userId = document.id
                val userData = document.data

                if (userData != null) {
                    val user = User.fromMap(userId, userData)
                    UIState.Success(user)
                } else {
                    UIState.Success(null)
                }
            } else {
                UIState.Success(null)
            }
        } catch (e: FirebaseFirestoreException) {
            val error = networkExceptionHandler.handleFirestoreException(e)
            UIState.Error(error)
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleException(e)
            UIState.Error(error)
        }
    }

    /**
     * Update user's display name
     * @param userId User ID to update
     * @param displayName New display name
     */
    suspend fun updateDisplayName(userId: String, displayName: String): UIState<Unit> {
        return try {
            val updates = mapOf(
                "displayName" to displayName
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(updates, SetOptions.merge())
                .await()

            // Update local user data
            val localUser = prefsManager.getLocalUser()
            if (localUser.isValid()) {
                prefsManager.saveLocalUser(localUser.copy(displayName = displayName))
            }

            UIState.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            val error = networkExceptionHandler.handleFirestoreException(e)
            UIState.Error(error)
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleException(e)
            UIState.Error(error)
        }
    }
}