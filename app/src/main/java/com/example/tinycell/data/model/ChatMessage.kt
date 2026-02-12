package com.example.tinycell.data.model

/**
 * Domain model representing a chat message.
 * Supports Text, Offers, and Image messages.
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
    val imageUrl: String? = null, // Added for image support
    val messageType: String = "TEXT" // TEXT, OFFER, IMAGE
)
