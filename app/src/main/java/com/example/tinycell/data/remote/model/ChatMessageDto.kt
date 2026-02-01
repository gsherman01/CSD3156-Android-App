package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.ChatMessageEntity
import com.example.tinycell.data.model.ChatMessage
import com.google.firebase.firestore.PropertyName

/**
 * Data Transfer Object for Firestore chat messages.
 * Updated with PropertyName annotations to handle Boolean naming mismatches.
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
    
    // [FIX]: Explicitly map 'read' from Firestore to 'isRead' in Kotlin
    @get:PropertyName("read") @set:PropertyName("read")
    var isRead: Boolean = false,
    
    @get:PropertyName("offerId") @set:PropertyName("offerId")
    var offerId: String? = null,
    
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
    messageType = messageType
)
