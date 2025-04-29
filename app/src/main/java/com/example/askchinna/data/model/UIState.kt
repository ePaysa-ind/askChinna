package com.example.askchinna.data.model

/**
 * app/src/main/java/com/askchinna/data/model/UIState.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


/**
 * Sealed class representing different UI states for data loading
 */
sealed class UIState<out T> {
    /**
     * Loading state when data is being fetched
     */
    object Loading : UIState<Nothing>()

    /**
     * Success state when data is successfully fetched
     *
     * @property data The fetched data
     */
    data class Success<T>(val data: T) : UIState<T>()

    /**
     * Error state when data fetching fails
     *
     * @property message Error message
     */
    data class Error(val message: String) : UIState<Nothing>()
}