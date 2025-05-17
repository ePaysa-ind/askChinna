/**
 * file path: app/src/main/java/com/example/askchinna/data/model/UIState.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 6, 2025
 * Version: 1.2
 *
 * Change Log:
 * 1.2 - May 6, 2025
 * - Added proper error handling
 * - Added state validation
 * - Added proper documentation
 * - Added helper methods for state transitions
 * - Added proper error recovery
 * - Added proper state management
 */
package com.example.askchinna.data.model

/**
 * Represents the state of the UI.
 * Used to manage loading, error, and success states.
 */
sealed class UIState<out T> {
    /**
     * Represents the initial state.
     */
    data object Initial : UIState<Nothing>()

    /**
     * Represents a loading state.
     * @param message Optional loading message
     */
    data class Loading(val message: String? = null) : UIState<Nothing>()

    /**
     * Represents a success state with data.
     * @param data The data to be displayed
     */
    data class Success<T>(override val data: T) : UIState<T>()

    /**
     * Represents an error state.
     * @param message The error message
     * @param cause The cause of the error
     * @param retryAction Optional action to retry the failed operation
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val retryAction: (() -> Unit)? = null
    ) : UIState<Nothing>()

    /**
     * Checks if the state is in a loading state.
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Checks if the state is in an error state.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Checks if the state is in a success state.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Gets the data if the state is in a success state.
     */
    open val data: T?
        get() = when (this) {
            is Success -> data
            else -> null
        }

    /**
     * Gets the error message if the state is in an error state.
     */
    val errorMessage: String?
        get() = when (this) {
            is Error -> message
            else -> null
        }

    /**
     * Gets the loading message if the state is in a loading state.
     */
    val loadingMessage: String?
        get() = when (this) {
            is Loading -> message
            else -> null
        }

    /**
     * Handles error recovery by providing a fallback value.
     * @param fallback Fallback value to use in case of error
     * @return New UIState with fallback value
     */
    @Suppress("UNCHECKED_CAST")
    fun <R> recover(fallback: R): UIState<R> {
        return when (this) {
            is Error -> Success(fallback)
            is Success -> {
                // Using explicit cast with suppression for unavoidable generic type erasure
                try {
                    Success(data as R)
                } catch (e: ClassCastException) {
                    // Fallback to provided value if cast fails
                    Success(fallback)
                }
            }
            is Loading -> Loading(message)
            is Initial -> Initial
        }
    }

    /**
     * Validates the state data.
     * @return true if valid, false otherwise
     */
    fun isValid(): Boolean {
        return when (this) {
            is Success -> data != null
            is Error -> message.isNotEmpty()
            is Loading -> true
            is Initial -> true
        }
    }
}