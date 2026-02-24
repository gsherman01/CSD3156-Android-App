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
 * Updated to support the new Listing model with image carousel.
 */
class FavouriteRepository(private val favouriteDao: FavouriteDao) {

    /**
     * Toggle a listing's favourite status for a user.
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

    suspend fun addFavourite(userId: String, listingId: String) {
        favouriteDao.addFavourite(
            FavouriteEntity(
                userId = userId,
                listingId = listingId,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFavourite(userId: String, listingId: String) {
        favouriteDao.removeFavourite(userId, listingId)
    }

    suspend fun isFavourite(userId: String, listingId: String): Boolean {
        return favouriteDao.isFavourite(userId, listingId)
    }

    /**
     * Get all favourite listings for a user.
     * Updated to handle List<String> for images.
     */
    fun getUserFavouriteListings(userId: String): Flow<List<Listing>> {
        return favouriteDao.getUserFavouriteListings(userId)
            .map { entities -> entities.map { it.toListing() } }
    }

    suspend fun getFavouriteCount(userId: String): Int {
        return favouriteDao.getFavouriteCount(userId)
    }

    suspend fun getFavouriteCountForListing(listingId: String): Int {
        return favouriteDao.getFavouriteCountForListing(listingId)
    }

    suspend fun removeAllFavouritesForUser(userId: String) {
        favouriteDao.removeAllFavouritesForUser(userId)
    }
}

/**
 * Extension function: Convert ListingEntity to Listing model.
 * [FIXED]: Maps imageUrls string to List<String> for Carousel support.
 */
private fun ListingEntity.toListing(): Listing {
    return Listing(
        id = id,
        title = title,
        price = price,
        category = categoryId,
        sellerId = userId,
        sellerName = sellerName,
        description = description,
        imageUrls = if (imageUrls.isBlank()) emptyList() else imageUrls.split(","),
        isSold = isSold,
        status = status,
        createdAt = createdAt
    )
}
