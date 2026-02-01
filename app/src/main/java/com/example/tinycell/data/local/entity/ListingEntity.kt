package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tinycell.data.remote.model.ListingDto

/**
 * [FINAL VERSION]: Room entity for Listing table.
 * Updated with status field for robust offer lifecycle handling.
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
    val sellerName: String = "",
    val categoryId: String,
    val location: String? = null,
    val imageUrls: String = "",
    val createdAt: Long,
    val isSold: Boolean = false,
    
    // [PHASE 6.1]: Explicit status for Offer Stage handling
    // AVAILABLE, PENDING (Under Offer), SOLD
    val status: String = "AVAILABLE"
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
    isSold = isSold,
    status = status
)
