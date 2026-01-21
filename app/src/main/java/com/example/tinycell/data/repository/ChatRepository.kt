package com.example.tinycell.data.repository

import com.example.tinycell.data.model.ChatMessage
import com.example.tinycell.data.model.ChatRoom
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat operations.
 */
interface ChatRepository {
    /**
     * Get or create a chat room for a listing between buyer and seller.
     * Uses deterministic ID: {listingId}_{sortedUserId1}_{sortedUserId2}
     */
    suspend fun getOrCreateChatRoom(
        listingId: String,
        listingTitle: String,
        buyerId: String,
        sellerId: String
    ): ChatRoom

    /**
     * Get real-time messages flow for a chat room.
     */
    fun getMessagesFlow(chatRoomId: String): Flow<List<ChatMessage>>

    /**
     * Send a message in a chat room.
     */
    suspend fun sendMessage(
        chatRoomId: String,
        senderId: String,
        receiverId: String,
        listingId: String,
        message: String
    ): Result<Unit>

    /**
     * Mark messages as read in a chat room.
     */
    suspend fun markMessagesAsRead(chatRoomId: String, receiverId: String)

    /**
     * Generate deterministic chat room ID.
     */
    fun generateChatRoomId(listingId: String, userId1: String, userId2: String): String
}
