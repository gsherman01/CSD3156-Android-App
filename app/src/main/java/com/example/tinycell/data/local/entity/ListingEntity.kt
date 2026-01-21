package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tinycell.data.remote.model.ListingDto

/**
 * [FINAL VERSION]: Room entity for Listing table.
 * Includes denormalized sellerName for performance.
 */
@Entity(
    tableName = "listings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["categoryId"])
    ]
)
data class ListingEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val userId: String,
    val sellerName: String = "", // Denormalized for fast browsing
    val categoryId: String,
    val location: String? = null,
    val imageUrls: String = "", // CSV format for Room
    val createdAt: Long,
    val isSold: Boolean = false
)

fun ListingEntity.toDto() = ListingDto(
    id = id,
    title = title,
    description = description,
    price = price,
    userId = userId,
    sellerName = sellerName,
    categoryId = categoryId,
    location = location,
    imageUrls = if (imageUrls.isEmpty()) emptyList() else imageUrls.split(","),
    createdAt = createdAt,
    isSold = isSold
)
