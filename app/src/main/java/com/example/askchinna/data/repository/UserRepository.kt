/**
 * file path: app/src/main/java/com/example/askchinna/data/repository/UserRepository.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 15, 2025
 * Version: 1.6
 *
 * Change Log:
 * 1.6 - May 15, 2025
 * - Added enhanced logging for debugging purposes
 * - Fixed phone number formatting and validation
 * - Added special handling for test phone numbers
 *
 * 1.5 - May 6, 2025
 * - Updated to match User model properties
 * - Updated user creation and update methods
 * - Updated error handling for new properties
 * - Updated documentation
 */
package com.example.askchinna.data.repository

import android.app.Activity
import android.util.Log
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.User
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.remote.FirebaseAuthManager
import com.example.askchinna.data.remote.FirestoreManager
import com.example.askchinna.util.NetworkExceptionHandler
import com.example.askchinna.util.SimpleCoroutineUtils
import kotlinx.coroutines.CoroutineExceptionHandler
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
 * Handles user registration, login, and usage tracking
 */
@Singleton
class UserRepository @Inject constructor(
    private val authManager: FirebaseAuthManager,
    private val firestoreManager: FirestoreManager,
    private val prefsManager: SharedPreferencesManager,
    private val networkExceptionHandler: NetworkExceptionHandler,
    private val coroutineUtils: SimpleCoroutineUtils
) {
    companion object {
        private const val TAG = "UserRepository"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY_MS = 1000L
        private const val MAX_RETRY_DELAY_MS = 8000L
    }

    private var isInitialized = false

    // Not used - this property is just for demonstration and should be removed if not needed
    @Suppress("unused")
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error: ${throwable.message}", throwable)
    }

    init {
        try {
            // Verify dependencies are initialized
            if (authManager.currentUserId != null || prefsManager.getUser() != null) {
                isInitialized = true
                Log.d(TAG, "UserRepository initialized successfully")
            } else {
                Log.d(TAG, "UserRepository initialized but no current user found")
                isInitialized = true  // Treat as initialized even without a user
            }
        } catch (e: Throwable) {
            handleError("Failed to initialize UserRepository", e)
        }
    }

    /**
     * Increment usage count when user performs an identification
     * Returns a Flow that emits a Unit when the operation is complete
     */
    fun incrementUsageCount(): Flow<Unit> = flow {
        try {
            if (!isInitialized) {
                Log.w(TAG, "UserRepository not initialized, skipping incrementUsageCount")
                emit(Unit)
                return@flow
            }

            // Get current user
            val userId = authManager.currentUserId
            if (userId == null) {
                Log.w(TAG, "No current user found, skipping incrementUsageCount")
                emit(Unit)
                return@flow
            }

            var attempts = 0
            var delayMs = INITIAL_RETRY_DELAY_MS

            while (attempts < MAX_RETRY_ATTEMPTS) {
                try {
                    // Get current user profile
                    when (val userResult = firestoreManager.getUser(userId)) {
                        is UIState.Success -> {
                            val user = userResult.data
                            val newCount = user.usageCount + 1

                            // Update usage count in Firestore
                            firestoreManager.updateUsageCount(userId, newCount)
                            emit(Unit)
                            return@flow
                        }
                        is UIState.Error -> {
                            attempts++
                            if (attempts == MAX_RETRY_ATTEMPTS) {
                                handleError("Error getting user: ${userResult.message}", Exception(userResult.message))
                                emit(Unit)
                                return@flow
                            }
                            delayMs = (delayMs * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
                            kotlinx.coroutines.delay(delayMs)
                        }
                        else -> {
                            emit(Unit)
                            return@flow
                        }
                    }
                } catch (e: Throwable) {
                    attempts++
                    if (attempts == MAX_RETRY_ATTEMPTS) {
                        handleError("Error updating usage count", e)
                        emit(Unit)
                        return@flow
                    }
                    delayMs = (delayMs * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
                    kotlinx.coroutines.delay(delayMs)
                }
            }
            emit(Unit)
        } catch (e: Throwable) {
            handleError("Error in incrementUsageCount", e)
            emit(Unit)
        }
    }.catch { e ->
        handleError("Flow error in incrementUsageCount", e)
        emit(Unit)
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Start the login process by sending OTP
     * @param mobileNumber Mobile number in format "+919876543210" (with country code)
     * @param activity Current activity for callbacks
     */
    fun sendOtp(mobileNumber: String, activity: Activity): Flow<UIState<String>> {
        return try {
            Log.d(TAG, "Sending OTP to: $mobileNumber")

            // Initialize repository if needed
            if (!isInitialized) {
                isInitialized = true
                Log.d(TAG, "Initializing repository during sendOtp")
            }

            // Special handling for test phone numbers (for debugging)
            if (mobileNumber == "+917780350915" ||
                mobileNumber == "+919866679227" ||
                mobileNumber == "+917993754064") {
                Log.d(TAG, "Test phone number detected: $mobileNumber")
            }

            // Ensure phone number is properly formatted
            val formattedPhone = formatPhoneNumber(mobileNumber)
            Log.d(TAG, "Formatted phone: $formattedPhone")

            // Call Firebase Auth Manager to send OTP
            authManager.sendOtpToPhone(formattedPhone, activity)
        } catch (e: Throwable) {
            Log.e(TAG, "Error sending OTP", e)
            handleError("Error sending OTP", e)
            flow { emit(UIState.Error("Failed to send OTP: ${e.message ?: "Unknown error"}")) }
        }
    }

    /**
     * Verify OTP and complete login
     * @param otp OTP received by user
     */
    fun verifyOtp(otp: String): Flow<UIState<User>> = flow {
        try {
            Log.d(TAG, "Verifying OTP: $otp")

            if (!isInitialized) {
                isInitialized = true
                Log.d(TAG, "Initializing repository during verifyOtp")
            }

            emit(UIState.Loading())

            var attempts = 0
            var delayMs = INITIAL_RETRY_DELAY_MS

            while (attempts < MAX_RETRY_ATTEMPTS) {
                try {
                    // Important: verifyOtp returns UIState<User> directly, not a Flow
                    Log.d(TAG, "Attempt ${attempts+1} to verify OTP")
                    when (val verifyResult = authManager.verifyOtp(otp)) {
                        is UIState.Success -> {
                            // We have authenticated, now get or create user profile
                            val user = verifyResult.data
                            Log.d(TAG, "OTP verified successfully for user: ${user.uid}")

                            when (val userResult = firestoreManager.getOrCreateUser(user)) {
                                is UIState.Success -> {
                                    // Update last login with a helper method
                                    val resultUser = userResult.data
                                    Log.d(TAG, "User profile retrieved/created: ${resultUser.uid}")
                                    updateLastLogin(resultUser.uid)
                                    emit(UIState.Success(resultUser))
                                    return@flow
                                }
                                is UIState.Error -> {
                                    Log.e(TAG, "Error getting/creating user: ${userResult.message}")
                                    attempts++
                                    if (attempts == MAX_RETRY_ATTEMPTS) {
                                        emit(UIState.Error(userResult.message))
                                        return@flow
                                    }
                                    delayMs = (delayMs * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
                                    kotlinx.coroutines.delay(delayMs)
                                }
                                else -> {
                                    // Loading state, no action needed
                                }
                            }
                        }
                        is UIState.Error -> {
                            Log.e(TAG, "Error verifying OTP: ${verifyResult.message}")
                            attempts++
                            if (attempts == MAX_RETRY_ATTEMPTS) {
                                emit(UIState.Error(verifyResult.message))
                                return@flow
                            }
                            delayMs = (delayMs * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
                            kotlinx.coroutines.delay(delayMs)
                        }
                        else -> {
                            // Loading state, no action needed
                        }
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "Exception during OTP verification", e)
                    attempts++
                    if (attempts == MAX_RETRY_ATTEMPTS) {
                        handleError("Error verifying OTP", e)
                        emit(UIState.Error("Failed to verify OTP: ${e.message ?: "Unknown error"}"))
                        return@flow
                    }
                    delayMs = (delayMs * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in verifyOtp flow", e)
            handleError("Error in verifyOtp", e)
            emit(UIState.Error("Failed to verify OTP: ${e.message ?: "Unknown error"}"))
        }
    }.catch { e ->
        Log.e(TAG, "Caught exception in verifyOtp flow", e)
        handleError("Flow error in verifyOtp", e)
        val errorMsg = networkExceptionHandler.handle(e as Exception)
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
        return try {
            Log.d(TAG, "Registering user with mobile: $mobileNumber, name: $displayName")

            if (!isInitialized) {
                isInitialized = true
                Log.d(TAG, "Initializing repository during registerUser")
            }

            // Registration for mobile auth just starts the OTP process
            val formattedPhone = formatPhoneNumber(mobileNumber)

            // Store display name for later use after OTP verification
            try {
                prefsManager.saveDisplayName(displayName)
                Log.d(TAG, "Saved display name: $displayName")
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to save display name: ${e.message}")
                // Continue even if storing display name fails
            }

            authManager.sendOtpToPhone(formattedPhone, activity)
        } catch (e: Throwable) {
            Log.e(TAG, "Error registering user", e)
            handleError("Error registering user", e)
            flow { emit(UIState.Error("Failed to register user: ${e.message ?: "Unknown error"}")) }
        }
    }

    /**
     * Complete registration after OTP verification
     * @param otp OTP received by user
     */
    fun completeRegistration(otp: String): Flow<UIState<User>> = flow {
        try {
            Log.d(TAG, "Completing registration with OTP: $otp")

            if (!isInitialized) {
                isInitialized = true
                Log.d(TAG, "Initializing repository during completeRegistration")
            }

            emit(UIState.Loading())

            // Important: verifyOtp returns UIState<User> directly, not a Flow
            when (val verifyResult = authManager.verifyOtp(otp)) {
                is UIState.Success -> {
                    // Create a new user with the stored display name
                    val displayName = prefsManager.getDisplayName()
                    Log.d(TAG, "OTP verified successfully. Creating user with name: $displayName")

                    // Create user with current time in milliseconds
                    val user = verifyResult.data.copy(
                        displayName = displayName,
                        isVerified = true,
                        createdAt = System.currentTimeMillis()
                    )

                    // Create user in Firestore
                    when (val createResult = firestoreManager.createUser(user)) {
                        is UIState.Success -> {
                            // Clear saved display name
                            prefsManager.clearDisplayName()
                            Log.d(TAG, "User created successfully: ${user.uid}")
                            emit(UIState.Success(createResult.data))
                        }
                        is UIState.Error -> {
                            Log.e(TAG, "Error creating user: ${createResult.message}")
                            emit(UIState.Error(createResult.message))
                        }
                        is UIState.Loading -> {
                            // Loading state, no action needed
                        }
                        else -> {
                            // Handle other states if needed
                        }
                    }
                }
                is UIState.Error -> {
                    Log.e(TAG, "Error verifying OTP during registration: ${verifyResult.message}")
                    emit(UIState.Error(verifyResult.message))
                }
                is UIState.Loading -> {
                    // Loading state, no action needed
                }
                else -> {
                    // Handle other states if needed
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error completing registration", e)
            handleError("Error completing registration", e)
            emit(UIState.Error("Failed to complete registration: ${e.message ?: "Unknown error"}"))
        }
    }.catch { e ->
        Log.e(TAG, "Caught exception in completeRegistration flow", e)
        handleError("Flow error in completeRegistration", e)
        val errorMsg = networkExceptionHandler.handle(e as Exception)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Get current user profile
     */
    fun getCurrentUser(): Flow<UIState<User>> = flow {
        try {
            Log.d(TAG, "Getting current user profile")

            if (!isInitialized) {
                Log.e(TAG, "UserRepository not initialized")
                throw IllegalStateException("UserRepository not initialized")
            }

            emit(UIState.Loading())

            // Get current user ID
            val userId = authManager.currentUserId
            Log.d(TAG, "Current user ID: $userId")

            if (userId != null) {
                // Get user from Firestore
                val userResult = firestoreManager.getUser(userId)
                Log.d(TAG, "Firestore user result: $userResult")
                emit(userResult)
            } else {
                // Try to get from local storage for offline mode
                val localUser = prefsManager.getUser()
                if (localUser != null) {
                    Log.d(TAG, "Found local user: ${localUser.uid}")
                    emit(UIState.Success(localUser))
                } else {
                    Log.d(TAG, "No local user found, user not authenticated")
                    emit(UIState.Error("User not authenticated"))
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error getting current user", e)
            handleError("Error getting current user", e)
            emit(UIState.Error("Failed to get user profile: ${e.message ?: "Unknown error"}"))
        }
    }.catch { e ->
        Log.e(TAG, "Caught exception in getCurrentUser flow", e)
        handleError("Flow error in getCurrentUser", e)
        val errorMsg = networkExceptionHandler.handle(e as Exception)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Check and update usage limits (5 uses per 30 days)
     */
    fun checkAndUpdateUsageLimit(): Flow<UIState<UsageLimit>> = flow {
        try {
            Log.d(TAG, "Checking and updating usage limits")

            if (!isInitialized) {
                Log.e(TAG, "UserRepository not initialized")
                emit(UIState.Error("Repository not initialized"))
                return@flow
            }

            emit(UIState.Loading())

            // Get current user
            val userId = authManager.currentUserId
            if (userId == null) {
                Log.d(TAG, "No current user, cannot check usage limits")
                emit(UIState.Error("User not authenticated"))
                return@flow
            }

            // Get user from Firestore
            when (val userResult = firestoreManager.getUser(userId)) {
                is UIState.Success -> {
                    val user = userResult.data
                    val now = System.currentTimeMillis()
                    Log.d(TAG, "Got user data for usage limit check: ${user.uid}")

                    // Get the current usage limit from preferences or create a new one
                    val usageLimit = prefsManager.getUsageLimit() ?: UsageLimit(
                        usageCount = user.usageCount,
                        lastUpdated = Date()
                    )

                    val lastUpdated = usageLimit.lastUpdated.time
                    val daysSinceLastUsage = TimeUnit.MILLISECONDS.toDays(now - lastUpdated)
                    Log.d(TAG, "Days since last usage: $daysSinceLastUsage")

                    // Reset count if 30 days have passed
                    if (daysSinceLastUsage >= 30) {
                        try {
                            // Update the usage count to 0
                            firestoreManager.updateUsageCount(userId, 0)
                            Log.d(TAG, "Reset usage count to 0")

                            // Create a new reset usage limit
                            val resetUsageLimit = UsageLimit(
                                usageCount = 0,
                                lastUpdated = Date()
                            )

                            // Save the usage limit to preferences
                            prefsManager.saveUsageLimit(resetUsageLimit)

                            emit(UIState.Success(resetUsageLimit))
                        } catch (e: Throwable) {
                            Log.e(TAG, "Error resetting usage count", e)
                            handleError("Error resetting usage count", e)
                            emit(UIState.Error("Failed to reset usage count"))
                        }
                    } else {
                        // Return current usage limit
                        Log.d(TAG, "Using current usage limit: ${usageLimit.usageCount}")
                        emit(UIState.Success(usageLimit))
                    }
                }
                is UIState.Error -> {
                    Log.e(TAG, "Error getting user for usage limits: ${userResult.message}")
                    emit(UIState.Error(userResult.message))
                }
                is UIState.Loading -> {
                    // Loading state, no action needed
                }
                else -> {
                    // Handle other states if needed
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error checking usage limit", e)
            handleError("Error checking usage limit", e)
            emit(UIState.Error("Failed to check usage limit: ${e.message ?: "Unknown error"}"))
        }
    }.catch { e ->
        Log.e(TAG, "Caught exception in checkAndUpdateUsageLimit flow", e)
        handleError("Flow error in checkAndUpdateUsageLimit", e)
        val errorMsg = networkExceptionHandler.handle(e as Exception)
        emit(UIState.Error(errorMsg))
    }.flowOn(coroutineUtils.ioDispatcher)

    /**
     * Helper method to update user's last login time
     * @param userId The user ID to update
     */
    private suspend fun updateLastLogin(userId: String) {
        try {
            Log.d(TAG, "Updating last login for user: $userId")
            firestoreManager.updateLastLogin(userId)
        } catch (e: Throwable) {
            Log.e(TAG, "Error updating last login: ${e.message}", e)
        }
    }

    /**
     * Format phone number with India country code
     */
    private fun formatPhoneNumber(mobileNumber: String): String {
        return if (mobileNumber.startsWith("+")) {
            mobileNumber
        } else if (mobileNumber.startsWith("91")) {
            "+$mobileNumber"
        } else {
            "+91$mobileNumber"
        }
    }

    /**
     * Handles errors in the repository
     * @param message Error message
     * @param e Exception that occurred
     */
    private fun handleError(message: String, e: Throwable) {
        Log.e(TAG, "$message: ${e.message}", e)
        if (e is Exception) {
            networkExceptionHandler.handle(e)
        }
    }

    /**
     * Clean up resources when the repository is no longer needed
     */
    fun cleanup() {
        try {
            Log.d(TAG, "Cleaning up UserRepository")
            isInitialized = false
        } catch (e: Throwable) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}