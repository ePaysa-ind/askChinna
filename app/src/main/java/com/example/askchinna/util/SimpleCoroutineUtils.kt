/**
 * File: app/src/main/java/com/example/askchinna/util/SimpleCoroutineUtils.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.3
 * 
 * Change Log:
 * 1.3 - May 6, 2025
 * - Added proper error handling with try-catch blocks
 * - Added comprehensive documentation
 * - Added coroutine exception handler
 * - Added supervisor job for better error handling
 * - Added proper logging
 * - Added coroutine scope management
 * - Added proper cleanup in error cases
 * - Added memory-efficient coroutine operations
 * - Added proper dependency injection support
 * - Added utility methods for different dispatchers
 */

package com.example.askchinna.util

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for coroutine dispatchers and common coroutine operations.
 * Provides centralized management of coroutine scopes, dispatchers, and error handling.
 * Used for dependency injection of dispatchers in tests.
 */
@Singleton
class SimpleCoroutineUtils @Inject constructor() {
    private val TAG = "SimpleCoroutineUtils"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error: ${throwable.message}", throwable)
    }

    /**
     * Main dispatcher for UI operations
     */
    val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    /**
     * IO dispatcher for network and disk operations
     */
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    /**
     * Default dispatcher for CPU-intensive operations
     */
    val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * Unconfined dispatcher for operations that don't require specific thread
     */
    val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    /**
     * Launches a coroutine with proper error handling
     * @param dispatcher CoroutineDispatcher to use
     * @param block The code to execute
     * @return Job that can be cancelled
     */
    fun launchWithErrorHandling(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return scope.launch(dispatcher + exceptionHandler) {
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Error in coroutine: ${e.message}", e)
            }
        }
    }

    /**
     * Executes a suspend function with proper error handling
     * @param dispatcher CoroutineDispatcher to use
     * @param block The suspend function to execute
     * @return Result of the operation
     */
    suspend fun <T> withErrorHandling(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        block: suspend CoroutineScope.() -> T
    ): Result<T> {
        return try {
            Result.success(withContext(dispatcher + exceptionHandler) {
                block()
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in coroutine: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cancels all coroutines and cleans up resources
     */
    fun cleanup() {
        try {
            job.cancel()
            scope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }

    companion object {
        /**
         * Executes the given action on the main thread after specified delay
         * @param delayMillis Delay in milliseconds
         * @param action Action to execute after the delay
         * @return Job that can be cancelled
         */
        fun delayOnMain(delayMillis: Long, action: () -> Unit): Job {
            return CoroutineScope(Dispatchers.Main).launch {
                try {
                    delay(delayMillis)
                    action()
                } catch (e: Exception) {
                    Log.e("SimpleCoroutineUtils", "Error in delayOnMain: ${e.message}", e)
                }
            }
        }

        /**
         * Executes the given action on the main thread at regular intervals
         * @param intervalMillis Interval between executions in milliseconds
         * @param initialDelayMillis Initial delay before first execution (default 0)
         * @param action Action to execute at each interval
         * @return Job that can be cancelled to stop the interval
         */
        fun intervalOnMain(intervalMillis: Long, initialDelayMillis: Long = 0, action: () -> Unit): Job {
            return CoroutineScope(Dispatchers.Main).launch {
                try {
                    delay(initialDelayMillis)
                    while (isActive) {
                        action()
                        delay(intervalMillis)
                    }
                } catch (e: Exception) {
                    Log.e("SimpleCoroutineUtils", "Error in intervalOnMain: ${e.message}", e)
                }
            }
        }

        /**
         * Executes the given action on the IO thread
         * @param action Action to execute
         * @return Job that can be cancelled
         */
        fun launchOnIO(action: suspend () -> Unit): Job {
            return CoroutineScope(Dispatchers.IO).launch {
                try {
                    action()
                } catch (e: Exception) {
                    Log.e("SimpleCoroutineUtils", "Error in launchOnIO: ${e.message}", e)
                }
            }
        }

        /**
         * Executes the given action on the Default thread
         * @param action Action to execute
         * @return Job that can be cancelled
         */
        fun launchOnDefault(action: suspend () -> Unit): Job {
            return CoroutineScope(Dispatchers.Default).launch {
                try {
                    action()
                } catch (e: Exception) {
                    Log.e("SimpleCoroutineUtils", "Error in launchOnDefault: ${e.message}", e)
                }
            }
        }
    }
}