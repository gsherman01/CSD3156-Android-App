package com.example.tinycell.data.model

/**
 * Represents a marketplace listing.
 * Designed to support future expansion with images, descriptions, and seller details.
 */
data class Listing(
    val id: String,
    val title: String,
    val price: Double,
    val category: String,
    val sellerId: String,             // Seller's user ID for chat functionality
    val sellerName: String,
    val description: String? = null,  // Optional description for detail screen
    val imageUrl: String? = null,     // Optional image URL (placeholder-ready for AsyncImage)
    val isSold: Boolean = false,      // Listing sold status
    val status: String = "AVAILABLE"  // AVAILABLE, PENDING (Under Offer), SOLD
)
