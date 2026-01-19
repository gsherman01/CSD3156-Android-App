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
 * [PHASE 5.5]: Updated ListingRepository with Auth Integration.
 */
class ListingRepository(
    private val listingDao: ListingDao,
    private val remoteRepo: RemoteListingRepository,
    private val imageRepo: RemoteImageRepository,
    private val authRepo: AuthRepository // Added in Phase 5.5
) {

    suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        try {
            val remoteListings = remoteRepo.fetchListings()
            val entities = remoteListings.map { it.toEntity() }
            listingDao.insertAll(entities)
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
     * [PHASE 5.5]: Dual Write with Real Auth UID.
     */
    suspend fun createListing(entity: ListingEntity) = withContext(Dispatchers.IO) {
        // 1. Ensure we have the correct UID from Auth
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val secureEntity = entity.copy(userId = currentUid)

        // 2. Save locally first
        listingDao.insert(secureEntity)

        try {
            // 3. Process Images
            val localPaths = if (secureEntity.imageUrls.isEmpty()) emptyList() else secureEntity.imageUrls.split(",")
            
            if (localPaths.isNotEmpty()) {
                val uploadResult = imageRepo.uploadImages(localPaths)
                val remoteUrls = uploadResult.getOrThrow()
                val updatedEntity = secureEntity.copy(imageUrls = remoteUrls.joinToString(","))

                listingDao.update(updatedEntity)
                remoteRepo.uploadListing(updatedEntity.toDto()).getOrThrow()
            } else {
                remoteRepo.uploadListing(secureEntity.toDto()).getOrThrow()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Remote write failed for ${secureEntity.id}: ${e.message}")
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
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val catId = category.ifBlank { "General" }

        val newListing = Listing(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            price = price,
            category = catId,
            sellerName = currentUid,
            description = description,
            imageUrl = imagePaths.joinToString(",")
        )

        createListing(newListing.toEntity(currentUid))
    }
}

/**
 * Mappers updated for Phase 5.5
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

private fun Listing.toEntity(uid: String = "anonymous"): ListingEntity {
    return ListingEntity(
        id = id,
        title = title,
        description = description ?: "",
        price = price,
        userId = uid,
        categoryId = category,
        location = null,
        imageUrls = imageUrl ?: "",
        createdAt = System.currentTimeMillis(),
        isSold = false
    )
}
