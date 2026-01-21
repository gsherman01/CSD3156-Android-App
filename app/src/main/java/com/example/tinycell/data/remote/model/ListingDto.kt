package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.ListingEntity

/**
 * [FINAL VERSION]: Data Transfer Object for Firestore listings.
 */
data class ListingDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val categoryId: String = "",
    val imageUrls: List<String> = emptyList(),
    val userId: String = "",
    val sellerName: String = "", // Denormalized for Firestore browsing
    val location: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSold: Boolean = false
)

fun ListingDto.toEntity() = ListingEntity(
    id = id,
    title = title,
    description = description,
    price = price,
    userId = userId,
    sellerName = sellerName,
    categoryId = categoryId,
    location = location,
    imageUrls = imageUrls.joinToString(","),
    createdAt = createdAt,
    isSold = isSold
)
