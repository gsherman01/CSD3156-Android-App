package com.example.tinycell.data.model

/**
 * Represents a marketplace listing.
 * Updated to include location for Carousell-like experience.
 */
data class Listing(
    val id: String,
    val title: String,
    val price: Double,
    val category: String,
    val sellerId: String,
    val sellerName: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val location: String? = null,    // Added location field
    val isSold: Boolean = false,
    val status: String = "AVAILABLE"
)
