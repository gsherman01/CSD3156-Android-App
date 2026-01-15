package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Category table.
 *
 * Represents a category for marketplace listings (e.g., Electronics, Fashion, Books).
 * Categories help users browse and filter listings.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val icon: String? = null  // Material icon name or emoji (e.g., "📱", "👗", "📚")
)
