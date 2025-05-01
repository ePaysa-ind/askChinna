/**
 * File: app/src/main/java/com/example/askchinna/data/local/dao/IdentificationResultDao.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Version: 1.0
 */

package com.example.askchinna.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.askchinna.data.local.entity.IdentificationResultEntity

@Dao
interface IdentificationResultDao {
    @Query("SELECT * FROM identification_results ORDER BY timestamp DESC")
    suspend fun getAllResults(): List<IdentificationResultEntity>

    @Query("SELECT * FROM identification_results WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getResultsByUserId(userId: String): List<IdentificationResultEntity>

    @Query("SELECT * FROM identification_results WHERE id = :resultId")
    suspend fun getResultById(resultId: String): IdentificationResultEntity?

    @Query("SELECT COUNT(*) FROM identification_results WHERE userId = :userId")
    suspend fun getResultCountForUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: IdentificationResultEntity): Long

    @Update
    suspend fun updateResult(result: IdentificationResultEntity)

    @Delete
    suspend fun deleteResult(result: IdentificationResultEntity)

    @Query("DELETE FROM identification_results WHERE userId = :userId")
    suspend fun deleteResultsForUser(userId: String)
}