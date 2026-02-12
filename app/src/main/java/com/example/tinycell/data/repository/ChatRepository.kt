package com.example.tinycell.data.repository

import com.example.tinycell.data.model.ChatMessage
import com.example.tinycell.data.model.ChatRoom
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat operations.
 * Updated to support Global Unread tracking.
 */
interface ChatRepository {
    suspend fun getOrCreateChatRoom(
        listingId: String,
        listingTitle: String,
        buyerId: String,
        sellerId: String
    ): ChatRoom

    fun getMessagesFlow(chatRoomId: String): Flow<List<ChatMessage>>

    suspend fun sendMessage(
        chatRoomId: String,
        senderId: String,
        receiverId: String,
        listingId: String,
        message: String
    ): Result<Unit>

    suspend fun sendImageMessage(
        chatRoomId: String,
        senderId: String,
        receiverId: String,
        listingId: String,
        imagePath: String
    ): Result<Unit>

    suspend fun sendOfferMessage(
        chatRoomId: String,
        senderId: String,
        receiverId: String,
        listingId: String,
        amount: Double,
        offerId: String
    ): Result<Unit>

    suspend fun markMessagesAsRead(chatRoomId: String, receiverId: String)
    fun getChatRoomsForListing(listingId: String): Flow<List<ChatRoom>>
    fun getAllChatRoomsForUser(userId: String): Flow<List<ChatRoom>>
    fun getUnreadCountForChatRoom(chatRoomId: String, userId: String): Flow<Int>
    
    /**
     * [NEW]: Streams the total unread message count for the current user across all rooms.
     */
    fun getTotalUnreadCount(userId: String): Flow<Int>

    fun generateChatRoomId(listingId: String, userId1: String, userId2: String): String
}
