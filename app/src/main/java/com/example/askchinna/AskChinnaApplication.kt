/**
 * File: app/src/main/java/com/example/askchinna/AskChinnaApplication.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna

import android.app.Application
import android.util.Log
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

    // Application-scoped coroutine context
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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

        // Initialize application components
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
            networkStateMonitor.startMonitoring()

            // Initialize database and seed data
            initializeDatabase()

            // Clean up temp files
            cleanupTempFiles()

        } catch (e: Exception) {
            Log.e(TAG, "Error during app initialization: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    /**
     * Initialize database and seed initial app data
     */
    private fun initializeDatabase() {
        applicationScope.launch {
            try {
                // Initialize room database
                database.openHelper.writableDatabase

                // Seed initial data if needed
                dataSeedService.seedInitialData()

            } catch (e: Exception) {
                Log.e(TAG, "Database initialization error: ${e.message}")
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    /**
     * Clean up temporary files from previous sessions
     */
    private fun cleanupTempFiles() {
        applicationScope.launch {
            try {
                // Clean up image temp files
                imageHelper.cleanupTempImages()
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning temp files: ${e.message}")
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        // Stop network monitoring
        networkStateMonitor.stopMonitoring()
    }
}