package com.example.tinycell.data.repository

import com.example.tinycell.data.remote.model.ListingDto
import kotlinx.coroutines.flow.Flow

/**
 * [TODO_NETWORKING_INTEGRATION]
 * One Responsibility: Abstract Firestore operations for Listings.
 */
interface RemoteListingRepository {
    /**
     * Streams all listings from Firestore.
     */
    fun getRemoteListings(): Flow<List<ListingDto>>

    /**
     * Pushes a new listing to Firestore.
     */
    suspend fun uploadListing(listing: ListingDto): Result<Unit>

    /**
     * Fetches a single listing by ID.
     */
    suspend fun getListingById(id: String): ListingDto?
}