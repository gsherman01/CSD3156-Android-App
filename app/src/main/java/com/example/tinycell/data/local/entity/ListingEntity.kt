package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tinycell.data.remote.model.ListingDto

/**
 * Room entity for Listing table.
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
    val categoryId: String,
    val location: String? = null,
    val imageUrls: String = "",
    val createdAt: Long,
    val isSold: Boolean = false
)

fun ListingEntity.toDto() = ListingDto(
    id = id,
    title = title,
    price = price,
    description = description,
    userId = userId,
    createdAt = createdAt,
    isSold = isSold,
    categoryId = categoryId,
    // [FIX]: Map CSV string back to List for Firestore
    imageUrls = if (imageUrls.isEmpty()) emptyList() else imageUrls.split(",")
)
