package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.ChatMessageEntity
import com.example.tinycell.data.model.ChatMessage

/**
 * Data Transfer Object for Firestore chat messages.
 * Updated to support the Formal Offer System.
 */
data class ChatMessageDto(
    val id: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val listingId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    
    // [PHASE 6]: Formal Offer integration
    val offerId: String? = null,
    val messageType: String = "TEXT" // TEXT, OFFER
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
