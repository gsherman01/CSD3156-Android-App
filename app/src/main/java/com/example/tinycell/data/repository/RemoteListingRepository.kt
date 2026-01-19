package com.example.tinycell.data.repository

import com.example.tinycell.data.remote.model.ListingDto
import kotlinx.coroutines.flow.Flow

/**
 * [PHASE 3]: Unified Remote Listing Repository.
 * This is the single contract for all Cloud Firestore operations.
 */
interface RemoteListingRepository {
    /**
     * [LEARNING_POINT: REAL-TIME FLOW]
     * Streams updates. UI observes this for instant changes.
     */
    fun getRemoteListings(): Flow<List<ListingDto>>

    /**
     * [LEARNING_POINT: ONE-SHOT FETCH]
     * Used by ListingRepository.syncFromRemote() to hydrate local Room.
     */
    suspend fun fetchListings(): List<ListingDto>

    /**
     * Pushes a new listing to Firestore.
     */
    suspend fun uploadListing(listing: ListingDto): Result<Unit>

    /**
     * Fetches a single listing by ID.
     */
    suspend fun getListingById(id: String): ListingDto?
}
