package com.example.tinycell.data.repository

import com.example.tinycell.data.local.dao.ListingDao
import com.example.tinycell.data.local.entity.ListingEntity
import com.example.tinycell.data.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//lol why the difference
//import com.example.tinycell.data.local.dao.AppDao
//import com.example.tinycell.data.local.entity.AppEntity // Assuming ListingEntity is scaffolded similarly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
/**
 * // Week1
 * LISTING REPOSITORY
 *
 * Repository pattern implementation for marketplace listings.
 * Handles data operations using Room database for local persistence.
 *
 * Current: Room database for local storage
 * Future: Add Retrofit API for remote sync (coordinate with Member 4 - Networking)
 */
/**
 * //  Week2 Friday
 * TODO: Camera Integrator Hook
 * - [IMAGE_URI]: Use the URI provided by the CameraX implementation here.
 * - [FILE_SYSTEM]: Implement logic to copy the temporary camera file to the app's internal storage
 *   to ensure the image persists after the cache is cleared.
 */
class ListingRepository(private val listingDao: ListingDao) {

    /**
     * Get all marketplace listings as reactive Flow.
     * UI can observe this to automatically update when data changes.
     */
    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
        .map { entities -> entities.map { it.toListing() } }

    /**
     * Get active (unsold) listings only.
     */
    val activeListings: Flow<List<Listing>> = listingDao.getActiveListings()
        .map { entities -> entities.map { it.toListing() } }

    /**
     * Get a single listing by ID.
     */
    suspend fun getListingById(id: String): Listing? {
        return listingDao.getListingById(id)?.toListing()
    }

    /**
     * Get listings filtered by category.
     */
    fun getListingsByCategory(categoryId: String): Flow<List<Listing>> {
        return listingDao.getListingsByCategory(categoryId)
            .map { entities -> entities.map { it.toListing() } }
    }

    /**
     * Get listings created by a specific user.
     */
    fun getListingsByUser(userId: String): Flow<List<Listing>> {
        return listingDao.getListingsByUser(userId)
            .map { entities -> entities.map { it.toListing() } }
    }

    /**
     * Search listings by title or description.
     */
    fun searchListings(query: String): Flow<List<Listing>> {
        return listingDao.searchListings(query)
            .map { entities -> entities.map { it.toListing() } }
    }

    /**
     * Insert a new listing into the database.
     */
    suspend fun insertListing(listing: Listing) {
        listingDao.insert(listing.toEntity())
    }

    /**
     * Insert multiple listings (for seeding/bulk operations).
     */
    suspend fun insertListings(listings: List<Listing>) {
        listingDao.insertAll(listings.map { it.toEntity() })
    }

    /**
     * Update an existing listing.
     */
    suspend fun updateListing(listing: Listing) {
        listingDao.update(listing.toEntity())
    }

    /**
     * Mark a listing as sold.
     */
    suspend fun markListingAsSold(listingId: String) {
        listingDao.markAsSold(listingId)
    }

    /**
     * Delete a listing.
     */
    suspend fun deleteListing(listing: Listing) {
        listingDao.delete(listing.toEntity())
    }

    /**
     * Get count of listings by user.
     */
    suspend fun getListingCountByUser(userId: String): Int {
        return listingDao.getListingCountByUser(userId)
    }


    /**
     * Logic for the new Create Listing Feature
     * One Responsibility: Saving a new listing with image paths
     */
    /**
     * [TODO_DATABASE_INTEGRATION]:
     * - PROBLEM: SQLite Error 787 (Foreign Key Constraint).
     * - ACTION: Ensure 'userId' and 'categoryId' exist in their respective tables
     *   before calling listingDao.insert().
     * - FIX: Implement a check here or seed the database with default values
     *   (e.g., a default user and a "General" category).
     */
    suspend fun createNewListing(
        title: String,
        price: Double,
        description: String,
        category: String,
        imagePaths: List<String>
    ) = withContext(Dispatchers.IO) {
        // [TODO_DATABASE_INTEGRATION]:
        // To resolve Error 787, ensure these IDs are seeded in AppDatabase
        val sellerId = "user_1"; // assiocated to a user account.
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

        try {
            listingDao.insert(newListing.toEntity())
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // [TODO_ERROR_HANDLING]: Log this specifically as a Foreign Key violation
            throw Exception("Foreign Key Violation: Ensure User '$sellerId' and Category '$catId' exist.")
        }
    }//end  of function
}// end of  class

/**
 * Extension function: Convert ListingEntity (database) to Listing (UI model).
 */
private fun ListingEntity.toListing(): Listing {
    return Listing(
        id = id,
        title = title,
        price = price,
        category = categoryId,  // TODO: Convert categoryId to category name (coordinate with Category repository)
        sellerName = userId,    // TODO: Convert userId to seller name (coordinate with User repository)
        description = description,
        imageUrl = imageUrls.split(",").firstOrNull()?.takeIf { it.isNotBlank() }  // Get first image URL
        // imageUrl = imageUrls //suggested change as you want an image carousel eventually(?)
    )
}

/**
 * Extension function: Convert Listing (UI model) to ListingEntity (database).
 */
private fun Listing.toEntity(): ListingEntity {
    return ListingEntity(
        id = id,
        title = title,
        description = description ?: "",
        price = price,
        userId = "user_1",  // TODO: Get from authentication system when implemented
        categoryId = category,  // Assuming category is the ID for now
        location = null,  // TODO: Add location field to Listing model when location feature is implemented
        // If the UI model only has one image, we save it;
        // the createNewListing function handles joining multiple paths.
        imageUrls = imageUrl ?: "",
        createdAt = System.currentTimeMillis(),
        isSold = false
    )
}
