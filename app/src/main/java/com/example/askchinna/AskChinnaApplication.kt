/**
 * File: app/src/main/java/com/example/askchinna/AskChinnaApplication.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 5, 2025
 * Version: 1.3
 */

package com.example.askchinna

import android.app.Application
import android.util.Log
import com.example.askchinna.data.remote.FirestoreInitializer
import androidx.appcompat.app.AppCompatDelegate
import com.example.askchinna.data.local.AppDatabase
import com.example.askchinna.service.DataSeedService
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.NetworkStateMonitor
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import javax.inject.Inject

/**
 * Custom Application class for the askChinna app
 * Handles initialization of required components and services
 */
@HiltAndroidApp
class AskChinnaApplication : Application() {

    companion object {
        private const val TAG = "AskChinnaApplication"
    }

    // Application-scoped coroutine context with proper cleanup
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Inject
    lateinit var firestoreInitializer: FirestoreInitializer

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    @Inject
    lateinit var dataSeedService: DataSeedService

    @Inject
    lateinit var imageHelper: ImageHelper

    override fun onCreate() {
        super.onCreate()
        initializeApp()
    }

    /**
     * Initialize all required components of the app
     */
    private fun initializeApp() {
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Configure Firebase Crashlytics
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

            // Set default night mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            // Start network monitoring
            startMonitoring()

            // Initialize database and seed data
            initializeDatabase()

            // Log that Firestore initialization is deferred
            Log.d(TAG, "Firestore initialization deferred until after authentication")

            // Clean up temp files
            cleanupTempFiles()

        } catch (e: Exception) {
            handleInitializationError(e)
        }
    }

    /**
     * Handle initialization errors with specific error types
     */
    private fun handleInitializationError(error: Exception) {
        val errorMessage = when (error) {
            is IllegalStateException -> "App initialization failed: ${error.message}"
            is SecurityException -> "Security error during initialization: ${error.message}"
            else -> "Unexpected error during initialization: ${error.message}"
        }

        Log.e(TAG, errorMessage, error)
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", error.javaClass.simpleName)
            recordException(error)
        }
    }

    /**
     * Initialize database and seed initial app data
     */
    private fun initializeDatabase() {
        applicationScope.launch {
            try {
                Log.d(TAG, "Initializing database...")
                seedInitialData()
            } catch (e: Exception) {
                handleDatabaseError(e)
            }
        }
    }

    /**
     * Handle database-specific errors
     */
    private fun handleDatabaseError(error: Exception) {
        val errorMessage = when (error) {
            is IllegalStateException -> "Database initialization failed: ${error.message}"
            is SecurityException -> "Database security error: ${error.message}"
            else -> "Database error: ${error.message}"
        }

        Log.e(TAG, errorMessage, error)
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", error.javaClass.simpleName)
            setCustomKey("error_location", "database_initialization")
            recordException(error)
        }
    }

    /**
     * Seed predefined app data (crops, users, etc.)
     */
    private fun seedInitialData() {
        applicationScope.launch {
            try {
                val success = dataSeedService.seedInitialData()
                if (success) {
                    Log.d(TAG, "Initial data seeded successfully")
                } else {
                    Log.e(TAG, "Failed to seed initial data")
                }
            } catch (e: Exception) {
                handleDataSeedingError(e)
            }
        }
    }

    /**
     * Handle data seeding errors
     */
    private fun handleDataSeedingError(error: Exception) {
        val errorMessage = when (error) {
            is IllegalStateException -> "Data seeding failed: ${error.message}"
            is SecurityException -> "Data seeding security error: ${error.message}"
            else -> "Data seeding error: ${error.message}"
        }

        Log.e(TAG, errorMessage, error)
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", error.javaClass.simpleName)
            setCustomKey("error_location", "data_seeding")
            recordException(error)
        }
    }



    /**
     * Clean up temporary files from previous sessions
     */
    private fun cleanupTempFiles() {
        try {
            // Clean up image temp files
            imageHelper.cleanupTempImages()

            // Clean up any other temporary files
            // Add other temp file cleanup operations here

        } catch (e: Exception) {
            handleTempFileCleanupError(e)
        }
    }

    /**
     * Handle temporary file cleanup errors
     */
    private fun handleTempFileCleanupError(error: Exception) {
        val errorMessage = when (error) {
            is IllegalStateException -> "Temp file cleanup failed: ${error.message}"
            is SecurityException -> "Temp file cleanup security error: ${error.message}"
            else -> "Temp file cleanup error: ${error.message}"
        }

        Log.e(TAG, errorMessage, error)
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", error.javaClass.simpleName)
            setCustomKey("error_location", "temp_file_cleanup")
            recordException(error)
        }
    }

    override fun onTerminate() {
        try {
            // 1. First stop all background operations
            applicationScope.cancel()

            // 2. Stop network monitoring
            stopMonitoring()

            // 3. Clean up temporary files
            cleanupTempFiles()

            // 4. Close database connections
            database.close()

            // 5. Clear any cached data
            clearCachedData()

            super.onTerminate()
        } catch (e: Exception) {
            handleTerminationError(e)
        }
    }

    /**
     * Clean up any cached data in memory
     */
    private fun clearCachedData() {
        try {
            // Clear image cache
            imageHelper.clearImageCache()

            // Clear any other cached data
            // Add other cache clearing operations here

        } catch (e: Exception) {
            handleCacheClearingError(e)
        }
    }

    /**
     * Handle termination errors with specific error types
     */
    private fun handleTerminationError(error: Exception) {
        val errorMessage = when (error) {
            is IllegalStateException -> "Application termination failed: ${error.message}"
            is SecurityException -> "Application termination security error: ${error.message}"
            else -> "Application termination error: ${error.message}"
        }

        Log.e(TAG, errorMessage, error)
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", error.javaClass.simpleName)
            setCustomKey("error_location", "application_termination")
            recordException(error)
        }
    }

    /**
     * Handle cache clearing errors
     */
    private fun handleCacheClearingError(error: Exception) {
        val errorMessage = when (error) {
            is IllegalStateException -> "Cache clearing failed: ${error.message}"
            is SecurityException -> "Cache clearing security error: ${error.message}"
            else -> "Cache clearing error: ${error.message}"
        }

        Log.e(TAG, errorMessage, error)
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", error.javaClass.simpleName)
            setCustomKey("error_location", "cache_clearing")
            recordException(error)
        }
    }

    private fun startMonitoring() {
        try {
            networkStateMonitor.startMonitoring()
        } catch (e: Exception) {
            handleNetworkMonitoringError(e)
        }
    }

    /**
     * Handle network monitoring errors with specific error types
     */
    private fun handleNetworkMonitoringError(error: Exception) {
        val errorMessage = when (error) {
            is IllegalStateException -> "Network monitoring failed: ${error.message}"
            is SecurityException -> "Network monitoring security error: ${error.message}"
            else -> "Network monitoring error: ${error.message}"
        }

        Log.e(TAG, errorMessage, error)
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", error.javaClass.simpleName)
            setCustomKey("error_location", "network_monitoring")
            recordException(error)
        }
    }

    private fun stopMonitoring() {
        try {
            networkStateMonitor.stopMonitoring()
        } catch (e: Exception) {
            handleNetworkMonitoringError(e)
        }
    }
}