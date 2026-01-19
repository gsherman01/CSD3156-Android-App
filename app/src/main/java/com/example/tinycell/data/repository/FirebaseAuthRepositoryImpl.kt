package com.example.tinycell.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * [PHASE 5.6]: Firebase Auth Implementation.
 * Updated with Debug/Admin features for single-device testing.
 */
class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {

    private var debugUserId: String? = null

    override fun getCurrentUserId(): String? {
        // [ADMIN_OVERRIDE]: If a debug ID is set, use it. Otherwise use real Auth.
        return debugUserId ?: auth.currentUser?.uid
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
