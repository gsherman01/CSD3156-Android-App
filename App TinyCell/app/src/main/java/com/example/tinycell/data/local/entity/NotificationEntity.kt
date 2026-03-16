package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for User Notifications (the Bulletin).
 */
@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["timestamp"])]
)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String, // Receiver of the notification
    val title: String,
    val message: String,
    val type: String, // OFFER_MADE, OFFER_ACCEPTED, PRICE_CHANGE, STATUS_CHANGE
    val referenceId: String, // listingId or offerId
    val timestamp: Long,
    val isRead: Boolean = false
)
