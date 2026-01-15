package com.example.tinycell.data.repository

import com.example.tinycell.data.local.dao.ListingDao
import com.example.tinycell.data.local.entity.ListingEntity
import com.example.tinycell.data.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * LISTING REPOSITORY
 *
 * Repository pattern implementation for marketplace listings.
 * Handles data operations using Room database for local persistence.
 *
 * Current: Room database for local storage
 * Future: Add Retrofit API for remote sync (coordinate with Member 4 - Networking)
 */
class ListingRepository(private val listingDao: ListingDao) {

    /**
     * Get all marketplace listings as reactive Flow.
     * UI can observe this to automatically update when data changes.
     */
    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
        .map { entities -> entities.map { it.toListing() } }

    /**
     * Get active (unsold) listings only.
     */
    val activeListings: Flow<List<Listing>> = listingDao.getActiveListings()
        .map { entities -> entities.map { it.toListing() } }

    /**
     * Get a single listing by ID.
     */
    suspend fun getListingById(id: String): Listing? {
        return listingDao.getListingById(id)?.toListing()
    }

    /**
     * Get listings filtered by category.
     */
    fun getListingsByCategory(categoryId: String): Flow<List<Listing>> {
        return listingDao.getListingsByCategory(categoryId)
            .map { entities -> entities.map { it.toListing() } }
    }

    /**
     * Get listings created by a specific user.
     */
    fun getListingsByUser(userId: String): Flow<List<Listing>> {
        return listingDao.getListingsByUser(userId)
            .map { entities -> entities.map { it.toListing() } }
    }

    /**
     * Search listings by title or description.
     */
    fun searchListings(query: String): Flow<List<Listing>> {
        return listingDao.searchListings(query)
            .map { entities -> entities.map { it.toListing() } }
    }

    /**
     * Insert a new listing into the database.
     */
    suspend fun insertListing(listing: Listing) {
        listingDao.insert(listing.toEntity())
    }

    /**
     * Insert multiple listings (for seeding/bulk operations).
     */
    suspend fun insertListings(listings: List<Listing>) {
        listingDao.insertAll(listings.map { it.toEntity() })
    }

    /**
     * Update an existing listing.
     */
    suspend fun updateListing(listing: Listing) {
        listingDao.update(listing.toEntity())
    }

    /**
     * Mark a listing as sold.
     */
    suspend fun markListingAsSold(listingId: String) {
        listingDao.markAsSold(listingId)
    }

    /**
     * Delete a listing.
     */
    suspend fun deleteListing(listing: Listing) {
        listingDao.delete(listing.toEntity())
    }

    /**
     * Get count of listings by user.
     */
    suspend fun getListingCountByUser(userId: String): Int {
        return listingDao.getListingCountByUser(userId)
    }
}

/**
 * Extension function: Convert ListingEntity (database) to Listing (UI model).
 */
private fun ListingEntity.toListing(): Listing {
    return Listing(
        id = id,
        title = title,
        price = price,
        category = categoryId,  // TODO: Convert categoryId to category name (coordinate with Category repository)
        sellerName = userId,    // TODO: Convert userId to seller name (coordinate with User repository)
        description = description,
        imageUrl = imageUrls.split(",").firstOrNull()?.takeIf { it.isNotBlank() }  // Get first image URL
    )
}

/**
 * Extension function: Convert Listing (UI model) to ListingEntity (database).
 */
private fun Listing.toEntity(): ListingEntity {
    return ListingEntity(
        id = id,
        title = title,
        description = description ?: "",
        price = price,
        userId = "user_1",  // TODO: Get from authentication system when implemented
        categoryId = category,  // Assuming category is the ID for now
        location = null,  // TODO: Add location field to Listing model when location feature is implemented
        imageUrls = imageUrl ?: "",  // Single image URL for now
        createdAt = System.currentTimeMillis(),
        isSold = false
    )
}
