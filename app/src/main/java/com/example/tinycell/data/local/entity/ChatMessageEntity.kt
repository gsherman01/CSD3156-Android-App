package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for ChatMessage table.
 * Supports Text, Offers, and Image messages.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["senderId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["receiverId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ListingEntity::class, parentColumns = ["id"], childColumns = ["listingId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["senderId"]), Index(value = ["receiverId"]), Index(value = ["listingId"]),
        Index(value = ["chatRoomId"]), Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
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
