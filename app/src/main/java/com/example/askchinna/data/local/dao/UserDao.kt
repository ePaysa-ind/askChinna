/**
 * File: app/src/main/java/com/example/askchinna/data/local/dao/UserDao.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Updated: May 11, 2025
 * Version: 1.2
 * 
 * Change Log:
 * 1.2 - May 11, 2025
 * - Fixed Room query errors by ensuring column names match UserEntity fields
 * - Updated documentation
 * 
 * Description: Data Access Object for user-related database operations
 * Provides CRUD operations for user entities with proper error handling
 */

package com.example.askchinna.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.askchinna.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    /**
     * Retrieves a user by their unique ID
     * 
     * @param userId The unique identifier of the user
     * @return UserEntity if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    /**
     * Retrieves a user by their phone number
     * 
     * @param phoneNumber The phone number to search for
     * @return UserEntity if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE mobileNumber = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserEntity?

    /**
     * Observes user data changes for a specific user
     * 
     * @param userId The unique identifier of the user
     * @return Flow of UserEntity updates
     */
    @Query("SELECT * FROM users WHERE uid = :userId")
    fun observeUserById(userId: String): Flow<UserEntity?>

    /**
     * Inserts a new user or replaces existing one
     * 
     * @param user The user entity to insert
     * @return The row ID of the inserted user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    /**
     * Updates an existing user
     * 
     * @param user The user entity to update
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Deletes a user
     * 
     * @param user The user entity to delete
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)

    /**
     * Bulk insert users with transaction support
     * 
     * @param users List of user entities to insert
     * @return List of row IDs for inserted users
     */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>): List<Long>

    /**
     * Bulk update users with transaction support
     * 
     * @param users List of user entities to update
     */
    @Transaction
    @Update
    suspend fun updateUsers(users: List<UserEntity>)

    /**
     * Bulk delete users with transaction support
     * 
     * @param users List of user entities to delete
     */
    @Transaction
    @Delete
    suspend fun deleteUsers(users: List<UserEntity>)
}