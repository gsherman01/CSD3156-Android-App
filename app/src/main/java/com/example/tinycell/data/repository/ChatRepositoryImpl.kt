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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

private const val TAG = "ChatRepositoryImpl"

/**
 * Implementation of ChatRepository with support for Images and Identity Switching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryImpl(
    private val firestoreChatDataSource: FirestoreChatDataSource,
    private val chatMessageDao: ChatMessageDao,
    private val userDao: UserDao,
    private val listingDao: ListingDao,
    private val remoteListingRepository: RemoteListingRepository,
    private val remoteImageRepository: RemoteImageRepository,
    private val authRepository: AuthRepository // Added for Identity Switching support
) : ChatRepository {

    override suspend fun getOrCreateChatRoom(
        listingId: String, listingTitle: String, buyerId: String, sellerId: String
    ): ChatRoom = withContext(Dispatchers.IO) {
        val chatRoomId = generateChatRoomId(listingId, buyerId, sellerId)
        val existingRoom = firestoreChatDataSource.getChatRoom(chatRoomId)
        if (existingRoom != null) return@withContext existingRoom.toDomain()

        val newRoom = ChatRoomDto(
            id = chatRoomId, listingId = listingId, buyerId = buyerId, sellerId = sellerId,
            listingTitle = listingTitle, lastMessage = null, lastMessageTimestamp = null
        )
        firestoreChatDataSource.createOrUpdateChatRoom(newRoom)
        newRoom.toDomain()
    }

    override fun getMessagesFlow(chatRoomId: String): Flow<List<ChatMessage>> {
        return firestoreChatDataSource.getMessagesFlow(chatRoomId).map { dtos ->
            cacheMessages(dtos)
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun sendMessage(
        chatRoomId: String, senderId: String, receiverId: String, listingId: String, message: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        
        // Ensure we use the Active Identity (important for Admin Switch)
        val activeSenderId = authRepository.getCurrentUserId() ?: senderId
        
        val messageDto = ChatMessageDto(
            id = "", chatRoomId = chatRoomId, senderId = activeSenderId, receiverId = receiverId,
            listingId = listingId, message = message, timestamp = timestamp, isRead = false, messageType = "TEXT"
        )
        val result = firestoreChatDataSource.sendMessage(messageDto)
        if (result.isSuccess) {
            firestoreChatDataSource.updateChatRoomLastMessage(chatRoomId, message, timestamp)
            Result.success(Unit)
        } else Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
    }

    override suspend fun sendImageMessage(
        chatRoomId: String, senderId: String, receiverId: String, listingId: String, imagePath: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val activeSenderId = authRepository.getCurrentUserId() ?: senderId
            val uploadResult = remoteImageRepository.uploadImages(listOf(imagePath))
            val imageUrl = uploadResult.getOrThrow().first()
            val timestamp = System.currentTimeMillis()
            val messageDto = ChatMessageDto(
                id = "", chatRoomId = chatRoomId, senderId = activeSenderId, receiverId = receiverId,
                listingId = listingId, message = "📷 Image", timestamp = timestamp,
                isRead = false, imageUrl = imageUrl, messageType = "IMAGE"
            )
            val result = firestoreChatDataSource.sendMessage(messageDto)
            if (result.isSuccess) {
                firestoreChatDataSource.updateChatRoomLastMessage(chatRoomId, "📷 Image", timestamp)
                Result.success(Unit)
            } else Result.failure(result.exceptionOrNull() ?: Exception("Failed to send image"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun sendOfferMessage(
        chatRoomId: String, senderId: String, receiverId: String, listingId: String, amount: Double, offerId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val activeSenderId = authRepository.getCurrentUserId() ?: senderId
        val timestamp = System.currentTimeMillis()
        val displayMessage = "Offered \$${"%.2f".format(amount)}"
        val messageDto = ChatMessageDto(
            id = "", chatRoomId = chatRoomId, senderId = activeSenderId, receiverId = receiverId,
            listingId = listingId, message = displayMessage, timestamp = timestamp,
            isRead = false, offerId = offerId, messageType = "OFFER"
        )
        val result = firestoreChatDataSource.sendMessage(messageDto)
        if (result.isSuccess) {
            firestoreChatDataSource.updateChatRoomLastMessage(chatRoomId, displayMessage, timestamp)
            Result.success(Unit)
        } else Result.failure(result.exceptionOrNull() ?: Exception("Failed to send offer"))
    }

    override suspend fun markMessagesAsRead(chatRoomId: String, receiverId: String) {
        withContext(Dispatchers.IO) {
            firestoreChatDataSource.markMessagesAsRead(chatRoomId, receiverId)
            chatMessageDao.markChatRoomAsRead(chatRoomId, receiverId)
        }
    }

    override fun getChatRoomsForListing(listingId: String): Flow<List<ChatRoom>> = firestoreChatDataSource.getChatRoomsForListing(listingId).map { dtos -> dtos.map { it.toDomain() } }
    override fun getAllChatRoomsForUser(userId: String): Flow<List<ChatRoom>> = firestoreChatDataSource.getAllChatRoomsForUser(userId).map { dtos -> dtos.map { it.toDomain() } }
    override fun getUnreadCountForChatRoom(chatRoomId: String, userId: String): Flow<Int> = firestoreChatDataSource.getUnreadMessageCount(chatRoomId, userId)
    
    override fun getTotalUnreadCount(userId: String): Flow<Int> {
        return firestoreChatDataSource.getAllChatRoomsForUser(userId).flatMapLatest { rooms ->
            if (rooms.isEmpty()) {
                flowOf(0)
            } else {
                val flows = rooms.map { room ->
                    firestoreChatDataSource.getUnreadMessageCount(room.id, userId)
                }
                combine(flows) { counts -> counts.sum() }
            }
        }
    }

    override fun generateChatRoomId(listingId: String, userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${listingId}_${sortedIds[0]}_${sortedIds[1]}"
    }

    private suspend fun cacheMessages(messages: List<ChatMessageDto>) {
        if (messages.isEmpty()) return
        try {
            ensureContextExists(messages.first().listingId, messages.first().senderId, messages.first().receiverId)
            chatMessageDao.insertAll(messages.map { it.toEntity() })
        } catch (e: Exception) { Log.e(TAG, "Error caching messages: ${e.message}") }
    }

    private suspend fun ensureContextExists(listingId: String, senderId: String, receiverId: String) {
        listOf(senderId, receiverId).forEach { uid ->
            if (userDao.getUserById(uid) == null) {
                userDao.insert(UserEntity(id = uid, name = "User_$uid", email = "", createdAt = System.currentTimeMillis()))
            }
        }
        if (listingDao.getListingById(listingId) == null) {
            try {
                remoteListingRepository.getListingById(listingId)?.let { listingDao.insert(it.toEntity()) }
            } catch (e: Exception) { Log.e(TAG, "Failed to cache missing listing $listingId") }
        }
    }
}
