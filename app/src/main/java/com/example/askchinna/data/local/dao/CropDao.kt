/**
 * File: app/src/main/java/com/example/askchinna/data/local/dao/CropDao.kt
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
import com.example.askchinna.data.local.entity.CropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM crops")
    suspend fun getAllCrops(): List<CropEntity>

    @Query("SELECT * FROM crops")
    fun getAllCropsFlow(): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE id = :cropId")
    suspend fun getCropById(cropId: String): CropEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity): Long

    @Update
    suspend fun updateCrop(crop: CropEntity)

    @Delete
    suspend fun deleteCrop(crop: CropEntity)

    @Query("DELETE FROM crops")
    suspend fun deleteAllCrops()
}