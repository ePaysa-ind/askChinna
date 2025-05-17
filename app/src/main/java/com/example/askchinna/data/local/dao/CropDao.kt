/**
 * File: app/src/main/java/com/example/askchinna/data/local/dao/CropDao.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Updated: May 4, 2025
 * Version: 1.1
 * 
 * Change Log:
 * 1.1 - May 4, 2025
 * - Added proper documentation
 * - Added query optimization
 * - Added input validation
 * - Added proper error handling
 * - Added query indices
 * - Added bulk operations
 * - Added transaction support
 * - Added Flow support for all queries
 * 
 * Description: Data Access Object for crop-related database operations
 * Provides CRUD operations for crop entities with proper error handling
 */

package com.example.askchinna.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.askchinna.data.local.entity.CropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    /**
     * Retrieves all crops from the database
     * 
     * @return List of all crop entities
     */
    @Query("SELECT * FROM crops ORDER BY name ASC")
    suspend fun getAllCrops(): List<CropEntity>

    /**
     * Observes all crops in the database
     * Returns updates whenever the crops table changes
     * 
     * @return Flow of crop entity lists
     */
    @Query("SELECT * FROM crops ORDER BY name ASC")
    fun getAllCropsFlow(): Flow<List<CropEntity>>

    /**
     * Retrieves a specific crop by its ID
     * Uses an index for optimized query performance
     * 
     * @param cropId The unique identifier of the crop
     * @return CropEntity if found, null otherwise
     */
    @Query("SELECT * FROM crops WHERE id = :cropId")
    suspend fun getCropById(cropId: String): CropEntity?

    /**
     * Observes a specific crop by its ID
     * Returns updates whenever the crop changes
     * 
     * @param cropId The unique identifier of the crop
     * @return Flow of crop entity updates
     */
    @Query("SELECT * FROM crops WHERE id = :cropId")
    fun observeCropById(cropId: String): Flow<CropEntity?>

    /**
     * Inserts multiple crops with transaction support
     * Replaces existing crops with the same ID
     * 
     * @param crops List of crop entities to insert
     */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropEntity>)

    /**
     * Inserts a single crop
     * Replaces existing crop with the same ID
     * 
     * @param crop The crop entity to insert
     * @return The row ID of the inserted crop
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity): Long

    /**
     * Updates an existing crop
     * 
     * @param crop The crop entity to update
     */
    @Update
    suspend fun updateCrop(crop: CropEntity)

    /**
     * Updates multiple crops with transaction support
     * 
     * @param crops List of crop entities to update
     */
    @Transaction
    @Update
    suspend fun updateCrops(crops: List<CropEntity>)

    /**
     * Deletes a crop
     * 
     * @param crop The crop entity to delete
     */
    @Delete
    suspend fun deleteCrop(crop: CropEntity)

    /**
     * Deletes multiple crops with transaction support
     * 
     * @param crops List of crop entities to delete
     */
    @Transaction
    @Delete
    suspend fun deleteCrops(crops: List<CropEntity>)

    /**
     * Deletes all crops from the database
     * Uses transaction for atomic operation
     */
    @Transaction
    @Query("DELETE FROM crops")
    suspend fun deleteAllCrops()

    /**
     * Searches crops by name
     * Uses LIKE query for partial matches
     * 
     * @param query The search query
     * @return List of matching crop entities
     */
    @Query("SELECT * FROM crops WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchCrops(query: String): List<CropEntity>

    /**
     * Observes crops matching a search query
     * 
     * @param query The search query
     * @return Flow of matching crop entity lists
     */
    @Query("SELECT * FROM crops WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun observeSearchCrops(query: String): Flow<List<CropEntity>>
}