package com.example.tinycell.data.remote.model

import com.example.tinycell.data.local.entity.UserEntity

data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profilePicUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun UserDto.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email,
    profilePicUrl = profilePicUrl,
    createdAt = createdAt
)
