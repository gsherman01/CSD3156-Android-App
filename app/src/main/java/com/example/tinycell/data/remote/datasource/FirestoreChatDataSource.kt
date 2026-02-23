package com.example.tinycell.data.remote.datasource

import android.util.Log
import com.example.tinycell.data.remote.model.ChatMessageDto
import com.example.tinycell.data.remote.model.ChatRoomDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreChatDataSource"

/**
 * Firestore Remote Data Source for Chats.
 * [RELIABILITY UPDATED]: Uses deterministic IDs and awaits creation.
 */
class FirestoreChatDataSource(
    private val firestore: FirebaseFirestore
) {
    private val chatRoomsCollection = firestore.collection("chat_rooms")
    private val chatMessagesCollection = firestore.collection("chat_messages")

    suspend fun getChatRoom(chatRoomId: String): ChatRoomDto? {
        return try {
            val doc = chatRoomsCollection.document(chatRoomId).get().await()
            doc.toObject(ChatRoomDto::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat room: ${e.message}")
            null
        }
    }

    /**
     * Create or update a chat room.
     * [FIX]: Ensures document exists before returning to prevent listener races.
     */
    suspend fun createOrUpdateChatRoom(chatRoom: ChatRoomDto): Result<Unit> {
        return try {
            // Using .set() ensures we use the deterministic chatRoomId
            chatRoomsCollection.document(chatRoom.id).set(chatRoom).await()
            Log.d(TAG, "Chat room ${chatRoom.id} created/updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating chat room: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateChatRoomLastMessage(chatRoomId: String, message: String, timestamp: Long) {
        try {
            chatRoomsCollection.document(chatRoomId).update(
                mapOf(
                    "lastMessage" to message,
                    "lastMessageTimestamp" to timestamp
                )
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating chat room last message: ${e.message}")
        }
    }

    fun getMessagesFlow(chatRoomId: String): Flow<List<ChatMessageDto>> = callbackFlow {
        Log.d(TAG, "Starting messages listener for room: $chatRoomId")

        val subscription = chatMessagesCollection
            .whereEqualTo("chatRoomId", chatRoomId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Messages listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessageDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                Log.d(TAG, "Received ${messages.size} messages for room $chatRoomId")
                trySend(messages)
            }

        awaitClose {
            Log.d(TAG, "Closing messages listener for room: $chatRoomId")
            subscription.remove()
        }
    }

    /**
     * Send a message and return the generated message ID.
     */
    suspend fun sendMessage(message: ChatMessageDto): Result<String> {
        return try {
            // Adding ensures a new document with an auto-id
            val docRef = chatMessagesCollection.add(message).await()
            Log.d(TAG, "Message sent with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun markMessagesAsRead(chatRoomId: String, receiverId: String) {
        try {
            val unreadMessages = chatMessagesCollection
                .whereEqualTo("chatRoomId", chatRoomId)
                .whereEqualTo("receiverId", receiverId)
                .whereEqualTo("read", false)
                .get()
                .await()

            if (unreadMessages.isEmpty) return

            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "read", true)
            }
            batch.commit().await()
            Log.d(TAG, "Marked ${unreadMessages.size()} messages as read")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read: ${e.message}")
        }
    }

    fun getChatRoomsForListing(listingId: String): Flow<List<ChatRoomDto>> = callbackFlow {
        val subscription = chatRoomsCollection
            .whereEqualTo("listingId", listingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val chatRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatRoomDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(chatRooms.sortedByDescending { it.lastMessageTimestamp ?: 0L })
            }
        awaitClose { subscription.remove() }
    }

    fun getAllChatRoomsForUser(userId: String): Flow<List<ChatRoomDto>> = callbackFlow {
        val subscription = chatRoomsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val allRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatRoomDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                val userRooms = allRooms.filter { it.buyerId == userId || it.sellerId == userId }
                trySend(userRooms.sortedByDescending { it.lastMessageTimestamp ?: 0L })
            }
        awaitClose { subscription.remove() }
    }

    fun getUnreadMessageCount(chatRoomId: String, userId: String): Flow<Int> = callbackFlow {
        val subscription = chatMessagesCollection
            .whereEqualTo("chatRoomId", chatRoomId)
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { subscription.remove() }
    }
}
