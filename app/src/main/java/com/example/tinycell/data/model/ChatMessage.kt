package com.example.tinycell.data.model

/**
 * Domain model representing a chat message.
 * Updated to support the Formal Offer System.
 */
data class ChatMessage(
    val id: String,
    val chatRoomId: String,
    val senderId: String,
    val receiverId: String,
    val listingId: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val offerId: String? = null,
    val messageType: String = "TEXT"
)
