package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for Listing table.
 *
 * Represents a marketplace listing created by a user.
 * Foreign keys establish relationships with User and Category tables.
 * Indices on userId and categoryId improve query performance.
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

    /**
     * Stores multiple image URLs as comma-separated string.
     * Example: "url1,url2,url3"
     * Convert to/from List<String> in repository layer.
     */
    val imageUrls: String = "",

    val createdAt: Long,

    val isSold: Boolean = false
)
