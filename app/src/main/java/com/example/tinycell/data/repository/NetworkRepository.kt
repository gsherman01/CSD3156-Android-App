package com.example.tinycell.data.repository

import com.example.tinycell.data.local.entity.AppEntity
import com.example.tinycell.data.remote.ApiService

/**
 * TODO: Network Repository Tasks
 * 1. [SYNC LOGIC]: Implement logic to fetch from network and save to Room (Local-First).
 * 2. [ERROR HANDLING]: Wrap calls in a Result or Resource wrapper to handle
 *    404, 500, or No Internet scenarios.
 */
class NetworkRepository(private val apiService: ApiService) {

    suspend fun fetchItems(): List<AppEntity> {
        // Placeholder for network call logic
        return apiService.getNetworkItems()
    }
}
