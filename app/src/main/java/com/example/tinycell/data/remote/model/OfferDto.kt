package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.OfferEntity
import com.google.firebase.firestore.PropertyName

/**
 * [PHASE 6]: Formal Offer System.
 * Valid status values: SENT, ACCEPTED, REJECTED.
 */
data class OfferDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("listingId") @set:PropertyName("listingId")
    var listingId: String = "",

    @get:PropertyName("buyerId") @set:PropertyName("buyerId")
    var buyerId: String = "",

    @get:PropertyName("sellerId") @set:PropertyName("sellerId")
    var sellerId: String = "",

    @get:PropertyName("amount") @set:PropertyName("amount")
    var amount: Double = 0.0,

    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = "SENT", // SENT, ACCEPTED, REJECTED

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
)

/** Maps a Firestore DTO down to the Room entity. */
fun OfferDto.toEntity() = OfferEntity(
    id = id,
    listingId = listingId,
    buyerId = buyerId,
    sellerId = sellerId,
    amount = amount,
    status = status,
    timestamp = timestamp
)

/** Maps a Room entity up to the Firestore DTO. */
fun OfferEntity.toDto() = OfferDto(
    id = id,
    listingId = listingId,
    buyerId = buyerId,
    sellerId = sellerId,
    amount = amount,
    status = status,
    timestamp = timestamp
)
