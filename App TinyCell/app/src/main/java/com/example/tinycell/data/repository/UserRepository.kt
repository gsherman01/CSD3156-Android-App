package com.example.tinycell.data.repository

import com.example.tinycell.data.local.dao.UserDao
import com.example.tinycell.data.local.entity.UserEntity
import com.example.tinycell.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * USER REPOSITORY
 *
 * Repository pattern implementation for user data.
 * Handles user CRUD operations using Room database.
 *
 * Future: Add authentication and profile management features.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Get all users as reactive Flow.
     */
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
        .map { entities -> entities.map { it.toUser() } }

    /**
     * Get a user by their ID.
     */
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toUser()
    }

    /**
     * Insert a new user.
     */
    suspend fun insertUser(user: User) {
        userDao.insert(user.toEntity())
    }

    /**
     * Insert multiple users (for seeding/bulk operations).
     */
    suspend fun insertUsers(users: List<User>) {
        userDao.insertAll(users.map { it.toEntity() })
    }

    /**
     * Update an existing user's information.
     */
    suspend fun updateUser(user: User) {
        userDao.update(user.toEntity())
    }

    /**
     * Delete a user.
     * Note: This will CASCADE delete their listings, favourites, and messages.
     */
    suspend fun deleteUser(user: User) {
        userDao.delete(user.toEntity())
    }
}

/**
 * Extension function: Convert UserEntity (database) to User (UI model).
 */
private fun UserEntity.toUser(): User {
    return User(
        id = id,
        username = name  // Map 'name' to 'username' for UI model
    )
}

/**
 * Extension function: Convert User (UI model) to UserEntity (database).
 */
private fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = username,  // Map 'username' to 'name' for database
        email = "$username@tinycell.sg",  // Generate email if not provided (temporary)
        profilePicUrl = null,
        createdAt = System.currentTimeMillis()
    )
}
