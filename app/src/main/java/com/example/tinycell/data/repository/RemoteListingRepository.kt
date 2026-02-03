package com.example.tinycell.data.repository

import com.example.tinycell.data.remote.model.ListingDto
import kotlinx.coroutines.flow.Flow

/**
 * [PHASE 3]: Unified Remote Listing Repository.
 * Offer cloud operations have been moved to RemoteOfferRepository.
 */
interface RemoteListingRepository {
    /**
     * Streams updates. UI observes this for instant changes.
     */
    fun getRemoteListings(): Flow<List<ListingDto>>

    /**
     * Used by ListingRepository.syncFromRemote() to hydrate local Room.
     */
    suspend fun fetchListings(): List<ListingDto>

    /**
     * [PAGINATION_SUPPORT]: Fetches a batch of listings from Firestore.
     * @param pageSize Number of items to fetch.
     * @param lastTimestamp Timestamp of the last item in the previous batch (for cursor).
     */
    suspend fun fetchListingsBatch(pageSize: Int, lastTimestamp: Long?): List<ListingDto>

    /**
     * Pushes a new listing to Firestore.
     */
    suspend fun uploadListing(listing: ListingDto): Result<Unit>

    /**
     * Fetches a single listing by ID.
     */
    suspend fun getListingById(id: String): ListingDto?

    /**
     * [PHASE 6]: Targeted status update on a listing document.
     * Used by the Offer lifecycle to transition a listing to PENDING or SOLD
     * without re-uploading the entire document.
     */
    suspend fun updateListingStatus(listingId: String, status: String, isSold: Boolean): Result<Unit>
}
