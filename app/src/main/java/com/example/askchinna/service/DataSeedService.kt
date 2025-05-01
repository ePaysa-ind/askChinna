/*
 * File: com/example/askchinna/service/DataSeedService.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.service

import android.content.Context
import android.util.Log
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.repository.CropRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
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
    private val TAG = "DataSeedService"

    companion object {
        private const val CROPS_COLLECTION = "crops"
        private const val LAST_UPDATE_KEY = "last_update_timestamp"
    }

    /**
     * Seeds the application with initial crop data.
     * First tries to load from local database, then from raw resources,
     * and finally attempts to sync with Firestore if online.
     *
     * @return Number of crops successfully loaded, or -1 on failure
     */
    suspend fun seedCropData(): Int = withContext(Dispatchers.IO) {
        try {
            // First check if we already have crops in the local database
            val existingCrops = cropRepository.getAllCrops()
            if (existingCrops.isNotEmpty()) {
                Log.d(TAG, "Using existing crop data from local database: ${existingCrops.size} crops")

                // Try to sync with Firestore in the background
                try {
                    syncWithFirestore()
                } catch (e: Exception) {
                    Log.w(TAG, "Background sync with Firestore failed, will use local data", e)
                    // This is non-fatal as we already have local data
                }

                return@withContext existingCrops.size
            }

            // Load from raw resources as fallback
            val cropsFromJson = loadCropsFromRawResource()
            if (cropsFromJson.isNotEmpty()) {
                Log.d(TAG, "Loaded crop data from raw resources: ${cropsFromJson.size} crops")

                // Save to local database
                cropRepository.insertAllCrops(cropsFromJson)

                // Try to sync with Firestore if possible
                try {
                    syncWithFirestore()
                } catch (e: Exception) {
                    Log.w(TAG, "Sync with Firestore failed, using local data only", e)
                    // This is non-fatal as we already loaded from resources
                }

                return@withContext cropsFromJson.size
            }

            Log.e(TAG, "Failed to load crop data from any source")
            return@withContext -1
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding crop data", e)
            return@withContext -1
        }
    }

    /**
     * Loads crop data from the raw resource JSON file.
     * This provides offline functionality even on first app launch.
     *
     * @return List of Crop objects from the JSON file
     * @throws IOException If the resource cannot be read
     */
    @Throws(IOException::class)
    private fun loadCropsFromRawResource(): List<Crop> {
        try {
            val inputStream = context.resources.openRawResource(R.raw.crops_data)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()
            reader.close()

            val cropListType = object : TypeToken<List<Crop>>() {}.type
            return Gson().fromJson(jsonString, cropListType)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading crops data from raw resource", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing crops data JSON", e)
            throw IOException("Failed to parse crops data", e)
        }
    }

    /**
     * Synchronizes local crop data with Firestore.
     * Checks for newer data on Firestore and updates local database if needed.
     *
     * @throws Exception if synchronization fails
     */
    @Throws(Exception::class)
    private suspend fun syncWithFirestore() = withContext(Dispatchers.IO) {
        try {
            // Get last update timestamp
            val lastUpdateTimestamp = cropRepository.getLastUpdateTimestamp() ?: 0L

            // Query Firestore for newer data
            val cropsCollection = firestore.collection(CROPS_COLLECTION)
                .whereGreaterThan(LAST_UPDATE_KEY, lastUpdateTimestamp)
                .get()
                .await()

            if (cropsCollection.isEmpty) {
                Log.d(TAG, "No newer crop data found in Firestore")
                return@withContext
            }

            // Convert Firestore documents to Crop objects
            val updatedCrops = cropsCollection.documents.mapNotNull { document ->
                try {
                    document.toObject(Crop::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Error converting Firestore document to Crop", e)
                    null
                }
            }

            if (updatedCrops.isNotEmpty()) {
                Log.d(TAG, "Updating ${updatedCrops.size} crops from Firestore")
                cropRepository.insertAllCrops(updatedCrops)

                // Update the last sync timestamp
                val maxTimestamp = updatedCrops.maxOfOrNull { it.lastUpdateTimestamp ?: 0L } ?: 0L
                if (maxTimestamp > 0) {
                    cropRepository.updateLastUpdateTimestamp(maxTimestamp)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing with Firestore", e)
            throw e
        }
    }

    /**
     * Validates that all required crops are present in the database.
     * The app requires exactly 10 specific crops to function properly.
     *
     * @return True if all required crops are present, false otherwise
     */
    suspend fun validateRequiredCropsPresent(): Boolean = withContext(Dispatchers.IO) {
        val requiredCropNames = listOf(
            "chilies", "okra", "maize", "cotton", "tomatoes",
            "watermelon", "soybean", "rice", "wheat", "pigeon peas"
        )

        try {
            val crops = cropRepository.getAllCrops()
            val cropNames = crops.map { it.name.lowercase() }

            // Check if all required crops are present
            val missingCrops = requiredCropNames.filter { !cropNames.contains(it) }

            if (missingCrops.isNotEmpty()) {
                Log.w(TAG, "Missing required crops: $missingCrops")
                return@withContext false
            }

            // Check if we have exactly 10 crops
            if (crops.size != 10) {
                Log.w(TAG, "Expected exactly 10 crops, but found ${crops.size}")
                return@withContext false
            }

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error validating required crops", e)
            return@withContext false
        }
    }
}