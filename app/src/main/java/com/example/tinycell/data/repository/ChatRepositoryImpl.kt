package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.dao.ChatMessageDao
import com.example.tinycell.data.local.dao.UserDao
import com.example.tinycell.data.local.dao.ListingDao
import com.example.tinycell.data.local.entity.UserEntity
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
    private val chatMessageDao: ChatMessageDao,
    private val userDao: UserDao,
    private val listingDao: ListingDao
) : ChatRepository {

    override suspend fun getOrCreateChatRoom(
        listingId: String,
        listingTitle: String,
        buyerId: String,
        sellerId: String
    ): ChatRoom = withContext(Dispatchers.IO) {
        val chatRoomId = generateChatRoomId(listingId, buyerId, sellerId)
        Log.d(TAG, "Getting or creating chat room: $chatRoomId")

        val existingRoom = firestoreChatDataSource.getChatRoom(chatRoomId)
        if (existingRoom != null) {
            return@withContext existingRoom.toDomain()
        }

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
        return firestoreChatDataSource.getMessagesFlow(chatRoomId)
            .map { dtos ->
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
        val timestamp = System.currentTimeMillis()
        val messageDto = ChatMessageDto(
            id = "",
            chatRoomId = chatRoomId,
            senderId = senderId,
            receiverId = receiverId,
            listingId = listingId,
            message = message,
            timestamp = timestamp,
            isRead = false,
            messageType = "TEXT"
        )

        val result = firestoreChatDataSource.sendMessage(messageDto)
        if (result.isSuccess) {
            firestoreChatDataSource.updateChatRoomLastMessage(chatRoomId, message, timestamp)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    override suspend fun sendOfferMessage(
        chatRoomId: String,
        senderId: String,
        receiverId: String,
        listingId: String,
        amount: Double,
        offerId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val displayMessage = "Offered \$${amount}"
        
        val messageDto = ChatMessageDto(
            id = "",
            chatRoomId = chatRoomId,
            senderId = senderId,
            receiverId = receiverId,
            listingId = listingId,
            message = displayMessage,
            timestamp = timestamp,
            isRead = false,
            offerId = offerId,
            messageType = "OFFER"
        )

        val result = firestoreChatDataSource.sendMessage(messageDto)
        if (result.isSuccess) {
            firestoreChatDataSource.updateChatRoomLastMessage(chatRoomId, displayMessage, timestamp)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to send offer message"))
        }
    }

    override suspend fun markMessagesAsRead(chatRoomId: String, receiverId: String) {
        withContext(Dispatchers.IO) {
            firestoreChatDataSource.markMessagesAsRead(chatRoomId, receiverId)
            chatMessageDao.markChatRoomAsRead(chatRoomId, receiverId)
        }
    }

    override fun getChatRoomsForListing(listingId: String): Flow<List<ChatRoom>> {
        return firestoreChatDataSource.getChatRoomsForListing(listingId)
            .map { dtos -> dtos.map { it.toDomain() } }
    }

    override fun getUnreadCountForChatRoom(chatRoomId: String, userId: String): Flow<Int> {
        return firestoreChatDataSource.getUnreadMessageCount(chatRoomId, userId)
    }

    override fun generateChatRoomId(listingId: String, userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${listingId}_${sortedIds[0]}_${sortedIds[1]}"
    }

    private suspend fun cacheMessages(messages: List<ChatMessageDto>) {
        try {
            // [FIX]: Ensure Listing and Users exist in Room before caching chat messages
            // to avoid FOREIGN KEY constraint failed (Error 787).
            messages.firstOrNull()?.let { firstMsg ->
                ensureContextExists(firstMsg.listingId, firstMsg.senderId, firstMsg.receiverId)
            }
            
            val entities = messages.map { it.toEntity() }
            chatMessageDao.insertAll(entities)
        } catch (e: Exception) {
            Log.e(TAG, "Error caching messages: ${e.message}")
        }
    }

    /**
     * Ensures that the database integrity is maintained before inserting chat messages.
     */
    private suspend fun ensureContextExists(listingId: String, senderId: String, receiverId: String) {
        // Ensure Users exist
        listOf(senderId, receiverId).forEach { uid ->
            if (userDao.getUserById(uid) == null) {
                userDao.insert(UserEntity(id = uid, name = "User_$uid", email = "", createdAt = System.currentTimeMillis()))
            }
        }
        
        // Ensure Listing exists (Note: In a full implementation, we might fetch this from Firestore)
        // For MVP, we check if it exists locally.
        if (listingDao.getListingById(listingId) == null) {
            Log.w(TAG, "Listing $listingId missing from local DB. Chat message caching might still fail if FK is strict.")
        }
    }
}
