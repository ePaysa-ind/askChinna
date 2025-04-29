/**
 * File: app/src/main/java/com/example/askchinna/data/local/AppDatabase.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.askchinna.data.local.converter.DateConverter
import com.example.askchinna.data.local.converter.ListConverter
import com.example.askchinna.data.local.dao.CropDao
import com.example.askchinna.data.local.dao.IdentificationResultDao
import com.example.askchinna.data.local.dao.UserDao
import com.example.askchinna.data.local.entity.CropEntity
import com.example.askchinna.data.local.entity.IdentificationResultEntity
import com.example.askchinna.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room database for the askChinna app
 * Provides local persistent storage with offline support
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

    companion object {
        private const val DATABASE_NAME = "askchinna-db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                buildDatabase(context, scope).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // Only for development
                .addCallback(AppDatabaseCallback(scope))
                .build()
        }
    }

    /**
     * Database callback to handle creation and opening of database
     */
    class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Initialize database with required data in a background coroutine
            scope.launch(Dispatchers.IO) {
                // Database creation operations can be done here if needed
            }
        }
    }
}

/**
 * Data Access Object (DAO) interfaces for database entities
 */

/**
 * DAO for User operations
 */
package com.example.askchinna.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.askchinna.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

/**
 * DAO for Crop operations
 */
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

/**
 * DAO for IdentificationResult operations
 */
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

/**
 * Type converters for Room
 */

/**
 * Converter for Date objects
 */
package com.example.askchinna.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

/**
 * Converter for List objects
 */
class ListConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}

/**
 * Database entities
 */

/**
 * User entity for Room database
 */
package com.example.askchinna.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phoneNumber: String,
    val registrationDate: Date,
    val usageCount: Int = 0,
    val lastUsageUpdate: Date = Date()
)

/**
 * Crop entity for Room database
 */
@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val scientificName: String,
    val iconResName: String
)

/**
 * IdentificationResult entity for Room database
 */
@Entity(tableName = "identification_results")
data class IdentificationResultEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val cropId: String,
    val cropName: String,
    val imageUrl: String,
    val problemName: String,
    val description: String,
    val severity: Int,
    val confidence: Float,
    val actionsList: List<String>,
    val timestamp: Date,
    val feedbackRating: Int? = null,
    val feedbackComments: String? = null,
    val isSyncedToCloud: Boolean = false
)package com.example.askchinna.data.local

