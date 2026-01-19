package com.example.tinycell.data.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * [PHASE 5.6]: Simple Auth Repository Interface.
 * Updated to support user switching and admin debugging.
 */
interface AuthRepository {
    fun getCurrentUserId(): String?
    
    suspend fun signInAnonymously(): Result<Unit>
    
    /**
     * Signs out the current user.
     */
    fun signOut()

    /**
     * [ADMIN_ONLY]: For testing on a single device.
     * In a real app, this would be restricted.
     */
    fun setDebugUserId(id: String?)
}
