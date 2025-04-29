package com.example.askchinna.data.repository

/**
 * app/src/main/java/com/askchinna/data/repository/UserRepository.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */

package com.askchinna.data.repository

import android.app.Activity
import com.askchinna.data.local.SharedPreferencesManager
import com.askchinna.data.model.UIState
import com.askchinna.data.model.User
import com.askchinna.data.model.UsageLimit
import com.askchinna.data.remote.FirebaseAuthManager
import com.askchinna.data.remote.FirestoreManager
import com.askchinna.data.remote.NetworkExceptionHandler
import com.askchinna.util.SimpleCoroutineUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class managing user authentication and data
 */
@Singleton
class UserRepository @Inject constructor(
    private val authManager: FirebaseAuthManager,
    private val firestoreManager: FirestoreManager,
    private val prefsManager: SharedPreferencesManager,
    private val networkExceptionHandler: NetworkExceptionHandler,
    private val coroutineUtils: SimpleCoroutineUtils
) {
    /**
     * Start the login process by sending OTP
     * @param mobileNumber Mobile number in format "9876543210" (without country code)
     * @param activity Current activity for callbacks
     */
    fun sendOtp(mobileNumber: String, activity: Activity): Flow<UIState<String>> {
        // Format phone number with India country code (+91)
        val formattedPhone = formatPhoneNumber(mobileNumber)
        return authManager.sendOtpToPhone(formattedPhone, activity)
    }

    /**
     * Verify OTP and complete login
     * @param otp OTP received by user
     */
    fun verifyOtp(otp: String): Flow<UIState<User>> = flow {
        emit(UIState.Loading())

        val verifyResult = authManager.verifyOtp(otp)

        if (verifyResult is UIState.Success) {
            // We have authenticated, now get or create user profile
            val user = verifyResult.data
            val userResult = firestoreManager.getOrCreateUser(user)

            // Update last login and usage tracking
            if (userResult is UIState.Success) {
                updateUserLastLogin(userResult.data)
                emit(UIState.Success(userResult.data))
            } else if (userResult is UIState.Error) {
                emit(UIState.Error(userResult.message))
            }
        } else if (verifyResult is UIState.Error) {
            emit(UIState.Error(verifyResult.message))
        }
    }.catch { e ->
        val errorMsg = networkExceptionHandler.handleException(e)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Register a new user with mobile number
     * @param mobileNumber Mobile number in format "9876543210" (without country code)
     * @param displayName Optional display name
     * @param activity Current activity for callbacks
     */
    fun registerUser(
        mobileNumber: String,
        displayName: String,
        activity: Activity
    ): Flow<UIState<String>> {
        // Registration for mobile auth just starts the OTP process
        val formattedPhone = formatPhoneNumber(mobileNumber)

        // Store display name for later use after OTP verification
        prefsManager.saveDisplayName(displayName)

        return authManager.sendOtpToPhone(formattedPhone, activity)
    }

    /**
     * Complete registration after OTP verification
     * @param otp OTP received by user
     */
    fun completeRegistration(otp: String): Flow<UIState<User>> = flow {
        emit(UIState.Loading())

        val verifyResult = authManager.verifyOtp(otp)

        if (verifyResult is UIState.Success) {
            // Create a new user with the stored display name
            val displayName = prefsManager.getDisplayName()
            val user = verifyResult.data.copy(
                displayName = displayName,
                isVerified = true,
                createdAt = Timestamp.now()
            )

            // Create user in Firestore
            val createResult = firestoreManager.createUser(user)

            if (createResult is UIState.Success) {
                // Clear stored display name
                prefsManager.clearDisplayName()
                emit(UIState.Success(createResult.data))
            } else if (createResult is UIState.Error) {
                emit(UIState.Error(createResult.message))
            }
        } else if (verifyResult is UIState.Error) {
            emit(UIState.Error(verifyResult.message))
        }
    }.catch { e ->
        val errorMsg = networkExceptionHandler.handleException(e)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Get current user profile
     */
    fun getCurrentUser(): Flow<UIState<User>> = flow {
        emit(UIState.Loading())

        // Get current user ID
        val userId = authManager.currentUserId

        if (userId != null) {
            // Get user from Firestore
            val userResult = firestoreManager.getUser(userId)
            emit(userResult)
        } else {
            // Try to get from local storage for offline mode
            val localUser = prefsManager.getLocalUser()
            if (localUser.isValid()) {
                emit(UIState.Success(localUser))
            } else {
                emit(UIState.Error("User not authenticated"))
            }
        }
    }.catch { e ->
        val errorMsg = networkExceptionHandler.handleException(e)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Check and update usage limits (5 uses per 30 days)
     */
    fun checkAndUpdateUsageLimit(): Flow<UIState<UsageLimit>> = flow {
        emit(UIState.Loading())

        // Get current user
        val userId = authManager.currentUserId
        if (userId == null) {
            emit(UIState.Error("User not authenticated"))
            return@flow
        }

        // Get user profile to check usage
        val userResult = firestoreManager.getUser(userId)

        if (userResult is UIState.Success) {
            val user = userResult.data
            val now = Timestamp.now()
            var usageCount = user.usageCount
            var resetDate = user.usageResetDate
            var resetNeeded = false

            // Check if reset is needed (30 days passed since last reset)
            if (resetDate != null) {
                val diffMs = now.toDate().time - resetDate.toDate().time
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)

                if (diffDays >= 30) {
                    // Reset usage count
                    usageCount = 0
                    resetDate = now
                    resetNeeded = true
                }
            } else {
                // First time using app - initialize
                resetDate = now
                resetNeeded = true
            }

            // Check if user has reached limit
            val hasReachedLimit = usageCount >= 5

            // If changes needed, update user profile
            if (resetNeeded) {
                val updateResult = firestoreManager.updateUsageTracking(
                    userId, usageCount, resetDate
                )

                if (updateResult is UIState.Error) {
                    emit(UIState.Error(updateResult.message))
                    return@flow
                }
            }

            // Return usage limit info
            val usageLimit = UsageLimit(
                currentCount = usageCount,
                maxCount = 5,
                resetDate = resetDate?.toDate() ?: Date(),
                hasReachedLimit = hasReachedLimit
            )

            emit(UIState.Success(usageLimit))
        } else if (userResult is UIState.Error) {
            emit(UIState.Error(userResult.message))
        }
    }.catch { e ->
        val errorMsg = networkExceptionHandler.handleException(e)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Increment usage count when user performs an identification
     */
    fun incrementUsageCount(): Flow<UIState<Int>> = flow {
        emit(UIState.Loading())

        // Get current user
        val userId = authManager.currentUserId
        if (userId == null) {
            emit(UIState.Error("User not authenticated"))
            return@flow
        }

        // Get current user profile
        val userResult = firestoreManager.getUser(userId)

        if (userResult is UIState.Success) {
            val user = userResult.data
            val newCount = user.usageCount + 1

            // Update usage count in Firestore
            val updateResult = firestoreManager.updateUsageCount(userId, newCount)

            if (updateResult is UIState.Success) {
                emit(UIState.Success(newCount))
            } else if (updateResult is UIState.Error) {
                emit(UIState.Error(updateResult.message))
            }
        } else if (userResult is UIState.Error) {
            emit(UIState.Error(userResult.message))
        }
    }.catch { e ->
        val errorMsg = networkExceptionHandler.handleException(e)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Resend OTP code
     */
    fun resendOtp(mobileNumber: String, activity: Activity): Flow<UIState<String>> {
        val formattedPhone = formatPhoneNumber(mobileNumber)
        return authManager.resendOtp(formattedPhone, activity)
    }

    /**
     * Sign out current user
     */
    fun signOut(): Flow<UIState<Unit>> = flow {
        emit(UIState.Loading())

        authManager.signOut()
        prefsManager.clearAll()

        emit(UIState.Success(Unit))
    }.catch { e ->
        val errorMsg = networkExceptionHandler.handleException(e)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Update user's last login timestamp
     */
    private suspend fun updateUserLastLogin(user: User) {
        val userId = user.uid
        firestoreManager.updateLastLogin(userId)
    }

    /**
     * Format phone number with India country code
     */
    private fun formatPhoneNumber(phone: String): String {
        // Remove any non-digit characters
        val digitsOnly = phone.replace(Regex("\\D"), "")

        // If user entered with country code, make sure it's formatted correctly
        return if (digitsOnly.startsWith("91") && digitsOnly.length >= 12) {
            "+$digitsOnly"
        } else {
            // Add India country code (+91)
            "+91$digitsOnly"
        }
    }
}