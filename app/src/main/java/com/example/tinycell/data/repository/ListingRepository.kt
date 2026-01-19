package com.example.tinycell.data.repository

import com.example.tinycell.data.local.dao.ListingDao
import com.example.tinycell.data.local.entity.ListingEntity
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.remote.model.toEntity
import com.example.tinycell.data.remote.model.toDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Remote dependencies
import com.example.tinycell.data.repository.RemoteListingRepository

/**
 * [PHASE 3]: REPOSITORY SYNC STRATEGY
 *
 * This repository implements the Local-First pattern.
 * - Room is the Single Source of Truth for the UI.
 * - Firestore is the remote source of truth.
 *
 * [LEARNING_POINT: OFFLINE-FIRST]
 * We separate the data stream (Flow from Room) from the sync action (Suspend function).
 * This ensures the UI is always reactive and works without internet.
 */
class ListingRepository(
    private val listingDao: ListingDao,
    private val remoteRepo: RemoteListingRepository
) {

    /**
     * [TODO_SYNC_STRATEGY]: Background Sync
     * Pulls latest listings from Firestore and saves them to Room.
     * Room's 'OnConflictStrategy.REPLACE' prevents duplicate IDs.
     */
    suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch from Firestore (via RemoteRepo interface)
            val remoteListings = remoteRepo.fetchListings()
            
            // 2. Map DTOs to Entities
            val entities = remoteListings.map { it.toEntity() }
            
            // 3. Update local database
            listingDao.insertAll(entities)
        } catch (e: Exception) {
            // [TODO_ERROR_HANDLING]: Log sync failure (e.g., Network timeout)
        }
    }

    /**
     * UI Observation: Always observe Room.
     */
    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
        .map { entities -> entities.map { it.toListing() } }

    val activeListings: Flow<List<Listing>> = listingDao.getActiveListings()
        .map { entities -> entities.map { it.toListing() } }

    suspend fun getListingById(id: String): Listing? {
        return listingDao.getListingById(id)?.toListing()
    }

    /**
     * [LEARNING_POINT: WRITE-THROUGH CACHE]
     * When creating a listing, we save to local Room immediately for instant UI feedback,
     * then push to Firestore to sync with other users.
     */
    suspend fun createListing(entity: ListingEntity) {
        // 1. Save locally first (Immediate UI feedback)
        listingDao.insert(entity)

        // 2. Push to Firestore (Sync to Cloud)
        try {
            remoteRepo.createListing(entity.toDto())
        } catch (e: Exception) {
            // [TODO_RETRY_LOGIC]: If remote fails, mark for future sync
        }
    }

    // --- Search and Filtering Logic ---

    fun getListingsByCategory(categoryId: String): Flow<List<Listing>> {
        return listingDao.getListingsByCategory(categoryId)
            .map { entities -> entities.map { it.toListing() } }
    }

    fun getListingsByUser(userId: String): Flow<List<Listing>> {
        return listingDao.getListingsByUser(userId)
            .map { entities -> entities.map { it.toListing() } }
    }

    fun searchListings(query: String): Flow<List<Listing>> {
        return listingDao.searchListings(query)
            .map { entities -> entities.map { it.toListing() } }
    }

    suspend fun insertListing(listing: Listing) {
        listingDao.insert(listing.toEntity())
    }

    suspend fun updateListing(listing: Listing) {
        listingDao.update(listing.toEntity())
    }

    suspend fun markListingAsSold(listingId: String) {
        listingDao.markAsSold(listingId)
    }

    suspend fun deleteListing(listing: Listing) {
        listingDao.delete(listing.toEntity())
    }

    suspend fun createNewListing(
        title: String,
        price: Double,
        description: String,
        category: String,
        imagePaths: List<String>
    ) = withContext(Dispatchers.IO) {
        val sellerId = "user_1"
        val catId = category.ifBlank { "General" }

        val newListing = Listing(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            price = price,
            category = catId,
            sellerName = sellerId,
            description = description,
            imageUrl = imagePaths.joinToString(",")
        )

        // Use the unified createListing logic
        createListing(newListing.toEntity())
    }
}

/**
 * Mappers remain separate to keep Repository clean.
 */
private fun ListingEntity.toListing(): Listing {
    return Listing(
        id = id,
        title = title,
        price = price,
        category = categoryId,
        sellerName = userId,
        description = description,
        imageUrl = imageUrls.split(",").firstOrNull()?.takeIf { it.isNotBlank() }
    )
}

private fun Listing.toEntity(): ListingEntity {
    return ListingEntity(
        id = id,
        title = title,
        description = description ?: "",
        price = price,
        userId = "user_1",
        categoryId = category,
        location = null,
        imageUrls = imageUrl ?: "",
        createdAt = System.currentTimeMillis(),
        isSold = false
    )
}
