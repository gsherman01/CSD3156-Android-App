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
 * Includes all necessary CRUD operations and SQL 787 safety checks.
 */
class ListingRepository(
    private val listingDao: ListingDao,
    private val userDao: UserDao,
    private val remoteRepo: RemoteListingRepository,
    private val imageRepo: RemoteImageRepository,
    private val authRepo: AuthRepository
) {

    /**
     * Ensures any user referenced in remote listings exists locally to avoid FK errors.
     */
    suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
        }
    }

    /**
     * Observable Flows for UI
     */
    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
        .map { entities -> entities.map { it.toListing() } }

    val activeListings: Flow<List<Listing>> = listingDao.getActiveListings()
        .map { entities -> entities.map { it.toListing() } }

    /**
     * Single Fetch Operations
     */
    suspend fun getListingById(id: String): Listing? {
        return listingDao.getListingById(id)?.toListing()
    }

    /**
     * [PHASE 4/5]: Dual Write Logic
     */
    suspend fun createListing(entity: ListingEntity) = withContext(Dispatchers.IO) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val currentName = authRepo.getCurrentUserName() ?: "Anonymous"
        
        ensureUserExistsLocally(currentUid, currentName)

        val secureEntity = entity.copy(userId = currentUid, sellerName = currentName)
        listingDao.insert(secureEntity)

        try {
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
            Log.e(TAG, "Remote write failed: ${e.message}")
        }
    }

    private suspend fun ensureUserExistsLocally(uid: String, name: String) {
        if (userDao.getUserById(uid) == null) {
            userDao.insert(UserEntity(
                id = uid,
                name = name.ifBlank { "User_$uid" },
                email = "",
                createdAt = System.currentTimeMillis()
            ))
        }
    }

    /**
     * Filtering & Search
     */
    fun getListingsByCategory(categoryId: String) = listingDao.getListingsByCategory(categoryId)
        .map { entities -> entities.map { it.toListing() } }

    fun getListingsByUser(userId: String) = listingDao.getListingsByUser(userId)
        .map { entities -> entities.map { it.toListing() } }

    fun searchListings(query: String) = listingDao.searchListings(query)
        .map { entities -> entities.map { it.toListing() } }

    /**
     * Advanced search with multiple filters
     */
    fun searchWithFilters(
        query: String,
        categoryId: String,
        minPrice: Double,
        maxPrice: Double,
        minDate: Long,
        maxDate: Long
    ): Flow<List<Listing>> = listingDao.searchWithFilters(
        query = query,
        categoryId = categoryId,
        minPrice = minPrice,
        maxPrice = maxPrice,
        minDate = minDate,
        maxDate = maxDate
    ).map { entities -> entities.map { it.toListing() } }

    /**
     * Get all categories for filter UI
     */
    suspend fun getAllCategories() = listingDao.getAllCategories()

    /**
     * Standard CRUD
     */
    suspend fun markListingAsSold(listingId: String) = listingDao.markAsSold(listingId)

    suspend fun deleteListing(listing: Listing) = listingDao.delete(listing.toEntity())

    suspend fun createNewListing(title: String, price: Double, description: String, category: String, imagePaths: List<String>) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val currentName = authRepo.getCurrentUserName() ?: "Anonymous"
        val newListing = Listing(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            price = price,
            category = category.ifBlank { "General" },
            sellerName = currentName,
            description = description,
            imageUrl = imagePaths.joinToString(",")
        )
        createListing(newListing.toEntity(currentUid, currentName))
    }
}

/**
 * Mapping Helpers
 */
private fun ListingEntity.toListing() = Listing(
    id = id, 
    title = title, 
    price = price, 
    category = categoryId, 
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
