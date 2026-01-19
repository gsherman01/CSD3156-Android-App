package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.ListingEntity

/**
 * Data Transfer Object for Firestore listings.
 * Uses flat structure and simple types for easy console preview and sync.
 */
data class ListingDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val categoryId: String = "",
    val imageUrls: List<String> = emptyList(),
    val userId: String = "",
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
    categoryId = categoryId,
    location = location,
    // Convert List to CSV for Room
    imageUrls = imageUrls.joinToString(","),
    createdAt = createdAt,
    isSold = isSold
)

fun ListingEntity.toDto() = ListingDto(
    id = id,
    title = title,
    description = description,
    price = price,
    categoryId = categoryId,
    // Convert CSV back to List for Firestore/UI
    imageUrls = if (imageUrls.isEmpty()) emptyList() else imageUrls.split(","),
    userId = userId,
    location = location,
    createdAt = createdAt,
    isSold = isSold
)
