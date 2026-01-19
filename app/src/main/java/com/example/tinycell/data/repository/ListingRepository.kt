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

/**
 * [PHASE 3.5]: Updated ListingRepository with Image Cloud Support.
 *
 * This repository now orchestrates:
 * 1. Image Upload (Firebase Storage)
 * 2. Listing Metadata (Firestore)
 * 3. Local Cache (Room)
 */
class ListingRepository(
    private val listingDao: ListingDao,
    private val remoteRepo: RemoteListingRepository,
    private val imageRepo: RemoteImageRepository // Added for Phase 3.5
) {

    suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        try {
            val remoteListings = remoteRepo.fetchListings()
            val entities = remoteListings.map { it.toEntity() }
            listingDao.insertAll(entities)
        } catch (e: Exception) {
            // Log sync failure
        }
    }

    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
        .map { entities -> entities.map { it.toListing() } }

    val activeListings: Flow<List<Listing>> = listingDao.getActiveListings()
        .map { entities -> entities.map { it.toListing() } }

    suspend fun getListingById(id: String): Listing? {
        return listingDao.getListingById(id)?.toListing()
    }

    /**
     * [PHASE 3.5]: Uploads a listing with its images.
     * 1. Saves metadata to local Room first (with local paths).
     * 2. Uploads images to cloud and gets remote URLs.
     * 3. Updates local Room and Firestore with remote URLs.
     */
    suspend fun createListing(entity: ListingEntity) {
        // [TODO_OFFLINE_READY]: In the future, we can use WorkManager 
        // to handle the cloud upload if the network is flaky.
        
        // 1. Save locally first (Optimistic Update)
        listingDao.insert(entity)

        try {
            // 2. Upload images to Firebase Storage
            val localPaths = if (entity.imageUrls.isEmpty()) emptyList() else entity.imageUrls.split(",")
            val uploadResult = imageRepo.uploadImages(localPaths)
            
            val remoteUrls = uploadResult.getOrDefault(localPaths)
            val updatedEntity = entity.copy(imageUrls = remoteUrls.joinToString(","))

            // 3. Update Local Room with Remote URLs
            listingDao.update(updatedEntity)

            // 4. Push to Firestore
            remoteRepo.uploadListing(updatedEntity.toDto())
            
        } catch (e: Exception) {
            // [TODO_ERROR_HANDLING]: Mark listing as "pending_sync" if upload fails
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

    /**
     * Entry point for Creating a Listing.
     */
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

        createListing(newListing.toEntity())
    }
}

/**
 * Mappers
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
