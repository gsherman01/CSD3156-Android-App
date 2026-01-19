package com.example.tinycell.data.remote.datasource

import com.example.tinycell.data.remote.model.ChatMessageDto
import com.example.tinycell.data.local.entity.ChatMessageEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * [PHASE 2]: Firestore Remote Data Source for Chats.
 * Handles real-time message streams.
 */
class FirestoreChatDataSource(
    private val firestore: FirebaseFirestore
) {
    private val chatsCollection = firestore.collection("chats")

    /**
     * Listens for messages between two users regarding a specific listing.
     */
    fun getMessages(listingId: String, user1: String, user2: String): Flow<List<ChatMessageDto>> = callbackFlow {
        val subscription = chatsCollection
            .whereEqualTo("listingId", listingId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessageDto::class.java)?.copy(id = doc.id)
                }?.filter { msg ->
                    (msg.senderId == user1 && msg.receiverId == user2) ||
                    (msg.senderId == user2 && msg.receiverId == user1)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun sendMessage(message: ChatMessageDto) {
        chatsCollection.add(message).await()
    }
}
