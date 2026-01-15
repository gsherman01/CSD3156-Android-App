package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for Favourite table (many-to-many relationship).
 *
 * Join table connecting users and their favourite listings.
 * Composite unique index on (userId, listingId) prevents duplicate favourites.
 * CASCADE delete ensures favourites are removed when user or listing is deleted.
 */
@Entity(
    tableName = "favourites",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ListingEntity::class,
            parentColumns = ["id"],
            childColumns = ["listingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["listingId"]),
        Index(value = ["userId", "listingId"], unique = true)
    ]
)
data class FavouriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,

    val listingId: String,

    val createdAt: Long
)
