package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.dao.ListingDao
import com.example.tinycell.data.local.dao.UserDao
import com.example.tinycell.data.local.entity.ListingEntity
import com.example.tinycell.data.local.entity.UserEntity
import com.example.tinycell.data.local.entity.toDto
import com.example.tinycell.data.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ListingRepository"

/**
 * [FINAL VERSION]: Updated ListingRepository.
 * Includes Pagination and Real-time sync support.
 */
class ListingRepository(
    private val listingDao: ListingDao,
    private val userDao: UserDao,
    private val remoteRepo: RemoteListingRepository,
    private val imageRepo: RemoteImageRepository,
    private val authRepo: AuthRepository
) {

    /**
     * Observable Flows for UI - Traditional full list
     */
    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
        .map { entities -> entities.map { it.toListing() } }

    val activeListings: Flow<List<Listing>> = listingDao.getActiveListings()
        .map { entities -> entities.map { it.toListing() } }

    /**
     * [PAGINATION_SUPPORT]: Observes a specific window of data from Room.
     */
    fun getActiveListingsPaged(lastTimestamp: Long, pageSize: Int): Flow<List<Listing>> {
        return listingDao.getActiveListingsBatch(lastTimestamp, pageSize)
            .map { entities -> entities.map { it.toListing() } }
    }

    /**
     * [PAGINATION_SUPPORT]: Triggers a background fetch for a batch of data from Firestore.
     */
    suspend fun syncBatchFromRemote(pageSize: Int, lastTimestamp: Long?) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Syncing batch from remote. LastTimestamp: $lastTimestamp")
            val remoteListings = remoteRepo.fetchListingsBatch(pageSize, lastTimestamp)
            
            // Sync users referenced in listings to satisfy Foreign Keys
            remoteListings.map { it.userId to it.sellerName }.distinct().forEach { (uid, name) ->
                ensureUserExistsLocally(uid, name)
            }

            // Convert and Insert into Room
            val entities = remoteListings.map { dto ->
                ListingEntity(
                    id = dto.id,
                    title = dto.title,
                    description = dto.description,
                    price = dto.price,
                    userId = dto.userId,
                    sellerName = dto.sellerName,
                    categoryId = dto.categoryId,
                    location = dto.location,
                    imageUrls = dto.imageUrls.joinToString(","),
                    createdAt = dto.createdAt,
                    isSold = dto.isSold
                )
            }
            listingDao.insertAll(entities)
            Log.d(TAG, "Batch sync completed. Inserted ${entities.size} items.")
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync failed: ${e.message}")
        }
    }

    suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "DEBUG: Starting full remote sync...")
            val remoteListings = remoteRepo.fetchListings()
            
            remoteListings.map { it.userId to it.sellerName }.distinct().forEach { (uid, name) ->
                ensureUserExistsLocally(uid, name)
            }

            val entities = remoteListings.map { dto ->
                ListingEntity(
                    id = dto.id,
                    title = dto.title,
                    description = dto.description,
                    price = dto.price,
                    userId = dto.userId,
                    sellerName = dto.sellerName,
                    categoryId = dto.categoryId,
                    location = dto.location,
                    imageUrls = dto.imageUrls.joinToString(","),
                    createdAt = dto.createdAt,
                    isSold = dto.isSold
                )
            }
            listingDao.insertAll(entities)
            Log.d(TAG, "DEBUG: Sync completed.")
        } catch (e: Exception) {
            Log.e(TAG, "DEBUG: Sync failed: ${e.message}")
        }
    }

    suspend fun createListing(entity: ListingEntity) = withContext(Dispatchers.IO) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val currentName = authRepo.getCurrentUserName() ?: "Anonymous"
        
        ensureUserExistsLocally(currentUid, currentName)
        val secureEntity = entity.copy(userId = currentUid, sellerName = currentName)
        listingDao.insert(secureEntity)

        try {
            val localPaths = if (secureEntity.imageUrls.isEmpty()) emptyList() else secureEntity.imageUrls.split(",")
            var finalEntity = secureEntity

            if (localPaths.isNotEmpty()) {
                val uploadResult = imageRepo.uploadImages(localPaths)
                val remoteUrls = uploadResult.getOrThrow()
                finalEntity = secureEntity.copy(imageUrls = remoteUrls.joinToString(","))
                listingDao.update(finalEntity)
            }

            remoteRepo.uploadListing(finalEntity.toDto()).getOrThrow()
        } catch (e: Exception) {
            Log.e(TAG, "Remote write failed: ${e.message}")
        }
    }

    /**
     * Helper to create a listing from UI parameters.
     */
    suspend fun createNewListing(title: String, price: Double, description: String, category: String, imagePaths: List<String>) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val currentName = authRepo.getCurrentUserName() ?: "Anonymous"
        val newListing = Listing(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            price = price,
            category = category.ifBlank { "General" },
            sellerId = currentUid,
            sellerName = currentName,
            description = description,
            imageUrl = imagePaths.joinToString(",")
        )
        createListing(newListing.toEntity(currentUid, currentName))
    }

    private suspend fun ensureUserExistsLocally(uid: String, name: String) {
        val existingUser = userDao.getUserById(uid)
        if (existingUser == null) {
            userDao.insert(UserEntity(
                id = uid,
                name = name.ifBlank { "User_$uid" },
                email = "",
                createdAt = System.currentTimeMillis()
            ))
        } else if (existingUser.name != name && name.isNotBlank()) {
            userDao.insert(existingUser.copy(name = name))
        }
    }

    suspend fun getListingById(id: String): Listing? = listingDao.getListingById(id)?.toListing()

    fun getListingsByCategory(categoryId: String) = listingDao.getListingsByCategory(categoryId)
        .map { entities -> entities.map { it.toListing() } }

    /**
     * Get listings for a specific user.
     */
    fun getListingsByUser(userId: String): Flow<List<Listing>> {
        return listingDao.getListingsByUser(userId)
            .map { entities -> entities.map { it.toListing() } }
    }

    fun searchWithFilters(
        query: String,
        categoryIds: List<String>,
        minPrice: Double,
        maxPrice: Double,
        minDate: Long,
        maxDate: Long
    ): Flow<List<Listing>> = listingDao.searchWithFilters(
        query = query,
        categoryIds = categoryIds,
        categoryIdsSize = categoryIds.size,
        minPrice = minPrice,
        maxPrice = maxPrice,
        minDate = minDate,
        maxDate = maxDate
    ).map { entities -> entities.map { it.toListing() } }

    suspend fun getAllCategories() = listingDao.getAllCategories()
    suspend fun markListingAsSold(listingId: String) = listingDao.markAsSold(listingId)
    suspend fun deleteListing(listing: Listing) = listingDao.delete(listing.toEntity())
}

/**
 * Mapping Helpers
 */
private fun ListingEntity.toListing() = Listing(
    id = id,
    title = title,
    price = price,
    category = categoryId,
    sellerId = userId,
    sellerName = sellerName,
    description = description,
    imageUrl = imageUrls.split(",").firstOrNull()
)

private fun Listing.toEntity(
    uid: String = "anonymous", 
    sName: String = "Anonymous"
) = ListingEntity(
    id = id, 
    title = title, 
    description = description ?: "", 
    price = price, 
    userId = uid, 
    sellerName = sName, 
    categoryId = category, 
    location = null, 
    imageUrls = imageUrl ?: "", 
    createdAt = System.currentTimeMillis(), 
    isSold = false
)
