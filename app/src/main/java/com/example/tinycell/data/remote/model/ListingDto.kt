package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.ListingEntity
import com.google.firebase.firestore.PropertyName

/**
 * [FINAL VERSION]: Data Transfer Object for Firestore listings.
 * Updated with PropertyName annotations to ensure Boolean compatibility.
 */
data class ListingDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",
    
    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",
    
    @get:PropertyName("price") @set:PropertyName("price")
    var price: Double = 0.0,
    
    @get:PropertyName("categoryId") @set:PropertyName("categoryId")
    var categoryId: String = "",
    
    @get:PropertyName("imageUrls") @set:PropertyName("imageUrls")
    var imageUrls: List<String> = emptyList(),
    
    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",
    
    @get:PropertyName("sellerName") @set:PropertyName("sellerName")
    var sellerName: String = "",
    
    @get:PropertyName("location") @set:PropertyName("location")
    var location: String? = null,
    
    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),

    // [TODO_COMPATIBILITY]: Ensure SQL 'isSold' (Int 0/1) matches Firestore Boolean
    @get:PropertyName("isSold") @set:PropertyName("isSold")
    var isSold: Boolean = false
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
