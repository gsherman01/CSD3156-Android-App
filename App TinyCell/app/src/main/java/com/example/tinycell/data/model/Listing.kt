package com.example.tinycell.data.model

/**
 * Represents a marketplace listing.
 * Updated to support multiple images for the carousel feature.
 */
data class Listing(
    val id: String,
    val title: String,
    val price: Double,
    val category: String,
    val sellerId: String,
    val sellerName: String,
    val description: String? = null,
    val imageUrls: List<String> = emptyList(), // Changed to List for carousel
    val location: String? = null,
    val isSold: Boolean = false,
    val status: String = "AVAILABLE",
    val createdAt: Long = System.currentTimeMillis()
)
