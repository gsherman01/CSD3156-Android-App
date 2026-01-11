package com.example.tinycell.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class AppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
