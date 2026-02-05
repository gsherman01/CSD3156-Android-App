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
 * Handles real-time message streams and chat room management.
 */
class FirestoreChatDataSource(
    private val firestore: FirebaseFirestore
) {
    private val chatRoomsCollection = firestore.collection("chat_rooms")
    private val chatMessagesCollection = firestore.collection("chat_messages")

    /**
     * Get a chat room by its ID.
     */
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
     */
    suspend fun createOrUpdateChatRoom(chatRoom: ChatRoomDto): Result<Unit> {
        return try {
            chatRoomsCollection.document(chatRoom.id).set(chatRoom).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating chat room: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Update the last message in a chat room.
     */
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

    /**
     * Get real-time messages for a chat room.
     */
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
            val docRef = chatMessagesCollection.add(message).await()
            Log.d(TAG, "Message sent with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Mark messages as read in Firestore.
     */
    suspend fun markMessagesAsRead(chatRoomId: String, receiverId: String) {
        try {
            val unreadMessages = chatMessagesCollection
                .whereEqualTo("chatRoomId", chatRoomId)
                .whereEqualTo("receiverId", receiverId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Log.d(TAG, "Marked ${unreadMessages.size()} messages as read")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read: ${e.message}")
        }
    }

    /**
     * Get all chat rooms for a specific listing (for seller to see all inquiries).
     * Returns real-time flow of chat rooms sorted by most recent message.
     */
    fun getChatRoomsForListing(listingId: String): Flow<List<ChatRoomDto>> = callbackFlow {
        Log.d(TAG, "Starting chat rooms listener for listing: $listingId")

        val subscription = chatRoomsCollection
            .whereEqualTo("listingId", listingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Chat rooms listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val chatRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatRoomDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                // Sort by most recent message (nulls last)
                val sortedRooms = chatRooms.sortedByDescending { it.lastMessageTimestamp ?: 0L }

                Log.d(TAG, "Received ${sortedRooms.size} chat rooms for listing $listingId")
                trySend(sortedRooms)
            }

        awaitClose {
            Log.d(TAG, "Closing chat rooms listener for listing: $listingId")
            subscription.remove()
        }
    }

    /**
     * Get all chat rooms where the user is either a buyer or seller.
     * Used for the "All Chats" screen.
     * Returns real-time flow of chat rooms sorted by most recent message.
     */
    fun getAllChatRoomsForUser(userId: String): Flow<List<ChatRoomDto>> = callbackFlow {
        Log.d(TAG, "Starting all chat rooms listener for user: $userId")

        // Listen to all chat rooms and filter client-side for this user
        // This approach is simpler than managing multiple Firestore queries
        val subscription = chatRoomsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Chat rooms listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val allRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatRoomDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                // Filter rooms where user is either buyer or seller
                val userRooms = allRooms.filter { room ->
                    room.buyerId == userId || room.sellerId == userId
                }

                // Sort by most recent message
                val sortedRooms = userRooms.sortedByDescending { it.lastMessageTimestamp ?: 0L }

                Log.d(TAG, "Received ${sortedRooms.size} chat rooms for user $userId")
                trySend(sortedRooms)
            }

        awaitClose {
            Log.d(TAG, "Closing all chat rooms listener for user: $userId")
            subscription.remove()
        }
    }

    /**
     * Get unread message count for a specific chat room and user.
     * Returns real-time flow of unread count.
     */
    fun getUnreadMessageCount(chatRoomId: String, userId: String): Flow<Int> = callbackFlow {
        val subscription = chatMessagesCollection
            .whereEqualTo("chatRoomId", chatRoomId)
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Unread count listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val count = snapshot?.size() ?: 0
                trySend(count)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Legacy method: Get messages between two users for a specific listing.
     * Kept for backward compatibility.
     */
    fun getMessages(listingId: String, user1: String, user2: String): Flow<List<ChatMessageDto>> = callbackFlow {
        val subscription = chatMessagesCollection
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
}
