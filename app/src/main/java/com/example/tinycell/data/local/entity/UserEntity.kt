package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for User table.
 *
 * Represents a user in the TinyCell marketplace.
 * Each user can create multiple listings and have multiple favourites.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val email: String,

    val profilePicUrl: String? = null,

    val createdAt: Long

)
