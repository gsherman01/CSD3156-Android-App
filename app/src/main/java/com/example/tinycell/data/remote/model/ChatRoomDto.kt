package com.example.tinycell.data.remote.model

import com.example.tinycell.data.model.ChatRoom

/**
 * Data Transfer Object for Firestore chat rooms.
 */
data class ChatRoomDto(
    val id: String = "",
    val listingId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val listingTitle: String = "",
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null
)

fun ChatRoomDto.toDomain() = ChatRoom(
    id = id,
    listingId = listingId,
    buyerId = buyerId,
    sellerId = sellerId,
    listingTitle = listingTitle,
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp
)

fun ChatRoom.toDto() = ChatRoomDto(
    id = id,
    listingId = listingId,
    buyerId = buyerId,
    sellerId = sellerId,
    listingTitle = listingTitle,
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp
)
