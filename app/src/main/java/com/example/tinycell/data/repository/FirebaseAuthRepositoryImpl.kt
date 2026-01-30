package com.example.tinycell.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * [PHASE 5.8]: Firebase Auth Implementation with Admin Controls.
 * Ensuring UIDs and Display Names are consistent for Room and UI.
 */
class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {

    private var debugUserId: String? = null

    /**
     * ALWAYS returns the same ID that Room uses as a Foreign Key.
     */
    override fun getCurrentUserId(): String? {
        return debugUserId ?: auth.currentUser?.uid
    }

    /**
     * Guarantees a non-null display name. 
     * Uses Firebase name if available, otherwise generates one from the UID.
     */
    override fun getCurrentUserName(): String? {
        val firebaseName = auth.currentUser?.displayName
        if (!firebaseName.isNullOrBlank()) return firebaseName
        
        val id = getCurrentUserId() ?: "unknown"
        // Generate a recognizable name like User_7a2b
        return "User_${id.takeLast(4).uppercase()}"
    }

    override suspend fun signInAnonymously(): Result<Unit> = try {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun signOut() {
        auth.signOut()
        debugUserId = null
    }

    override fun setDebugUserId(id: String?) {
        debugUserId = id
    }
}
