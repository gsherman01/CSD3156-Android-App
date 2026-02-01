package com.example.tinycell.data.local.dao

import androidx.room.*
import com.example.tinycell.data.local.entity.OfferEntity
import kotlinx.coroutines.flow.Flow

/**
 * [PHASE 6]: DAO for Formal Offer System.
 */
@Dao
interface OfferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offer: OfferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<OfferEntity>)

    @Query("SELECT * FROM offers WHERE listingId = :listingId ORDER BY timestamp DESC")
    fun getOffersByListing(listingId: String): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers WHERE buyerId = :userId OR sellerId = :userId ORDER BY timestamp DESC")
    fun getOffersForUser(userId: String): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers WHERE id = :offerId")
    suspend fun getOfferById(offerId: String): OfferEntity?

    @Query("UPDATE offers SET status = :status WHERE id = :offerId")
    suspend fun updateStatus(offerId: String, status: String)

    @Delete
    suspend fun delete(offer: OfferEntity)
}
