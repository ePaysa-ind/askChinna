/**
 * app/src/main/java/com/askchinna/util/SimpleCoroutineUtils.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.util

import com.askchinna.data.model.UIState
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility functions for working with coroutines
 */
object SimpleCoroutineUtils {

    /**
     * Execute network request with basic error handling
     * @param call Suspend function to call
     * @param onError Error handler
     * @return UIState<T> UI state with result or error
     */
    suspend fun <T> executeNetworkCall(
        call: suspend () -> T,
        onError: (Exception) -> String = { it.message ?: "Unknown error" }
    ): UIState<T> {
        return try {
            val result = call()
            UIState.Success(result)
        } catch (e: Exception) {
            UIState.Error(onError(e))
        }
    }

    /**
     * Convert a Firebase Task to a suspend function
     * @return T Result of the task
     */
    suspend fun <T> Task<T>.await(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                continuation.resume(result)
            }
            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
    }

    /**
     * Retry a suspend function with exponential backoff
     * @param times Number of retry attempts
     * @param initialDelay Initial delay in milliseconds
     * @param maxDelay Maximum delay in milliseconds
     * @param factor Exponential backoff factor
     * @param block Suspend function to retry
     * @return T Result of the block
     */
    suspend fun <T> retry(
        times: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 5000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                // Wait for the specified delay before retrying
                kotlinx.coroutines.delay(currentDelay)
                // Increase the delay for the next retry with a maximum cap
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        // Last attempt
        return block()
    }
}