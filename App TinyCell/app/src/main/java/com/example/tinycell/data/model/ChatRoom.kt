package com.example.tinycell.data.model

/**
 * Domain model representing a chat room between a buyer and seller for a specific listing.
 */
data class ChatRoom(
    val id: String,
    val listingId: String,
    val buyerId: String,
    val sellerId: String,
    val listingTitle: String,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null
)
