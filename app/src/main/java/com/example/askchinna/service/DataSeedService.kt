/**
 * File: app/src/main/java/com/example/askchinna/service/DataSeedService.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.2
 */

package com.example.askchinna.service

import android.content.Context
import android.util.Log
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.repository.CropRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for seeding the app with initial data.
 * This includes loading crop information from local JSON files
 * and syncing with Firestore for updates.
 *
 * Optimized for low-end devices and offline functionality.
 */
@Singleton
class DataSeedService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cropRepository: CropRepository,
    private val firestore: FirebaseFirestore
) {
    private val tag = "DataSeedService"

    companion object {
        private const val CROPS_COLLECTION = "crops"
        private const val LAST_UPDATE_KEY = "last_update_timestamp"
    }

    /**
     * Seeds the application with all initial data
     * This is the main entry point for initializing app data
     *
     * @return Boolean True if all data was seeded successfully
     */
    suspend fun seedInitialData(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Starting initial data seeding")

            // Load crops from CropRepository
            val crops = cropRepository.getSupportedCrops()
            if (crops.isEmpty()) {
                Log.e(tag, "Failed to load crop data")
                return@withContext false
            }

            Log.d(tag, "Successfully loaded ${crops.size} crops")

            // Validate that all required crops are present
            val cropsValid = validateRequiredCropsPresent(crops)
            if (!cropsValid) {
                Log.e(tag, "Crop validation failed")
                return@withContext false
            }

            // Additional data seeding can be added here
            // For example: seed user data, default settings, etc.

            Log.d(tag, "Initial data seeding completed successfully")
            return@withContext true
        } catch (e: Exception) {
            Log.e(tag, "Error during initial data seeding: ${e.message}", e)
            return@withContext false
        }
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

            return true
        } catch (e: Exception) {
            Log.e(tag, "Error validating required crops", e)
            return false
        }
    }
}