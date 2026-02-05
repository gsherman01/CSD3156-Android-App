package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.dao.*
import com.example.tinycell.data.local.entity.*
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.remote.model.ListingDto
import com.example.tinycell.data.remote.model.OfferDto
import com.example.tinycell.data.remote.model.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ListingRepository"

/**
 * [FINAL VERSION]: Updated ListingRepository.
 * Optimized for performance with Room SSOT, Pagination, and Formal Offers.
 */
class ListingRepository(
    private val listingDao: ListingDao,
    private val userDao: UserDao,
    private val offerDao: OfferDao,
    private val remoteRepo: RemoteListingRepository,
    private val imageRepo: RemoteImageRepository,
    private val authRepo: AuthRepository
) {

    /**
     * [OFFER_SYSTEM]: Make a new formal offer.
     * [FIX]: Added id parameter to ensure consistency with Chat messages.
     */
    suspend fun makeOffer(listingId: String, amount: Double, offerId: String = java.util.UUID.randomUUID().toString()) = withContext(Dispatchers.IO) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val listing = listingDao.getListingById(listingId) ?: return@withContext
        
        val offerDto = OfferDto(
            id = offerId,
            listingId = listingId,
            buyerId = currentUid,
            sellerId = listing.userId,
            amount = amount,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        
        Log.d(TAG, "Making offer: $offerId for listing: $listingId")
        offerDao.insert(offerDto.toEntity())
        
        remoteRepo.sendOffer(offerDto).onFailure {
            Log.e(TAG, "Failed to send offer to cloud: ${it.message}")
        }
    }

    suspend fun acceptOffer(offerId: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Attempting to accept offer: $offerId")
        offerDao.updateStatus(offerId, "ACCEPTED")
        val result = remoteRepo.updateOfferStatus(offerId, "ACCEPTED")
        
        if (result.isSuccess) {
            val offer = offerDao.getOfferById(offerId)
            offer?.let { 
                Log.d(TAG, "Offer accepted successfully. Marking listing ${it.listingId} as SOLD.")
                listingDao.markAsSold(it.listingId) 
            }
        } else {
            Log.e(TAG, "Remote accept failed: ${result.exceptionOrNull()?.message}")
        }
    }

    suspend fun rejectOffer(offerId: String) = withContext(Dispatchers.IO) {
        offerDao.updateStatus(offerId, "REJECTED")
        remoteRepo.updateOfferStatus(offerId, "REJECTED")
    }

    fun getOffersForListing(listingId: String): Flow<List<OfferEntity>> = offerDao.getOffersByListing(listingId)

    /**
     * [REAL_TIME_BRIDGE]: Automatically sinks Cloud updates into Room.
     */
    fun startRealTimeSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            remoteRepo.getRemoteListings().collectLatest { remoteDtos ->
                try {
                    remoteDtos.map { it.userId to it.sellerName }.distinct().forEach { (uid, name) ->
                        ensureUserExistsLocally(uid, name)
                    }
                    listingDao.insertAll(remoteDtos.map { it.toEntity() })
                } catch (e: Exception) {
                    Log.e(TAG, "Real-time sync error: ${e.message}")
                }
            }
        }
    }

    /**
     * Observable Flows for UI - Room is the Single Source of Truth
     */
    val allListings: Flow<List<Listing>> = listingDao.getAllListings().map { entities -> entities.map { it.toListing() } }
    val activeListings: Flow<List<Listing>> = listingDao.getActiveListings().map { entities -> entities.map { it.toListing() } }

    fun getActiveListingsPaged(lastTimestamp: Long, pageSize: Int): Flow<List<Listing>> {
        return listingDao.getActiveListingsBatch(lastTimestamp, pageSize).map { entities -> entities.map { it.toListing() } }
    }

    suspend fun syncBatchFromRemote(pageSize: Int, lastTimestamp: Long?) = withContext(Dispatchers.IO) {
        try {
            val remoteListings = remoteRepo.fetchListingsBatch(pageSize, lastTimestamp)
            remoteListings.map { it.userId to it.sellerName }.distinct().forEach { (uid, name) -> ensureUserExistsLocally(uid, name) }
            listingDao.insertAll(remoteListings.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync failed: ${e.message}")
        }
    }

    suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        try {
            val remoteListings = remoteRepo.fetchListings()
            remoteListings.map { it.userId to it.sellerName }.distinct().forEach { (uid, name) -> ensureUserExistsLocally(uid, name) }
            listingDao.insertAll(remoteListings.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
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

    suspend fun createNewListing(title: String, price: Double, description: String, category: String, imagePaths: List<String>) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val currentName = authRepo.getCurrentUserName() ?: "Anonymous"
        val newListing = Listing(id = java.util.UUID.randomUUID().toString(), title = title, price = price, category = category, sellerId = currentUid, sellerName = currentName, description = description, imageUrl = imagePaths.joinToString(","))
        createListing(newListing.toEntity(currentUid, currentName))
    }

    private suspend fun ensureUserExistsLocally(uid: String, name: String) {
        val existingUser = userDao.getUserById(uid)
        if (existingUser == null) {
            userDao.insert(UserEntity(id = uid, name = name.ifBlank { "User_$uid" }, email = "", createdAt = System.currentTimeMillis()))
        } else if (existingUser.name != name && name.isNotBlank()) {
            userDao.insert(existingUser.copy(name = name))
        }
    }

    suspend fun getListingById(id: String): Listing? = listingDao.getListingById(id)?.toListing()
    fun getListingsByCategory(categoryId: String) = listingDao.getListingsByCategory(categoryId).map { entities -> entities.map { it.toListing() } }
    fun getListingsByUser(userId: String): Flow<List<Listing>> = listingDao.getListingsByUser(userId).map { entities -> entities.map { it.toListing() } }

    /**
     * Search listings with multiple filters.
     */
    fun searchWithFilters(
        query: String,
        categoryIds: List<String>,
        minPrice: Double,
        maxPrice: Double,
        minDate: Long,
        maxDate: Long
    ): Flow<List<Listing>> {
        return listingDao.searchWithFilters(
            query = query,
            categoryIds = categoryIds,
            categoryIdsSize = categoryIds.size,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minDate = minDate,
            maxDate = maxDate
        ).map { entities -> entities.map { it.toListing() } }
    }

    suspend fun getAllCategories() = listingDao.getAllCategories()
    suspend fun markListingAsSold(listingId: String) = listingDao.markAsSold(listingId)
    suspend fun deleteListing(listing: Listing) = listingDao.delete(listing.toEntity(listing.sellerId, listing.sellerName))
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
    imageUrl = imageUrls.split(",").firstOrNull(),
    isSold = isSold,
    status = status
)

private fun Listing.toEntity(uid: String, sName: String) = ListingEntity(
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
    isSold = isSold,
    status = status
)

private fun OfferDto.toEntity() = OfferEntity(
    id = id, listingId = listingId, buyerId = buyerId, sellerId = sellerId, amount = amount, status = status, timestamp = timestamp
)
