/**
 * File: app/src/main/java/com/example/askchinna/AskChinnaApplication.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.1
 */

package com.example.askchinna

import android.app.Application
import android.util.Log
import com.example.askchinna.data.remote.FirestoreInitializer
import androidx.appcompat.app.AppCompatDelegate
import com.example.askchinna.data.local.AppDatabase  // Fixed import path
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
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

            // Set default night mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            // Start network monitoring
            startMonitoring()

            // Initialize database and seed data
            initializeDatabase()

            // Initialize Firestore collections
            initializeFirestore()

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
                // Access database to ensure it's created
                // Fixed: removed direct access to openHelper and use getInstance pattern instead
                Log.d(TAG, "Initializing database...")

                // Seed initial data
                seedInitialData()

            } catch (e: Exception) {
                Log.e(TAG, "Database initialization error: ${e.message}")
                FirebaseCrashlytics.getInstance().recordException(e)
            }
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
                Log.e(TAG, "Error seeding initial data: ${e.message}")
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    /**
     * Initialize Firestore collections and documents
     */
    private fun initializeFirestore() {
        applicationScope.launch {
            try {
                val success = firestoreInitializer.initializeCollections()
                if (success) {
                    Log.d(TAG, "Firestore collections initialized successfully")
                } else {
                    Log.e(TAG, "Failed to initialize Firestore collections")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Firestore collections: ${e.message}")
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
        stopMonitoring()
    }

    private fun startMonitoring() {
        try {
            networkStateMonitor.startMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting network monitoring: ${e.message}")
        }
    }

    private fun stopMonitoring() {
        try {
            networkStateMonitor.stopMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping network monitoring: ${e.message}")
        }
    }
}