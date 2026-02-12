package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.ChatMessageEntity
import com.example.tinycell.data.model.ChatMessage
import com.google.firebase.firestore.PropertyName

/**
 * Data Transfer Object for Firestore chat messages.
 * Updated to support Image messages.
 */
data class ChatMessageDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("chatRoomId") @set:PropertyName("chatRoomId")
    var chatRoomId: String = "",
    
    @get:PropertyName("senderId") @set:PropertyName("senderId")
    var senderId: String = "",
    
    @get:PropertyName("receiverId") @set:PropertyName("receiverId")
    var receiverId: String = "",
    
    @get:PropertyName("listingId") @set:PropertyName("listingId")
    var listingId: String = "",
    
    @get:PropertyName("message") @set:PropertyName("message")
    var message: String = "",
    
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis(),
    
    @get:PropertyName("read") @set:PropertyName("read")
    var isRead: Boolean = false,
    
    @get:PropertyName("offerId") @set:PropertyName("offerId")
    var offerId: String? = null,
    
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl")
    var imageUrl: String? = null, // Added for image support
    
    @get:PropertyName("messageType") @set:PropertyName("messageType")
    var messageType: String = "TEXT"
)

fun ChatMessageDto.toEntity() = ChatMessageEntity(
    id = id,
    chatRoomId = chatRoomId,
    senderId = senderId,
    receiverId = receiverId,
    listingId = listingId,
    message = message,
    timestamp = timestamp,
    isRead = isRead,
    offerId = offerId,
    imageUrl = imageUrl,
    messageType = messageType
)

fun ChatMessageEntity.toDto() = ChatMessageDto(
    id = id,
    chatRoomId = chatRoomId,
    senderId = senderId,
    receiverId = receiverId,
    listingId = listingId,
    message = message,
    timestamp = timestamp,
    isRead = isRead,
    offerId = offerId,
    imageUrl = imageUrl,
    messageType = messageType
)

fun ChatMessageDto.toDomain() = ChatMessage(
    id = id,
    chatRoomId = chatRoomId,
    senderId = senderId,
    receiverId = receiverId,
    listingId = listingId,
    message = message,
    timestamp = timestamp,
    isRead = isRead,
    offerId = offerId,
    imageUrl = imageUrl,
    messageType = messageType
)
