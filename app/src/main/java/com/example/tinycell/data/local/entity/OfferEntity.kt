package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [PHASE 6]: Room Entity for Offer system.
 */
@Entity(
    tableName = "offers",
    foreignKeys = [
        ForeignKey(
            entity = ListingEntity::class,
            parentColumns = ["id"],
            childColumns = ["listingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listingId"]), Index(value = ["buyerId"]), Index(value = ["sellerId"])]
)
data class OfferEntity(
    @PrimaryKey
    val id: String,
    val listingId: String,
    val buyerId: String,
    val sellerId: String,
    val amount: Double,
    val status: String, // PENDING, ACCEPTED, REJECTED
    val timestamp: Long
)
