package com.example.tinycell.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tinycell.data.local.entity.FavouriteEntity
import com.example.tinycell.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Favourite table.
 *
 * Manages user's favourite listings (many-to-many relationship).
 * Includes JOIN queries to retrieve full listing details.
 */
@Dao
interface FavouriteDao {

    /**
     * Add a listing to user's favourites.
     * Uses IGNORE strategy to handle duplicate favourites gracefully.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavourite(favourite: FavouriteEntity)

    /**
     * Remove a listing from user's favourites.
     *
     * @param userId The user ID
     * @param listingId The listing ID to unfavourite
     */
    @Query("DELETE FROM favourites WHERE userId = :userId AND listingId = :listingId")
    suspend fun removeFavourite(userId: String, listingId: String)

    /**
     * Get all favourite entries for a user.
     * Returns raw FavouriteEntity objects.
     */
    @Query("SELECT * FROM favourites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserFavourites(userId: String): Flow<List<FavouriteEntity>>

    /**
     * Check if a specific listing is favourited by a user.
     *
     * @return true if favourited, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE userId = :userId AND listingId = :listingId)")
    suspend fun isFavourite(userId: String, listingId: String): Boolean

    /**
     * Get full listing details for user's favourites (JOIN query).
     *
     * This performs an INNER JOIN between favourites and listings tables,
     * returning complete ListingEntity objects ordered by when they were favourited.
     *
     * @param userId The user ID
     * @return Flow of ListingEntity objects that the user has favourited
     */
    @Query("""
        SELECT listings.*
        FROM listings
        INNER JOIN favourites ON listings.id = favourites.listingId
        WHERE favourites.userId = :userId
        ORDER BY favourites.createdAt DESC
    """)
    fun getUserFavouriteListings(userId: String): Flow<List<ListingEntity>>

    /**
     * Get count of favourites for a user (for profile stats).
     */
    @Query("SELECT COUNT(*) FROM favourites WHERE userId = :userId")
    suspend fun getFavouriteCount(userId: String): Int

    /**
     * Get count of how many users favourited a specific listing.
     * Useful for showing popularity.
     */
    @Query("SELECT COUNT(*) FROM favourites WHERE listingId = :listingId")
    suspend fun getFavouriteCountForListing(listingId: String): Int

    /**
     * Remove all favourites for a user (useful for account cleanup).
     */
    @Query("DELETE FROM favourites WHERE userId = :userId")
    suspend fun removeAllFavouritesForUser(userId: String)

    /**
     * Delete all favourites (useful for testing/reset).
     */
    @Query("DELETE FROM favourites")
    suspend fun deleteAll()
}
