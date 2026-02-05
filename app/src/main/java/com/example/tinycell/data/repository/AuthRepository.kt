package com.example.tinycell.data.repository

/**
 * [PHASE 5.6]: Simple Auth Repository Interface.
 * Updated to support user switching and admin debugging.
 */
interface AuthRepository {
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
