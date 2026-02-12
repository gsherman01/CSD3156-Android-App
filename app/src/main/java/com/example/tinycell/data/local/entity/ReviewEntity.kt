package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for User Reviews.
 * A review can be for a user as a Seller or as a Buyer.
 */
@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["reviewerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["revieweeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ListingEntity::class,
            parentColumns = ["id"],
            childColumns = ["listingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["reviewerId"]),
        Index(value = ["revieweeId"]),
        Index(value = ["listingId"])
    ]
)
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val listingId: String,
    val reviewerId: String,
    val revieweeId: String,
    val rating: Int, // 1-5 stars
    val comment: String,
    val timestamp: Long,
    val role: String // "BUYER" or "SELLER" (role of the reviewee in this transaction)
)
