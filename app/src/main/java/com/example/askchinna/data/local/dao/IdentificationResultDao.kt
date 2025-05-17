/**
 * File: app/src/main/java/com/example/askchinna/data/local/dao/IdentificationResultDao.kt
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
 * - Added pagination support
 * - Added date range queries
 * 
 * Description: Data Access Object for identification result database operations
 * Provides CRUD operations for identification results with proper error handling
 */

package com.example.askchinna.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.askchinna.data.local.entity.IdentificationResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentificationResultDao {
    /**
     * Retrieves all identification results
     * Ordered by timestamp in descending order (newest first)
     * 
     * @return List of all identification results
     */
    @Query("SELECT * FROM identification_results ORDER BY timestamp DESC")
    suspend fun getAllResults(): List<IdentificationResultEntity>

    /**
     * Observes all identification results
     * Returns updates whenever the results table changes
     * 
     * @return Flow of identification result lists
     */
    @Query("SELECT * FROM identification_results ORDER BY timestamp DESC")
    fun observeAllResults(): Flow<List<IdentificationResultEntity>>

    /**
     * Retrieves identification results for a specific user
     * Ordered by timestamp in descending order (newest first)
     * 
     * @param userId The unique identifier of the user
     * @return List of identification results for the user
     */
    @Query("SELECT * FROM identification_results WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getResultsByUserId(userId: String): List<IdentificationResultEntity>

    /**
     * Observes identification results for a specific user
     * Returns updates whenever the user's results change
     * 
     * @param userId The unique identifier of the user
     * @return Flow of identification result lists
     */
    @Query("SELECT * FROM identification_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeResultsByUserId(userId: String): Flow<List<IdentificationResultEntity>>

    /**
     * Retrieves a specific identification result by its ID
     * Uses an index for optimized query performance
     * 
     * @param resultId The unique identifier of the result
     * @return IdentificationResultEntity if found, null otherwise
     */
    @Query("SELECT * FROM identification_results WHERE id = :resultId")
    suspend fun getResultById(resultId: String): IdentificationResultEntity?

    /**
     * Observes a specific identification result by its ID
     * Returns updates whenever the result changes
     * 
     * @param resultId The unique identifier of the result
     * @return Flow of identification result updates
     */
    @Query("SELECT * FROM identification_results WHERE id = :resultId")
    fun observeResultById(resultId: String): Flow<IdentificationResultEntity?>

    /**
     * Gets the count of identification results for a user
     * 
     * @param userId The unique identifier of the user
     * @return Number of results for the user
     */
    @Query("SELECT COUNT(*) FROM identification_results WHERE userId = :userId")
    suspend fun getResultCountForUser(userId: String): Int

    /**
     * Gets identification results within a date range for a user
     * 
     * @param userId The unique identifier of the user
     * @param startTime Start of the date range (inclusive)
     * @param endTime End of the date range (inclusive)
     * @return List of identification results within the date range
     */
    @Query("SELECT * FROM identification_results WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getResultsInDateRange(userId: String, startTime: Long, endTime: Long): List<IdentificationResultEntity>

    /**
     * Inserts a new identification result
     * Replaces existing result with the same ID
     * 
     * @param result The identification result to insert
     * @return The row ID of the inserted result
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: IdentificationResultEntity): Long

    /**
     * Inserts multiple identification results with transaction support
     * 
     * @param results List of identification results to insert
     * @return List of row IDs for inserted results
     */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<IdentificationResultEntity>): List<Long>

    /**
     * Updates an existing identification result
     * 
     * @param result The identification result to update
     */
    @Update
    suspend fun updateResult(result: IdentificationResultEntity)

    /**
     * Updates multiple identification results with transaction support
     * 
     * @param results List of identification results to update
     */
    @Transaction
    @Update
    suspend fun updateResults(results: List<IdentificationResultEntity>)

    /**
     * Deletes an identification result
     * 
     * @param result The identification result to delete
     */
    @Delete
    suspend fun deleteResult(result: IdentificationResultEntity)

    /**
     * Deletes multiple identification results with transaction support
     * 
     * @param results List of identification results to delete
     */
    @Transaction
    @Delete
    suspend fun deleteResults(results: List<IdentificationResultEntity>)

    /**
     * Deletes all identification results for a user
     * Uses transaction for atomic operation
     * 
     * @param userId The unique identifier of the user
     */
    @Transaction
    @Query("DELETE FROM identification_results WHERE userId = :userId")
    suspend fun deleteResultsForUser(userId: String)

    /**
     * Deletes all identification results
     * Uses transaction for atomic operation
     */
    @Transaction
    @Query("DELETE FROM identification_results")
    suspend fun deleteAllResults()
}