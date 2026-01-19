package com.example.tinycell.data.remote.datasource

import com.example.tinycell.data.remote.model.UserDto
import com.example.tinycell.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * [PHASE 2]: Firestore Remote Data Source for Users.
 */
class FirestoreUserDataSource(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun fetchUserById(userId: String): User? {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            snapshot.toObject(UserDto::class.java)?.toUser()
        } catch (e: Exception) {
            null
        }
    }

    private fun UserDto.toUser(): User {
        return User(
            id = this.id,
            name = this.name.ifBlank { "Anonymous" },
            email = this.email,
            profilePicUrl = this.profilePicUrl
        )
    }
}
