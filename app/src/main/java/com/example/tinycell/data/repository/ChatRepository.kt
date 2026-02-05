package com.example.tinycell.data.repository

import com.example.tinycell.data.model.ChatMessage
import com.example.tinycell.data.model.ChatRoom
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat operations.
 * Updated to support the Formal Offer System.
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

    /**
     * [PHASE 6]: Sends a formal offer as a chat message.
     */
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

    /**
     * Gets all chat rooms where the user is either a buyer or seller.
     * Used for the "All Chats" screen in bottom navigation.
     */
    fun getAllChatRoomsForUser(userId: String): Flow<List<ChatRoom>>

    fun getUnreadCountForChatRoom(chatRoomId: String, userId: String): Flow<Int>
    fun generateChatRoomId(listingId: String, userId1: String, userId2: String): String
}
