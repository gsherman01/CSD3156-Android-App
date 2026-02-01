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
 * Updated to support paginated/batched loading.
 */
@Dao
interface ListingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listing: ListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<ListingEntity>)

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    /**
     * [PAGINATION_SUPPORT]: Get a batch of listings older than a specific timestamp.
     */
    @Query("""
        SELECT * FROM listings 
        WHERE isSold = 0 AND createdAt < :lastTimestamp 
        ORDER BY createdAt DESC 
        LIMIT :pageSize
    """)
    fun getActiveListingsBatch(lastTimestamp: Long, pageSize: Int): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :listingId")
    suspend fun getListingById(listingId: String): ListingEntity?

    @Query("SELECT * FROM listings WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getListingsByCategory(categoryId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE userId = :userId ORDER BY createdAt DESC")
    fun getListingsByUser(userId: String): Flow<List<ListingEntity>>

    @Query("""
        SELECT * FROM listings
        WHERE title LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchListings(query: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE isSold = 0 ORDER BY createdAt DESC")
    fun getActiveListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE isSold = 1 ORDER BY createdAt DESC")
    fun getSoldListings(): Flow<List<ListingEntity>>

    @Update
    suspend fun update(listing: ListingEntity)

    @Query("UPDATE listings SET isSold = 1 WHERE id = :listingId")
    suspend fun markAsSold(listingId: String)

    @Delete
    suspend fun delete(listing: ListingEntity)

    @Query("DELETE FROM listings")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

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
