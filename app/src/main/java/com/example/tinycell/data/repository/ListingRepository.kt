package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.dao.ListingDao
import com.example.tinycell.data.local.entity.ListingEntity
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.remote.model.toEntity
import com.example.tinycell.data.remote.model.toDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ListingRepository"

/**
 * [PHASE 4]: Dual Write Strategy Implementation.
 * 
 * Orchestrates data between Local Room (Source of Truth for UI) 
 * and Remote Firestore (Source of Truth for Cloud).
 */
class ListingRepository(
    private val listingDao: ListingDao,
    private val remoteRepo: RemoteListingRepository,
    private val imageRepo: RemoteImageRepository
) {

    /**
     * [PHASE 3]: Background Remote Sync.
     */
    suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        try {
            val remoteListings = remoteRepo.fetchListings()
            val entities = remoteListings.map { it.toEntity() }
            listingDao.insertAll(entities)
            Log.d(TAG, "Sync successful: ${entities.size} items updated.")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
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
     * [PHASE 4]: DUAL WRITE STRATEGY
     * 
     * 1. IMMEDIATE LOCAL WRITE: Save to Room so UI updates instantly.
     * 2. ASYNC REMOTE WRITE: Upload images and metadata to Firebase.
     * 
     * [LEARNING_POINT: CONSISTENCY]
     * Room is updated twice: first with local file paths, then with remote URLs.
     * This ensures the user can see their image even before it's uploaded.
     */
    suspend fun createListing(entity: ListingEntity) = withContext(Dispatchers.IO) {
        // 1. Save locally first (Optimistic Update)
        listingDao.insert(entity)
        Log.d(TAG, "Local write success: ${entity.id}")

        try {
            // 2. Process Images (Local Path -> Cloud URL)
            val localPaths = if (entity.imageUrls.isEmpty()) emptyList() else entity.imageUrls.split(",")
            
            if (localPaths.isNotEmpty()) {
                Log.d(TAG, "Starting image upload for: ${entity.id}")
                val uploadResult = imageRepo.uploadImages(localPaths)
                
                val remoteUrls = uploadResult.getOrThrow()
                val updatedEntity = entity.copy(imageUrls = remoteUrls.joinToString(","))

                // 3. Update local Room with cloud URLs
                listingDao.update(updatedEntity)
                
                // 4. Push to Firestore
                remoteRepo.uploadListing(updatedEntity.toDto()).getOrThrow()
                Log.d(TAG, "Remote write success: ${entity.id}")
            } else {
                // No images, push metadata directly
                remoteRepo.uploadListing(entity.toDto()).getOrThrow()
            }
            
        } catch (e: Exception) {
            // [TODO_RETRY_STRATEGY]: In a real app, we would mark this entity 
            // as "needs_sync" in Room and use WorkManager to retry later.
            Log.e(TAG, "Remote write failed for ${entity.id}: ${e.message}")
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
    ) {
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
