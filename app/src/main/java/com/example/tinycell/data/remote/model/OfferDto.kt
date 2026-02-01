package com.example.tinycell.data.remote.model

import com.google.firebase.firestore.PropertyName

/**
 * [PHASE 6]: Formal Offer System.
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
    var status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
)
