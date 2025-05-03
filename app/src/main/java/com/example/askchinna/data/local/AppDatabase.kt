/**
 * file path: app/src/main/java/com/example/askchinna/data/local/AppDatabase.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30,2025
 * Updated: May 2, 2025
 * Version: 1.2
 * Description: Room database for the askChinna app
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
                .fallbackToDestructiveMigration(false) // Only for development
                .addCallback(AppDatabaseCallback(scope))
                .build()
        }
    }

    /**
     * Database callback to handle creation and opening of database
     */
    class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Initialize database with required data in a background coroutine
            scope.launch(Dispatchers.IO) {
                // Database creation operations can be done here if needed
            }
        }
    }
}