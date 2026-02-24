package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.dao.*
import com.example.tinycell.data.local.entity.*
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.remote.model.OfferDto
import com.example.tinycell.data.remote.model.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TAG = "ListingRepository"

/**
 * [FINAL_STABILITY_FIX]: Corrected Lifecycle and Permission handling for Admin Switch and Master Account.
 */
class ListingRepository(
    private val listingDao: ListingDao,
    private val userDao: UserDao,
    private val offerDao: OfferDao,
    private val reviewDao: ReviewDao,
    private val notificationDao: NotificationDao,
    private val favouriteDao: FavouriteDao,
    private val remoteRepo: RemoteListingRepository,
    private val remoteNotificationRepo: RemoteNotificationRepository,
    private val imageRepo: RemoteImageRepository,
    private val authRepo: AuthRepository
) {

    // Helper to check if current user is the Master Admin (sLtu639XdRRxQot8gSEQ9IlKA002)
    private fun isMasterUser(): Boolean {
        // We use the real UID from Auth, not the debug override
        val realUid = (authRepo as? FirebaseAuthRepositoryImpl)?.userIdFlow?.value ?: authRepo.getCurrentUserId()
        return realUid == "sLtu639XdRRxQot8gSEQ9IlKA002"
    }

    /**
     * [OFFER_SYSTEM]: Make a new formal offer.
     */
    suspend fun makeOffer(listingId: String, amount: Double, offerId: String = UUID.randomUUID().toString()) = withContext(Dispatchers.IO) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val currentName = authRepo.getCurrentUserName() ?: "Anonymous"
        
        Log.d(TAG, "OFFER_DEBUG: Initiating offer for listing: $listingId by user: $currentUid")

        val listing = listingDao.getListingById(listingId) ?: remoteRepo.getListingById(listingId)?.toEntity()?.also { listingDao.insert(it) }
        if (listing == null) return@withContext

        ensureUserExistsLocally(currentUid, currentName)
        ensureUserExistsLocally(listing.userId, listing.sellerName)
        
        val offerDto = OfferDto(
            id = offerId, listingId = listingId, buyerId = currentUid, sellerId = listing.userId,
            amount = amount, status = "PENDING", timestamp = System.currentTimeMillis()
        )
        offerDao.insert(offerDto.toEntity())
        remoteRepo.sendOffer(offerDto)

        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(), userId = listing.userId, title = "New Offer!",
            message = "$currentName offered $${"%.2f".format(amount)} for ${listing.title}",
            type = "OFFER_MADE", referenceId = listingId, timestamp = System.currentTimeMillis()
        )
        notificationDao.insert(notification)
        remoteNotificationRepo.sendNotification(notification)
    }

    /**
     * [LIFECYCLE]: Accepting an offer.
     * [FIX]: Master user can always accept any offer for testing.
     */
    suspend fun acceptOffer(offerId: String) = withContext(Dispatchers.IO) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val offer = offerDao.getOfferById(offerId) ?: return@withContext
        
        Log.d(TAG, "OFFER_DEBUG: acceptOffer triggered by user: $currentUid for offer ${offer.id}")
        
        // Ensure only the Seller OR the Master account can accept
        if (currentUid != offer.sellerId && !isMasterUser()) {
            Log.e(TAG, "OFFER_DEBUG: Permission Denied. You are not the seller of this item.")
            return@withContext
        }

        val result = remoteRepo.updateOfferStatus(offerId, "ACCEPTED")
        
        if (result.isSuccess) {
            offerDao.updateStatus(offerId, "ACCEPTED")
            listingDao.markAsReserved(offer.listingId)
            
            val listing = listingDao.getListingById(offer.listingId)
            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(), userId = offer.buyerId, title = "Offer Accepted!",
                message = "Your offer for ${listing?.title ?: "an item"} was accepted!",
                type = "OFFER_ACCEPTED", referenceId = offer.listingId, timestamp = System.currentTimeMillis()
            )
            notificationDao.insert(notification)
            remoteNotificationRepo.sendNotification(notification)
            Log.d(TAG, "OFFER_DEBUG: Acceptance complete.")
        } else {
            Log.e(TAG, "OFFER_DEBUG: Remote write failed: ${result.exceptionOrNull()?.message}")
        }
    }

    suspend fun completeTransaction(listingId: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "OFFER_DEBUG: Completing sale for listing: $listingId")
        listingDao.markAsSold(listingId)
    }

    suspend fun updateListingPrice(listingId: String, newPrice: Double) = withContext(Dispatchers.IO) {
        val listing = listingDao.getListingById(listingId) ?: return@withContext
        if (newPrice != listing.price) {
            listingDao.update(listing.copy(price = newPrice))
            val watchers = favouriteDao.getWatchersForListing(listingId).first()
            watchers.forEach { fav ->
                val notification = NotificationEntity(id = UUID.randomUUID().toString(), userId = fav.userId, title = "Price Changed", message = "An item you like just updated its price.", type = "PRICE_CHANGE", referenceId = listingId, timestamp = System.currentTimeMillis())
                notificationDao.insert(notification)
                remoteNotificationRepo.sendNotification(notification)
            }
        }
    }

    fun startNotificationSync(scope: CoroutineScope) {
        val userId = authRepo.getCurrentUserId() ?: return
        scope.launch(Dispatchers.IO) {
            remoteNotificationRepo.getNotificationsFlow(userId).collectLatest { cloudNotifications ->
                notificationDao.insertAll(cloudNotifications)
            }
        }
    }

    fun getNotifications() = notificationDao.getNotificationsForUser(authRepo.getCurrentUserId() ?: "")
    fun getUnreadNotificationCount() = notificationDao.getUnreadCount(authRepo.getCurrentUserId() ?: "")
    suspend fun markNotificationsRead() = notificationDao.markAllAsRead(authRepo.getCurrentUserId() ?: "")

    suspend fun submitReview(listingId: String, reviewerId: String, revieweeId: String, rating: Int, comment: String, role: String) = withContext(Dispatchers.IO) {
        val review = ReviewEntity(id = UUID.randomUUID().toString(), listingId = listingId, reviewerId = reviewerId, revieweeId = revieweeId, rating = rating, comment = comment, timestamp = System.currentTimeMillis(), role = role)
        reviewDao.insert(review)
    }

    fun getReviewsForUser(userId: String) = reviewDao.getReviewsForUser(userId)
    fun getAverageRating(userId: String) = reviewDao.getAverageRating(userId)
    fun getReviewForTransaction(reviewerId: String, listingId: String) = reviewDao.getReviewByReviewerAndListing(reviewerId, listingId)

    suspend fun rejectOffer(offerId: String) = withContext(Dispatchers.IO) {
        offerDao.updateStatus(offerId, "REJECTED")
        remoteRepo.updateOfferStatus(offerId, "REJECTED")
    }

    fun getOffersForListing(listingId: String): Flow<List<OfferEntity>> = offerDao.getOffersByListing(listingId)

    fun startRealTimeSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            remoteRepo.getRemoteListings().collectLatest { remoteDtos ->
                try {
                    remoteDtos.map { it.userId to it.sellerName }.distinct().forEach { (uid, name) -> ensureUserExistsLocally(uid, name) }
                    listingDao.insertAll(remoteDtos.map { it.toEntity() })
                } catch (e: Exception) { Log.e(TAG, "Sync error: ${e.message}") }
            }
        }
    }

    val activeListings: Flow<List<Listing>> = listingDao.getActiveListings().map { entities -> entities.map { it.toListing() } }

    suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        try {
            val remoteListings = remoteRepo.fetchListings()
            remoteListings.map { it.userId to it.sellerName }.distinct().forEach { (uid, name) -> ensureUserExistsLocally(uid, name) }
            listingDao.insertAll(remoteListings.map { it.toEntity() })
        } catch (e: Exception) { Log.e(TAG, "Sync failed: ${e.message}") }
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
                if (uploadResult.isSuccess) {
                    finalEntity = secureEntity.copy(imageUrls = uploadResult.getOrThrow().joinToString(","))
                    listingDao.update(finalEntity)
                }
            }
            remoteRepo.uploadListing(finalEntity.toDto()).getOrThrow()
        } catch (e: Exception) { Log.e(TAG, "Remote write failed: ${e.message}") }
    }

    suspend fun createNewListing(title: String, price: Double, description: String, category: String, imagePaths: List<String>, location: String? = null) {
        val currentUid = authRepo.getCurrentUserId() ?: "anonymous"
        val currentName = authRepo.getCurrentUserName() ?: "Anonymous"
        val newListing = Listing(id = UUID.randomUUID().toString(), title = title, price = price, category = category, sellerId = currentUid, sellerName = currentName, description = description, imageUrls = imagePaths, location = location, createdAt = System.currentTimeMillis())
        createListing(newListing.toEntity(currentUid, currentName))
    }

    private suspend fun ensureUserExistsLocally(uid: String, name: String) {
        if (uid == "anonymous") return
        val existingUser = userDao.getUserById(uid)
        if (existingUser == null) {
            userDao.insert(UserEntity(id = uid, name = name.ifBlank { "User_$uid" }, email = "", createdAt = System.currentTimeMillis()))
        } else if (existingUser.name != name && name.isNotBlank()) {
            userDao.insert(existingUser.copy(name = name))
        }
    }

    suspend fun getListingById(id: String): Listing? = listingDao.getListingById(id)?.toListing()
    fun getListingFlow(id: String): Flow<Listing?> = listingDao.getListingFlow(id).map { it?.toListing() }
    fun getListingsByUser(userId: String): Flow<List<Listing>> = listingDao.getListingsByUser(userId).map { entities -> entities.map { it.toListing() } }

    fun searchWithFilters(query: String, categoryIds: List<String>, minPrice: Double, maxPrice: Double, minDate: Long, maxDate: Long): Flow<List<Listing>> {
        return listingDao.searchWithFilters(query, categoryIds, categoryIds.size, minPrice, maxPrice, minDate, maxDate).map { entities -> entities.map { it.toListing() } }
    }

    suspend fun getAllCategories() = listingDao.getAllCategories()
    suspend fun markListingAsSold(listingId: String) = listingDao.markAsSold(listingId)
    suspend fun deleteListing(listing: Listing) = listingDao.delete(listing.toEntity(listing.sellerId, listing.sellerName))
}

/**
 * Mapping Helpers
 */
private fun ListingEntity.toListing() = Listing(
    id = id, title = title, price = price, category = categoryId, sellerId = userId, sellerName = sellerName,
    description = description, imageUrls = if (imageUrls.isEmpty()) emptyList() else imageUrls.split(","), location = location,
    isSold = isSold, status = status, createdAt = createdAt
)

private fun Listing.toEntity(uid: String, sName: String) = ListingEntity(
    id = id, title = title, description = description ?: "", price = price, userId = uid, sellerName = sName,
    categoryId = category, location = location, imageUrls = imageUrls.joinToString(","), createdAt = createdAt,
    isSold = isSold, status = status
)

private fun OfferDto.toEntity() = OfferEntity(
    id = id, listingId = listingId, buyerId = buyerId, sellerId = sellerId, amount = amount, status = status, timestamp = timestamp
)
