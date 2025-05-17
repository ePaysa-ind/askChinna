/**
 * File: app/src/main/java/com/example/askchinna/data/remote/FirebaseAuthManager.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 16, 2025
 * Version: 1.5
 */
package com.example.askchinna.data.remote

import android.app.Activity
import android.util.Log
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.User
import com.example.askchinna.util.NetworkExceptionHandler
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirebaseAuthManager"
private const val PHONE_NUMBER_REGEX = "^\\+[1-9]\\d{1,14}$"
private const val MAX_RETRY_ATTEMPTS = 3

/**
 * Manager class for handling Firebase Phone Authentication
 */
@Singleton
class FirebaseAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val prefsManager: SharedPreferencesManager,
    private val networkExceptionHandler: NetworkExceptionHandler
) {
    /**
     * Current verification ID from phone auth
     */
    private var storedVerificationId: String? = null
    private var retryCount = 0

    /**
     * Current logged in user's UID
     */
    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    /**
     * Begin the phone verification process - sends SMS with OTP
     * @param phoneNumber Full phone number with country code (e.g., "+919876543210")
     * @param activity Current activity for callbacks
     * @return Flow emitting UI states during the verification process
     * @throws IllegalArgumentException if phone number format is invalid
     */
    fun sendOtpToPhone(phoneNumber: String, activity: Activity): Flow<UIState<String>> = callbackFlow {
        try {
            Log.d(TAG, "========== STARTING OTP PROCESS ==========")
            Log.d(TAG, "Sending OTP to phone: $phoneNumber")
            Log.d(TAG, "Current Auth state: User ${if (firebaseAuth.currentUser != null) "IS" else "is NOT"} logged in")
            Log.d(TAG, "Activity: ${activity.javaClass.simpleName}, isFinishing: ${activity.isFinishing}")

            if (!isValidPhoneNumber(phoneNumber)) {
                Log.e(TAG, "Invalid phone number format: $phoneNumber")
                Log.e(TAG, "Phone should match pattern: $PHONE_NUMBER_REGEX")
                throw IllegalArgumentException("Invalid phone number format")
            }

            trySend(UIState.Loading())
            retryCount = 0

            // Test phone number handling for development
            // These numbers should be configured in Firebase console
            val isTestNumber = phoneNumber == "+917780350915" ||
                    phoneNumber == "+919866679227" ||
                    phoneNumber == "+917993754064"

            if (isTestNumber) {
                Log.d(TAG, "TEST NUMBER DETECTED: $phoneNumber - Using Firebase test verification flow")
                Log.d(TAG, "Test numbers should have verification code 987123 in Firebase console")
            }

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    try {
                        Log.d(TAG, "✅ onVerificationCompleted: Auto-verification completed!")
                        Log.d(TAG, "Auth provider: ${credential.provider}")
                        Log.d(TAG, "SmsCode present: ${credential.smsCode != null}")

                        // Auto-verification completed (rare on most devices)
                        trySend(UIState.Success("auto"))
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error in auto verification", e)
                        Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                        trySend(UIState.Error("Auto verification failed: ${e.message ?: "unknown error"}"))
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "❌ onVerificationFailed: Verification FAILED", e)
                    Log.e(TAG, "Error class: ${e.javaClass.simpleName}")
                    Log.e(TAG, "Error message: ${e.message}")
                    Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")

                    val errorMessage = networkExceptionHandler.handle(e)
                    Log.e(TAG, "Sending error to UI: $errorMessage")
                    trySend(UIState.Error(errorMessage))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    try {
                        Log.d(TAG, "✅ onCodeSent: Code sent successfully!")
                        Log.d(TAG, "Verification ID received: ${verificationId.take(5)}...${verificationId.takeLast(5)}")
                        Log.d(TAG, "Force resending token received: $token")

                        // Store verification ID for later use
                        storedVerificationId = verificationId
                        trySend(UIState.Success(verificationId))
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error handling code sent callback", e)
                        Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                        trySend(UIState.Error("Failed to store verification ID: ${e.message ?: "unknown error"}"))
                    }
                }

                override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                    Log.d(TAG, "⚠️ onCodeAutoRetrievalTimeOut: Auto retrieval timed out")
                    Log.d(TAG, "Verification ID: ${verificationId.take(5)}...${verificationId.takeLast(5)}")
                    // We don't need to take any action here, user can still enter code manually
                }
            }

            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            Log.d(TAG, "📱 Calling PhoneAuthProvider.verifyPhoneNumber...")
            Log.d(TAG, "Options: timeout=60s, activity=${activity.javaClass.simpleName}")

            PhoneAuthProvider.verifyPhoneNumber(options)
            Log.d(TAG, "📤 verifyPhoneNumber call completed, waiting for callbacks...")

            awaitClose {
                // Clean up resources
                Log.d(TAG, "Flow closed, cleaning up resources")
                storedVerificationId = null
                retryCount = 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ CRITICAL ERROR sending OTP", e)
            Log.e(TAG, "Error class: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")

            val errorMessage = networkExceptionHandler.handle(e)
            Log.e(TAG, "Sending error to UI: $errorMessage")
            trySend(UIState.Error(errorMessage))
            close(e)
        }
    }

    /**
     * Verify OTP entered by user
     * @param otp OTP code received by user
     * @return UIState with verification result
     */
    suspend fun verifyOtp(otp: String): UIState<User> {
        return try {
            Log.d(TAG, "========== VERIFYING OTP ==========")
            Log.d(TAG, "Verifying OTP: ${otp.replace(Regex("."), "*")}") // Mask OTP in logs for security

            val verificationId = storedVerificationId
            if (verificationId == null) {
                Log.e(TAG, "❌ Verification ID not found in memory")
                return UIState.Error("Verification ID not found. Please request OTP again.")
            }

            if (otp.length != 6) {
                Log.e(TAG, "❌ Invalid OTP format: ${otp.length} digits (expected 6)")
                return UIState.Error("Invalid OTP format")
            }

            // Handle test phone numbers for development
            if (verificationId == "test_verification_id") {
                Log.d(TAG, "🧪 Using test verification flow for development")
                // For testing without real SMS
                val user = User(
                    uid = "test_user_id",
                    displayName = "Test User",
                    mobileNumber = "+917780350915",
                    isVerified = true,
                    usageCount = 0
                )
                Log.d(TAG, "✅ Test user created successfully: ${user.uid}")
                return UIState.Success(user)
            }

            Log.d(TAG, "Creating credential with verification ID: ${verificationId.take(5)}...${verificationId.takeLast(5)} and OTP")
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            Log.d(TAG, "Credential created: ${credential.provider}")

            Log.d(TAG, "📱 Signing in with credential...")
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Log.d(TAG, "✅ Sign in successful!")

            val user = authResult.user
            if (user == null) {
                Log.e(TAG, "❌ Authentication failed: User returned null after signInWithCredential")
                return UIState.Error("Authentication failed: User not found")
            }

            Log.d(TAG, "User authenticated: ${user.uid}")
            Log.d(TAG, "User phone: ${user.phoneNumber}")
            Log.d(TAG, "User display name: ${user.displayName ?: "null"}")

            // Create user model
            val userModel = User(
                uid = user.uid,
                displayName = user.displayName ?: "",
                mobileNumber = user.phoneNumber ?: "",
                isVerified = true,
                usageCount = 0 // Initialize usage count to 0
            )
            Log.d(TAG, "User model created: $userModel")

            // Save user to preferences
            try {
                prefsManager.saveUser(userModel)
                Log.d(TAG, "✅ User saved to preferences")
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Error saving user to preferences", e)
                Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                // Continue even if preference save fails
            }

            // Save auth token for offline usage
            try {
                val tokenTask = user.getIdToken(false).await()
                val token = tokenTask?.token
                if (token != null) {
                    prefsManager.saveAuthToken(token)
                    Log.d(TAG, "✅ Auth token saved: ${token.take(10)}...")
                } else {
                    Log.w(TAG, "⚠️ ID token is null")
                }

                // Update auth state
                prefsManager.saveAuthState(true)
                Log.d(TAG, "✅ Auth state saved as AUTHENTICATED")
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Error saving auth token", e)
                Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                // Continue even if token save fails
            }

            Log.d(TAG, "========== OTP VERIFICATION COMPLETE ==========")
            UIState.Success(userModel)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "❌ Invalid verification code", e)
            Log.e(TAG, "Error class: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            UIState.Error("Invalid verification code")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verifying OTP", e)
            Log.e(TAG, "Error class: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            val errorMessage = networkExceptionHandler.handle(e)
            UIState.Error(errorMessage)
        }
    }

    /**
     * Sign out current user and clear all session data
     */
    fun signOut() {
        try {
            Log.d(TAG, "Signing out user")
            Log.d(TAG, "Current user before signout: ${firebaseAuth.currentUser?.uid ?: "null"}")

            firebaseAuth.signOut()
            prefsManager.clearAll()  // Use clearAll() method to clear everything
            storedVerificationId = null
            retryCount = 0

            Log.d(TAG, "User signed out successfully")
            Log.d(TAG, "Current user after signout: ${firebaseAuth.currentUser?.uid ?: "null"}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error signing out", e)
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
        }
    }

    /**
     * Resend OTP code with retry mechanism
     * @param phoneNumber Phone number to send OTP to
     * @param activity Current activity for callbacks
     * @return Flow emitting UI states during the resend process
     */
    fun resendOtp(phoneNumber: String, activity: Activity): Flow<UIState<String>> {
        Log.d(TAG, "========== RESENDING OTP ==========")
        Log.d(TAG, "Resending OTP to: $phoneNumber (attempt ${retryCount + 1}/$MAX_RETRY_ATTEMPTS)")

        return if (retryCount < MAX_RETRY_ATTEMPTS) {
            retryCount++
            Log.d(TAG, "Proceeding with resend (attempt $retryCount/$MAX_RETRY_ATTEMPTS)")
            sendOtpToPhone(phoneNumber, activity)
        } else {
            Log.w(TAG, "⚠️ Maximum retry attempts reached ($MAX_RETRY_ATTEMPTS)")
            callbackFlow {
                trySend(UIState.Error("Maximum retry attempts reached. Please try again later."))
                close()
            }
        }
    }

    /**
     * Validate phone number format
     * @param phoneNumber Phone number to validate
     * @return true if valid, false otherwise
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val isValid = phoneNumber.matches(Regex(PHONE_NUMBER_REGEX))
        Log.d(TAG, "Phone number validation: $phoneNumber is ${if (isValid) "valid ✅" else "invalid ❌"}")
        if (!isValid) {
            Log.d(TAG, "Expected format: $PHONE_NUMBER_REGEX (e.g., +919876543210)")
        }
        return isValid
    }
}