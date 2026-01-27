package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.dao.ChatMessageDao
import com.example.tinycell.data.model.ChatMessage
import com.example.tinycell.data.model.ChatRoom
import com.example.tinycell.data.remote.datasource.FirestoreChatDataSource
import com.example.tinycell.data.remote.model.ChatMessageDto
import com.example.tinycell.data.remote.model.ChatRoomDto
import com.example.tinycell.data.remote.model.toDomain
import com.example.tinycell.data.remote.model.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val TAG = "ChatRepositoryImpl"

/**
 * Implementation of ChatRepository combining Firestore and local Room database.
 */
class ChatRepositoryImpl(
    private val firestoreChatDataSource: FirestoreChatDataSource,
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {

    override suspend fun getOrCreateChatRoom(
        listingId: String,
        listingTitle: String,
        buyerId: String,
        sellerId: String
    ): ChatRoom = withContext(Dispatchers.IO) {
        val chatRoomId = generateChatRoomId(listingId, buyerId, sellerId)
        Log.d(TAG, "Getting or creating chat room: $chatRoomId")

        // Check if chat room exists in Firestore
        val existingRoom = firestoreChatDataSource.getChatRoom(chatRoomId)

        if (existingRoom != null) {
            Log.d(TAG, "Found existing chat room: $chatRoomId")
            return@withContext existingRoom.toDomain()
        }

        // Create new chat room
        Log.d(TAG, "Creating new chat room: $chatRoomId")
        val newRoom = ChatRoomDto(
            id = chatRoomId,
            listingId = listingId,
            buyerId = buyerId,
            sellerId = sellerId,
            listingTitle = listingTitle,
            lastMessage = null,
            lastMessageTimestamp = null
        )

        firestoreChatDataSource.createOrUpdateChatRoom(newRoom)

        newRoom.toDomain()
    }

    override fun getMessagesFlow(chatRoomId: String): Flow<List<ChatMessage>> {
        Log.d(TAG, "Getting messages flow for room: $chatRoomId")
        // Use Firestore as the source of truth for real-time updates
        return firestoreChatDataSource.getMessagesFlow(chatRoomId)
            .map { dtos ->
                // Cache messages locally for offline access
                cacheMessages(dtos)
                dtos.map { it.toDomain() }
            }
    }

    override suspend fun sendMessage(
        chatRoomId: String,
        senderId: String,
        receiverId: String,
        listingId: String,
        message: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Sending message in room: $chatRoomId")

        val timestamp = System.currentTimeMillis()
        val messageDto = ChatMessageDto(
            id = "", // Will be assigned by Firestore
            chatRoomId = chatRoomId,
            senderId = senderId,
            receiverId = receiverId,
            listingId = listingId,
            message = message,
            timestamp = timestamp,
            isRead = false
        )

        // Send to Firestore
        val result = firestoreChatDataSource.sendMessage(messageDto)

        if (result.isSuccess) {
            // Update chat room's last message
            firestoreChatDataSource.updateChatRoomLastMessage(
                chatRoomId = chatRoomId,
                message = message,
                timestamp = timestamp
            )
            Log.d(TAG, "Message sent successfully")
            Result.success(Unit)
        } else {
            Log.e(TAG, "Failed to send message")
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    override suspend fun markMessagesAsRead(chatRoomId: String, receiverId: String) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Marking messages as read in room: $chatRoomId for receiver: $receiverId")
            // Update Firestore
            firestoreChatDataSource.markMessagesAsRead(chatRoomId, receiverId)
            // Update local cache
            chatMessageDao.markChatRoomAsRead(chatRoomId, receiverId)
        }
    }

    override fun getChatRoomsForListing(listingId: String): Flow<List<ChatRoom>> {
        Log.d(TAG, "Getting chat rooms for listing: $listingId")
        return firestoreChatDataSource.getChatRoomsForListing(listingId)
            .map { dtos -> dtos.map { it.toDomain() } }
    }

    override fun getUnreadCountForChatRoom(chatRoomId: String, userId: String): Flow<Int> {
        return firestoreChatDataSource.getUnreadMessageCount(chatRoomId, userId)
    }

    override fun generateChatRoomId(listingId: String, userId1: String, userId2: String): String {
        // Sort user IDs to ensure deterministic ID regardless of who initiates
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${listingId}_${sortedIds[0]}_${sortedIds[1]}"
    }

    private suspend fun cacheMessages(messages: List<ChatMessageDto>) {
        try {
            val entities = messages.map { it.toEntity() }
            chatMessageDao.insertAll(entities)
        } catch (e: Exception) {
            Log.e(TAG, "Error caching messages: ${e.message}")
        }
    }
}
