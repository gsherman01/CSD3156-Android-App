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
 */
@Dao
interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavourite(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE userId = :userId AND listingId = :listingId")
    suspend fun removeFavourite(userId: String, listingId: String)

    @Query("SELECT * FROM favourites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserFavourites(userId: String): Flow<List<FavouriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE userId = :userId AND listingId = :listingId)")
    suspend fun isFavourite(userId: String, listingId: String): Boolean

    @Query("""
        SELECT listings.*
        FROM listings
        INNER JOIN favourites ON listings.id = favourites.listingId
        WHERE favourites.userId = :userId
        ORDER BY favourites.createdAt DESC
    """)
    fun getUserFavouriteListings(userId: String): Flow<List<ListingEntity>>

    @Query("SELECT COUNT(*) FROM favourites WHERE userId = :userId")
    suspend fun getFavouriteCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM favourites WHERE listingId = :listingId")
    suspend fun getFavouriteCountForListing(listingId: String): Int

    /**
     * [NEW]: Get all users who have favorited a specific listing.
     * Used for sending notifications (e.g., price drops).
     */
    @Query("SELECT * FROM favourites WHERE listingId = :listingId")
    fun getWatchersForListing(listingId: String): Flow<List<FavouriteEntity>>

    @Query("DELETE FROM favourites WHERE userId = :userId")
    suspend fun removeAllFavouritesForUser(userId: String)

    @Query("DELETE FROM favourites")
    suspend fun deleteAll()
}
