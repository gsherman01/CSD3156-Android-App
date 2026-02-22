package com.example.tinycell.data.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Simple Auth Repository Interface.
 * Updated with userIdFlow for reactive UI updates during user switching.
 */
interface AuthRepository {
    /**
     * Observable stream of the current user ID.
     */
    val userIdFlow: StateFlow<String?>

    fun getCurrentUserId(): String?
    fun getCurrentUserName(): String?
    suspend fun signInAnonymously(): Result<Unit>
    fun signOut()

    /**
     * Update the current user's display name.
     */
    suspend fun updateUserName(newName: String)

    /**
     * [ADMIN_ONLY]: For testing on a single device.
     * Allows overriding the Firebase UID with a custom string.
     */
    fun setDebugUserId(id: String?)
}
