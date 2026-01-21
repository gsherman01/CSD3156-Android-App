package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.ChatMessageEntity
import com.example.tinycell.data.model.ChatMessage

/**
 * Data Transfer Object for Firestore chat messages.
 */
data class ChatMessageDto(
    val id: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val listingId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

fun ChatMessageDto.toEntity() = ChatMessageEntity(
    id = id,
    chatRoomId = chatRoomId,
    senderId = senderId,
    receiverId = receiverId,
    listingId = listingId,
    message = message,
    timestamp = timestamp,
    isRead = isRead
)

fun ChatMessageEntity.toDto() = ChatMessageDto(
    id = id,
    chatRoomId = chatRoomId,
    senderId = senderId,
    receiverId = receiverId,
    listingId = listingId,
    message = message,
    timestamp = timestamp,
    isRead = isRead
)

fun ChatMessageDto.toDomain() = ChatMessage(
    id = id,
    chatRoomId = chatRoomId,
    senderId = senderId,
    receiverId = receiverId,
    listingId = listingId,
    message = message,
    timestamp = timestamp,
    isRead = isRead
)

fun ChatMessage.toDto() = ChatMessageDto(
    id = id,
    chatRoomId = chatRoomId,
    senderId = senderId,
    receiverId = receiverId,
    listingId = listingId,
    message = message,
    timestamp = timestamp,
    isRead = isRead
)
