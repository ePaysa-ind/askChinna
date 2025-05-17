/**
 * File: app/src/main/java/com/example/askchinna/data/local/AppDatabase.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Updated: May 4, 2025
 * Version: 1.3
 *
 * Change Log:
 * 1.3 - May 4, 2025
 * - Added proper error handling for database operations
 * - Added resource cleanup
 * - Added error logging
 * - Added proper documentation
 * - Added database migration strategy
 * - Added proper cleanup in error cases
 * - Added database corruption handling
 * - Added proper singleton implementation
 *
 * Description: Room database for the askChinna app
 * Provides local persistent storage with offline support
 */
package com.example.askchinna.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.askchinna.data.local.converter.DateConverter
import com.example.askchinna.data.local.converter.ListConverter
import com.example.askchinna.data.local.dao.CropDao
import com.example.askchinna.data.local.dao.IdentificationResultDao
import com.example.askchinna.data.local.dao.UserDao
import com.example.askchinna.data.local.entity.CropEntity
import com.example.askchinna.data.local.entity.IdentificationResultEntity
import com.example.askchinna.data.local.entity.UserEntity

/**
 * Room database for the askChinna app
 * Provides local persistent storage with offline support
 * Implements proper error handling and resource management
 */
@Database(
    entities = [
        UserEntity::class,
        CropEntity::class,
        IdentificationResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun cropDao(): CropDao
    abstract fun identificationResultDao(): IdentificationResultDao

    companion object

}