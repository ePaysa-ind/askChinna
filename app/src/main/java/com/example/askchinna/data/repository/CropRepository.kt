/**
 * File: app/src/main/java/com/example/askchinna/data/repository/CropRepository.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 16, 2025
 * Version: 1.3
 */
package com.example.askchinna.data.repository

import android.content.Context
import android.util.Log
import com.example.askchinna.data.model.Crop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing crop data
 */
@Singleton
class CropRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "CropRepository"

    // Cache crops in memory once loaded
    private var cachedCrops: List<Crop>? = null

    /**
     * Get all supported crops.
     * This method first tries to fetch from local cache, then from local JSON,
     * and finally from Firestore if available.
     *
     * @return List of supported crops
     */
    suspend fun getSupportedCrops(): List<Crop> = withContext(Dispatchers.IO) {
        try {
            // Return cached crops if available
            cachedCrops?.let { return@withContext it }

            // Load from local JSON file as fallback
            val crops = loadCropsFromLocalJson()
            if (crops.isNotEmpty()) {
                cachedCrops = crops
                return@withContext crops
            }

            // As last resort, try to load from Firestore
            return@withContext loadCropsFromFirestore()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting supported crops", e)
            return@withContext emptyList<Crop>()
        }
    }

    /**
     * Get a crop by its ID.
     * This method fetches all crops and then filters for the one with the matching ID.
     *
     * @param cropId The ID of the crop to retrieve
     * @return The crop with the given ID, or null if not found
     */
    suspend fun getCropById(cropId: String): Crop? = withContext(Dispatchers.IO) {
        try {
            val crops = getSupportedCrops()
            return@withContext crops.find { it.id == cropId }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting crop by ID: $cropId", e)
            return@withContext null
        }
    }

    /**
     * Load crops from local JSON file in assets
     */
    private fun loadCropsFromLocalJson(): List<Crop> {
        return try {
            val jsonString = context.assets.open("crops_data.json").bufferedReader().use { it.readText() }
            Log.d(TAG, "Read JSON file: ${jsonString.take(100)}...")

            val type = object : TypeToken<List<Crop>>() {}.type
            val crops = Gson().fromJson<List<Crop>>(jsonString, type)

            Log.d(TAG, "Successfully parsed ${crops.size} crops from JSON")
            crops
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse crops data", e)
            emptyList()
        }
    }

    /**
     * Load crops from Firestore
     */
    private suspend fun loadCropsFromFirestore(): List<Crop> {
        return try {
            val snapshot = withTimeout(10000L) { // 10 second timeout
                firestore.collection("crops").get().await()
            }
            val crops = snapshot.documents.mapNotNull { doc ->
                try {
                    val crop = doc.toObject(Crop::class.java)
                    crop?.copy(id = doc.id) ?: null
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Crop", e)
                    null
                }
            }
            Log.d(TAG, "Loaded ${crops.size} crops from Firestore")
            crops
        } catch (e: Exception) {
            Log.e(TAG, "Error loading crops from Firestore", e)
            emptyList()
        }
    }
}