package com.example.tinycell.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tinycell.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

// incomplete part for CreateListing. This is known as foregin keys
import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.UserEntity

/**
 * Data Access Object for Listing table.
 *
 * Provides comprehensive CRUD operations and queries for marketplace listings.
 * Includes search, filtering, and sorting capabilities.
 */
@Dao
interface ListingDao {

    /**
     * Insert a new listing. Replaces if listing with same ID exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listing: ListingEntity)

    /**
     * Insert multiple listings at once (for seeding/sync operations).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<ListingEntity>)

    /**
     * Get all listings ordered by creation date (newest first).
     * Returns as Flow for reactive UI updates.
     */
    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    /**
     * Get a specific listing by ID.
     */
    @Query("SELECT * FROM listings WHERE id = :listingId")
    suspend fun getListingById(listingId: String): ListingEntity?

    /**
     * Get listings filtered by category.
     */
    @Query("SELECT * FROM listings WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getListingsByCategory(categoryId: String): Flow<List<ListingEntity>>

    /**
     * Get all listings created by a specific user.
     */
    @Query("SELECT * FROM listings WHERE userId = :userId ORDER BY createdAt DESC")
    fun getListingsByUser(userId: String): Flow<List<ListingEntity>>

    /**
     * Search listings by title or description.
     * Uses LIKE for case-insensitive partial matching.
     *
     * @param query Search term (will be wrapped with % wildcards)
     */
    @Query("""
        SELECT * FROM listings
        WHERE title LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchListings(query: String): Flow<List<ListingEntity>>

    /**
     * Get unsold listings only (active marketplace items).
     */
    @Query("SELECT * FROM listings WHERE isSold = 0 ORDER BY createdAt DESC")
    fun getActiveListings(): Flow<List<ListingEntity>>

    /**
     * Get sold listings (for user's sales history).
     */
    @Query("SELECT * FROM listings WHERE isSold = 1 ORDER BY createdAt DESC")
    fun getSoldListings(): Flow<List<ListingEntity>>

    /**
     * Get listings by user that are still active (not sold).
     */
    @Query("SELECT * FROM listings WHERE userId = :userId AND isSold = 0 ORDER BY createdAt DESC")
    fun getActiveListingsByUser(userId: String): Flow<List<ListingEntity>>

    /**
     * Update an existing listing.
     */
    @Update
    suspend fun update(listing: ListingEntity)

    /**
     * Mark a listing as sold.
     */
    @Query("UPDATE listings SET isSold = 1 WHERE id = :listingId")
    suspend fun markAsSold(listingId: String)

    /**
     * Delete a listing.
     */
    @Delete
    suspend fun delete(listing: ListingEntity)

    /**
     * Delete all listings (useful for testing/reset).
     */
    @Query("DELETE FROM listings")
    suspend fun deleteAll()

    /**
     * Get count of listings by user (for profile stats).
     */
    @Query("SELECT COUNT(*) FROM listings WHERE userId = :userId")
    suspend fun getListingCountByUser(userId: String): Int

    /**
     * [TODO_DATABASE_INTEGRATION]:
     * These methods are required for database seeding to prevent Error 787.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    /**
     * Advanced search with multiple filters.
     * Supports text search, multiple categories, price range, and date range simultaneously.
     *
     * @param query Search term for title/description (use "%" for no search)
     * @param categoryIds List of category IDs to filter (empty list = all categories)
     * @param minPrice Minimum price filter
     * @param maxPrice Maximum price filter
     * @param minDate Minimum creation date (timestamp)
     * @param maxDate Maximum creation date (timestamp)
     */
    @Query("""
        SELECT * FROM listings
        WHERE (:query = '%' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
          AND (
            CASE
                WHEN :categoryIdsSize = 0 THEN 1
                ELSE categoryId IN (:categoryIds)
            END
          )
          AND price >= :minPrice
          AND price <= :maxPrice
          AND createdAt >= :minDate
          AND createdAt <= :maxDate
          AND isSold = 0
        ORDER BY createdAt DESC
    """)
    fun searchWithFilters(
        query: String,
        categoryIds: List<String>,
        categoryIdsSize: Int,
        minPrice: Double,
        maxPrice: Double,
        minDate: Long,
        maxDate: Long
    ): Flow<List<ListingEntity>>


}
