package com.example.tinycell.data.repository

import com.example.tinycell.data.local.dao.FavouriteDao
import com.example.tinycell.data.local.entity.FavouriteEntity
import com.example.tinycell.data.local.entity.ListingEntity
import com.example.tinycell.data.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * FAVOURITE REPOSITORY
 *
 * Repository pattern implementation for user favourites (saved listings).
 * Handles many-to-many relationship between users and listings.
 *
 * Provides toggle functionality and JOIN queries to retrieve full listing details.
 */
class FavouriteRepository(private val favouriteDao: FavouriteDao) {

    /**
     * Toggle a listing's favourite status for a user.
     * If already favourited, removes it. If not favourited, adds it.
     *
     * @param userId The user ID
     * @param listingId The listing ID to toggle
     */
    suspend fun toggleFavourite(userId: String, listingId: String) {
        if (favouriteDao.isFavourite(userId, listingId)) {
            favouriteDao.removeFavourite(userId, listingId)
        } else {
            favouriteDao.addFavourite(
                FavouriteEntity(
                    userId = userId,
                    listingId = listingId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Add a listing to user's favourites.
     */
    suspend fun addFavourite(userId: String, listingId: String) {
        favouriteDao.addFavourite(
            FavouriteEntity(
                userId = userId,
                listingId = listingId,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Remove a listing from user's favourites.
     */
    suspend fun removeFavourite(userId: String, listingId: String) {
        favouriteDao.removeFavourite(userId, listingId)
    }

    /**
     * Check if a listing is favourited by a user.
     */
    suspend fun isFavourite(userId: String, listingId: String): Boolean {
        return favouriteDao.isFavourite(userId, listingId)
    }

    /**
     * Get all favourite listings for a user (with full listing details).
     * Uses JOIN query to retrieve complete Listing objects.
     *
     * @param userId The user ID
     * @return Flow of Listing objects that the user has favourited
     */
    fun getUserFavouriteListings(userId: String): Flow<List<Listing>> {
        return favouriteDao.getUserFavouriteListings(userId)
            .map { entities -> entities.map { it.toListing() } }
    }

    /**
     * Get count of favourites for a user.
     */
    suspend fun getFavouriteCount(userId: String): Int {
        return favouriteDao.getFavouriteCount(userId)
    }

    /**
     * Get how many users favourited a specific listing (popularity metric).
     */
    suspend fun getFavouriteCountForListing(listingId: String): Int {
        return favouriteDao.getFavouriteCountForListing(listingId)
    }

    /**
     * Remove all favourites for a user (cleanup operation).
     */
    suspend fun removeAllFavouritesForUser(userId: String) {
        favouriteDao.removeAllFavouritesForUser(userId)
    }
}

/**
 * Extension function: Convert ListingEntity to Listing model.
 * Reused from JOIN query results.
 */
private fun ListingEntity.toListing(): Listing {
    return Listing(
        id = id,
        title = title,
        price = price,
        category = categoryId,  // TODO: Convert to category name
        sellerName = userId,    // TODO: Convert to seller name
        description = description,
        imageUrl = imageUrls.split(",").firstOrNull()?.takeIf { it.isNotBlank() }
    )
}
