
/**
 * app/src/main/java/com/askchinna/data/remote/FirebaseAuthManager.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.data.remote

import android.app.Activity
import com.askchinna.data.local.SharedPreferencesManager
import com.askchinna.data.model.UIState
import com.askchinna.data.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

    /**
     * Current logged in user's UID
     */
    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    /**
     * Check if user is already authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null || prefsManager.getAuthToken().isNotEmpty()
    }

    /**
     * Begin the phone verification process - sends SMS with OTP
     * @param phoneNumber Full phone number with country code (e.g., "+919876543210")
     * @param activity Current activity for callbacks
     * @return Flow emitting UI states during the verification process
     */
    fun sendOtpToPhone(phoneNumber: String, activity: Activity): Flow<UIState<String>> = callbackFlow {
        try {
            trySend(UIState.Loading())

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification completed (rare on most devices)
                    trySend(UIState.Success("auto"))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    val error = networkExceptionHandler.handleAuthException(e)
                    trySend(UIState.Error(error))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // Store verification ID for later use
                    storedVerificationId = verificationId
                    trySend(UIState.Success(verificationId))
                }
            }

            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)

            awaitClose { /* Clean up resources if needed */ }
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleAuthException(e)
            trySend(UIState.Error(error))
            close(e)
        }
    }

    /**
     * Verify OTP entered by user
     * @param otp OTP code received by user
     * @return Flow with UI state for verification result
     */
    suspend fun verifyOtp(otp: String): UIState<User> {
        return try {
            val verificationId = storedVerificationId ?:
            return UIState.Error("Verification ID not found. Please request OTP again.")

            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val user = authResult.user ?:
            return UIState.Error("Authentication failed: User not found")

            // Create user model
            val userModel = User(
                uid = user.uid,
                mobileNumber = user.phoneNumber ?: "",
                isVerified = true
            )

            // Save auth token for offline usage
            user.getIdToken(false).await()?.token?.let { token ->
                prefsManager.saveAuthToken(token)
            }

            UIState.Success(userModel)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            UIState.Error("Invalid verification code")
        } catch (e: Exception) {
            val error = networkExceptionHandler.handleAuthException(e)
            UIState.Error(error)
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        firebaseAuth.signOut()
        prefsManager.clearAuthToken()
    }

    /**
     * Resend OTP code
     * @param phoneNumber Phone number to send OTP to
     * @param activity Current activity for callbacks
     */
    fun resendOtp(phoneNumber: String, activity: Activity): Flow<UIState<String>> {
        return sendOtpToPhone(phoneNumber, activity)
    }

    /**
     * Get the latest stored verification ID
     */
    fun getStoredVerificationId(): String? {
        return storedVerificationId
    }
}