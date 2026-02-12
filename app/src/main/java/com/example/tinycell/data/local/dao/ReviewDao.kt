package com.example.tinycell.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tinycell.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Reviews.
 */
@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE revieweeId = :userId ORDER BY timestamp DESC")
    fun getReviewsForUser(userId: String): Flow<List<ReviewEntity>>

    @Query("SELECT AVG(rating) FROM reviews WHERE revieweeId = :userId")
    fun getAverageRating(userId: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM reviews WHERE revieweeId = :userId")
    fun getReviewCount(userId: String): Flow<Int>

    // [FIX]: Check if a user has already reviewed this specific transaction/listing
    @Query("SELECT * FROM reviews WHERE reviewerId = :reviewerId AND listingId = :listingId LIMIT 1")
    fun getReviewByReviewerAndListing(reviewerId: String, listingId: String): Flow<ReviewEntity?>
}
