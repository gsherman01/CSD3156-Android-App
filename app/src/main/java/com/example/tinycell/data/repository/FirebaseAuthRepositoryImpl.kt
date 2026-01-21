package com.example.tinycell.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * [PHASE 5.8]: Firebase Auth Implementation with Admin Controls.
 */
class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {

    private var debugUserId: String? = null

    override fun getCurrentUserId(): String? {
        return debugUserId ?: auth.currentUser?.uid
    }

    override fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName ?: "User_${getCurrentUserId()?.takeLast(4)}"
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
