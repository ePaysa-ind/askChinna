/**
 * File: app/src/main/java/com/example/askchinna/util/SimpleCoroutineUtils.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 1, 2025
 * Version: 1.1
 */

package com.example.askchinna.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for coroutine dispatchers
 * Used for dependency injection of dispatchers in tests
 */
@Singleton
class SimpleCoroutineUtils @Inject constructor() {

    /**
     * Main dispatcher for UI operations
     */
    val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    /**
     * IO dispatcher for network and disk operations
     */
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
}