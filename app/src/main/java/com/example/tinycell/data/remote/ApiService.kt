package com.example.tinycell.data.remote

import com.example.tinycell.data.local.entity.AppEntity
import retrofit2.http.GET

/**
 * TODO: Networking Implementation
 * 1. [ENDPOINTS]: Replace 'items' with actual backend endpoints.
 * 2. [MODELS]: Create specific Data Transfer Objects (DTOs) if the API schema
 *    differs from the 'AppEntity' database model.
 */
interface ApiService {
    @GET("items")
    suspend fun getNetworkItems(): List<AppEntity>
}
