/**
 * File: app/src/main/java/com/example/askchinna/data/repository/CropRepository.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.data.repository

import android.content.Context
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for crop-related data operations
 * Responsible for loading and providing access to the list of supported crops
 */
@Singleton
class CropRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    // Cache of crops to avoid repeated loading from assets
    private var cachedCrops: List<Crop>? = null

    /**
     * Gets the list of supported crops from a local JSON file
     * Caches results to improve performance on subsequent calls
     *
     * @return List of crops supported by the application
     */
    suspend fun getSupportedCrops(): List<Crop> = withContext(Dispatchers.IO) {
        // Return cached crops if available
        cachedCrops?.let { return@withContext it }

        try {
            // Load crops from JSON file
            val cropsJson = context.resources.openRawResource(R.raw.crops_data)
                .bufferedReader()
                .use { it.readText() }

            // Parse JSON into list of Crop objects
            val cropListType = object : TypeToken<List<Crop>>() {}.type
            val crops = gson.fromJson<List<Crop>>(cropsJson, cropListType)

            // Cache the results
            cachedCrops = crops

            return@withContext crops
        } catch (e: IOException) {
            // If JSON reading fails, return a hardcoded list of the 10 supported crops
            val fallbackCrops = createFallbackCrops()
            cachedCrops = fallbackCrops
            return@withContext fallbackCrops
        }
    }

    /**
     * Creates a fallback list of crops in case the JSON loading fails
     * Contains the 10 crops specified in the project requirements
     */
    private fun createFallbackCrops(): List<Crop> {
        return listOf(
            Crop("chili", "Chili", R.drawable.ic_chili),
            Crop("okra", "Okra", R.drawable.ic_okra),
            Crop("maize", "Maize", R.drawable.ic_maize),
            Crop("cotton", "Cotton", R.drawable.ic_cotton),
            Crop("tomato", "Tomato", R.drawable.ic_tomato),
            Crop("watermelon", "Watermelon", R.drawable.ic_watermelon),
            Crop("soybean", "Soybean", R.drawable.ic_soybean),
            Crop("rice", "Rice", R.drawable.ic_rice),
            Crop("wheat", "Wheat", R.drawable.ic_wheat),
            Crop("pigeon_pea", "Pigeon Pea", R.drawable.ic_pigeon_pea)
        )
    }

    /**
     * Gets a specific crop by its ID
     *
     * @param cropId The unique identifier of the crop
     * @return The crop with the specified ID or null if not found
     */
    suspend fun getCropById(cropId: String): Crop? {
        val crops = getSupportedCrops()
        return crops.find { it.id == cropId }
    }
}
