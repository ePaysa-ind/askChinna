/**
 * File: app/src/main/java/com/example/askchinna/service/DataSeedService.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 15, 2025
 * Version: 1.4
 *
 * Change Log:
 * 1.4 - May 15, 2025
 * - Added authentication check before Firestore operations
 * - Fixed permission issues by deferring Firestore writes until authenticated
 * - Added local-only mode for unauthenticated users
 *
 * 1.3 - May 6, 2025
 * - Added proper error handling with try-catch blocks
 * - Added comprehensive documentation
 * - Added data validation methods
 * - Added proper resource cleanup
 * - Added proper logging
 * - Added proper state management
 * - Added proper error recovery
 * - Added proper data synchronization
 * - Added proper offline support
 * - Added proper retry mechanism
 */

package com.example.askchinna.service

import android.util.Log
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.repository.CropRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for seeding the app with initial data.
 * This includes loading crop information from local JSON files
 * and syncing with Firestore for updates.
 *
 * Optimized for low-end devices and offline functionality.
 * Implements retry mechanism and proper error handling.
 */
@Singleton
class DataSeedService @Inject constructor(
    private val cropRepository: CropRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val tag = "DataSeedService"

    companion object {
        private const val CROPS_COLLECTION = "crops"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    /**
     * Seeds the application with all initial data
     * This is the main entry point for initializing app data
     *
     * @return Boolean True if all data was seeded successfully
     */
    suspend fun seedInitialData(): Boolean = withContext(Dispatchers.IO) {

        for (attempt in 1..MAX_RETRY_ATTEMPTS) {
            try {
                Log.d(tag, "Starting initial data seeding (Attempt $attempt)")

                // Load crops from CropRepository
                val crops = cropRepository.getSupportedCrops()
                if (crops.isEmpty()) {
                    Log.e(tag, "Failed to load crop data")
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        kotlinx.coroutines.delay(RETRY_DELAY_MS)
                        continue
                    }
                    return@withContext false
                }

                Log.d(tag, "Successfully loaded ${crops.size} crops")

                // Validate that all required crops are present
                val cropsValid = validateRequiredCropsPresent(crops)
                if (!cropsValid) {
                    Log.e(tag, "Crop validation failed")
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        kotlinx.coroutines.delay(RETRY_DELAY_MS)
                        continue
                    }
                    return@withContext false
                }

                // Only attempt to sync with Firestore if user is authenticated
                // Otherwise, just use local data
                if (isUserAuthenticated() && isOnline()) {
                    Log.d(tag, "User is authenticated. Attempting to sync with Firestore")
                    syncWithFirestore(crops)
                } else {
                    Log.d(tag, "User is not authenticated or offline. Skipping Firestore sync")
                }

                Log.d(tag, "Initial data seeding completed successfully")
                return@withContext true
            } catch (e: Exception) {
                Log.e(tag, "Error during initial data seeding: ${e.message}", e)
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    kotlinx.coroutines.delay(RETRY_DELAY_MS)
                    continue
                }
                return@withContext false
            }
        }

        return@withContext false
    }

    /**
     * Validates that all required crops are present in the loaded crops.
     * The app requires exactly 10 specific crops to function properly.
     *
     * @param crops List of crops to validate
     * @return True if all required crops are present, false otherwise
     */
    private fun validateRequiredCropsPresent(crops: List<Crop>): Boolean {
        val requiredCropNames = listOf(
            "chili", "okra", "maize", "cotton", "tomato",
            "watermelon", "soybean", "rice", "wheat", "pigeon_pea"
        )

        try {
            val cropIds = crops.map { it.id.lowercase() }

            // Check if all required crops are present
            val missingCrops = requiredCropNames.filter { !cropIds.contains(it) }

            if (missingCrops.isNotEmpty()) {
                Log.w(tag, "Missing required crops: $missingCrops")
                return false
            }

            // Check if we have exactly 10 crops
            if (crops.size != 10) {
                Log.w(tag, "Expected exactly 10 crops, but found ${crops.size}")
                return false
            }

            // Validate each crop's data
            crops.forEach { crop ->
                if (!isValidCrop(crop)) {
                    Log.w(tag, "Invalid crop data for ${crop.id}")
                    return false
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(tag, "Error validating required crops", e)
            return false
        }
    }

    /**
     * Validates a single crop's data
     * @param crop Crop to validate
     * @return True if crop data is valid
     */
    private fun isValidCrop(crop: Crop): Boolean {
        return try {
            crop.id.isNotBlank() &&
                    crop.name.isNotBlank() &&
                    crop.iconResId > 0
        } catch (e: Exception) {
            Log.e(tag, "Error validating crop ${crop.id}", e)
            false
        }
    }

    /**
     * Checks if the current user is authenticated with Firebase
     * @return True if authenticated, false otherwise
     */
    private fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Syncs crop data with Firestore
     * @param crops List of crops to sync
     */
    private suspend fun syncWithFirestore(crops: List<Crop>) {
        try {
            // Double-check authentication before attempting Firestore operations
            if (!isUserAuthenticated()) {
                Log.w(tag, "Cannot sync with Firestore: User not authenticated")
                return
            }

            val batch = firestore.batch()
            val cropsRef = firestore.collection(CROPS_COLLECTION)

            crops.forEach { crop ->
                val docRef = cropsRef.document(crop.id)
                batch.set(docRef, crop)
            }

            batch.commit().await()
            Log.d(tag, "Successfully synced ${crops.size} crops with Firestore")
        } catch (e: Exception) {
            Log.e(tag, "Error syncing with Firestore: ${e.message}", e)
            // Don't rethrow - treat Firestore sync as optional
        }
    }

    /**
     * Checks if the device is online
     * @return True if online
     */
    private fun isOnline(): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec("ping -c 1 google.com")
            process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(tag, "Error checking online status", e)
            false
        }
    }
}