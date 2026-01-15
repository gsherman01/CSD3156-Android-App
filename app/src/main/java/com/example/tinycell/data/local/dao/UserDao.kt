package com.example.tinycell.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tinycell.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User table.
 *
 * Provides CRUD operations for users in the TinyCell marketplace.
 * Uses Flow for observable queries and suspend functions for single operations.
 */
@Dao
interface UserDao {

    /**
     * Insert a new user. Replaces if user with same ID already exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    /**
     * Insert multiple users at once (for seeding/bulk operations).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    /**
     * Get a user by their ID.
     * @return UserEntity if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    /**
     * Get all users as an observable Flow.
     * UI can observe this to reactively update when data changes.
     */
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    /**
     * Update an existing user's information.
     */
    @Update
    suspend fun update(user: UserEntity)

    /**
     * Delete a user from the database.
     * Note: CASCADE deletes will remove their listings/favourites/messages.
     */
    @Delete
    suspend fun delete(user: UserEntity)

    /**
     * Delete all users (useful for testing/reset).
     */
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
