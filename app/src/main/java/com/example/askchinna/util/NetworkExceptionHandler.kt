package com.example.askchinna.util

/**
 * app/src/main/java/com/example/askchinna/util/NetworkExceptionHandler.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 1, 2025
 * Version: 1.1
 */
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handler for network and Firebase exceptions
 * Provides human-readable error messages
 */
@Singleton
class NetworkExceptionHandler @Inject constructor() {

    /**
     * Handle general exceptions
     * @param exception The exception to handle
     * @return Human-readable error message
     */
    fun handle(exception: Exception): String {
        return when (exception) {
            is FirebaseException -> handleFirebaseException(exception)
            is UnknownHostException -> "No internet connection. Please check your network."
            is SocketTimeoutException -> "Connection timed out. Please try again."
            else -> "An unexpected error occurred: ${exception.message}"
        }
    }

    /**
     * Handle Firebase-specific exceptions
     * @param exception The Firebase exception
     * @return Human-readable error message
     */
    private fun handleFirebaseException(exception: FirebaseException): String {
        return when (exception) {
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            is FirebaseAuthException -> handleAuthException(exception)
            is FirebaseFirestoreException -> handleFirestoreException(exception)
            else -> "Firebase error: ${exception.message}"
        }
    }

    /**
     * Handle Firebase Auth exceptions
     * @param exception The Auth exception
     * @return Human-readable error message
     */
    private fun handleAuthException(exception: Exception): String {
        return when {
            exception.message?.contains("invalid-verification-code") == true ->
                "Invalid verification code. Please check and try again."
            exception.message?.contains("session-expired") == true ->
                "OTP session expired. Please request a new OTP."
            exception.message?.contains("credential-already-in-use") == true ->
                "This phone number is already linked to another account."
            exception.message?.contains("invalid-phone-number") == true ->
                "Invalid phone number format. Please enter a valid number."
            exception.message?.contains("quota-exceeded") == true ->
                "Too many requests. Please try again later."
            else -> "Authentication error: ${exception.message}"
        }
    }

    /**
     * Handle Firestore exceptions
     * @param exception The Firestore exception
     * @return Human-readable error message
     */
    private fun handleFirestoreException(exception: FirebaseFirestoreException): String {
        return when (exception.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Database unavailable. Please try again later."
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Permission denied. You don't have access to this data."
            FirebaseFirestoreException.Code.NOT_FOUND ->
                "Data not found. The requested information doesn't exist."
            FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                "This data already exists in the database."
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                "Database resource limit reached. Please try again later."
            else -> "Database error: ${exception.message}"
        }
    }
}