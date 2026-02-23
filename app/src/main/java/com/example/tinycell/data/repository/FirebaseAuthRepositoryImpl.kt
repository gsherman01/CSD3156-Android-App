package com.example.tinycell.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirebaseAuthRepo"

/**
 * Firebase Auth Implementation with Admin Controls.
 * [IDENTITY_SWITCH_SUPPORTED]: Synchronizes identity changes with UI and Logs.
 */
class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {

    private var debugUserId: String? = null
    
    private val _userIdFlow = MutableStateFlow<String?>(debugUserId ?: auth.currentUser?.uid)
    override val userIdFlow: StateFlow<String?> = _userIdFlow.asStateFlow()

    /**
     * Returns the Active ID (Debug override or Firebase UID).
     */
    override fun getCurrentUserId(): String? {
        return debugUserId ?: auth.currentUser?.uid
    }

    override fun getCurrentUserName(): String? {
        val firebaseName = auth.currentUser?.displayName
        if (!firebaseName.isNullOrBlank()) return firebaseName
        
        val id = getCurrentUserId() ?: "unknown"
        return "User_${id.takeLast(4).uppercase()}"
    }

    override suspend fun signInAnonymously(): Result<Unit> = try {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
        updateFlow()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun signOut() {
        auth.signOut()
        debugUserId = null
        updateFlow()
    }

    override suspend fun updateUserName(newName: String) {
        val user = auth.currentUser ?: return
        val profileUpdates = userProfileChangeRequest {
            displayName = newName
        }
        user.updateProfile(profileUpdates).await()
    }

    /**
     * [IDENTITY_SWITCH]: Overrides the current identity for local operations.
     * Note: Firestore Rules will still see the underlying Firebase Auth token.
     */
    override fun setDebugUserId(id: String?) {
        Log.d(TAG, "Identity Switch: ${getCurrentUserId()} -> $id")
        debugUserId = id
        
        // This notifies the entire app via userIdFlow
        updateFlow()
        
        // Developer Tip: To see this switch in Firebase Console, 
        // you would ideally use FirebaseAnalytics.setUserId(id).
    }

    private fun updateFlow() {
        val activeId = getCurrentUserId()
        _userIdFlow.value = activeId
        Log.i(TAG, "Active App Identity: $activeId")
    }
}
