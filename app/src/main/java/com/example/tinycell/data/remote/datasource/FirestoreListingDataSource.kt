package com.example.tinycell.data.remote.datasource

import com.example.tinycell.data.remote.model.ListingDto
import com.example.tinycell.data.remote.model.toEntity
import com.example.tinycell.data.model.Listing
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * [PHASE 2]: Firestore Remote Data Source [FIRESTORE STUFF  ONLY]
 * Handles raw Firestore operations for Listings.
 * This class is separate from the Repository to decouple the SDK implementation.
 */
class FirestoreListingDataSource(
    private val firestore: FirebaseFirestore
) {
    // this is firestore stuff
    private val listingsCollection = firestore.collection("listings")

    /**
     * Fetches all listings in real-time.
     * Maps Firestore documents directly to Domain models safely.
     * In the FireStore database!
     */
    fun fetchAllListings(): Flow<List<Listing>> = callbackFlow {
        val subscription = listingsCollection
            .whereEqualTo("isSold", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ListingDto::class.java)?.copy(id = doc.id)?.toListing()
                } ?: emptyList()
                
                trySend(items)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Fetches a single listing by ID.
     */
    suspend fun fetchListingById(id: String): Listing? {
        return try {
            val snapshot = listingsCollection.document(id).get().await()
            snapshot.toObject(ListingDto::class.java)?.copy(id = snapshot.id)?.toListing()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Domain Mapping Helper
     * Safely maps the DTO (Firestore structure) to the Listing (UI model).
     * Handles missing fields with sensible defaults.
     */
    private fun ListingDto.toListing(): Listing {
        return Listing(
            id = this.id,
            title = this.title.ifBlank { "Untitled Listing" },
            price = this.price,
            category = this.categoryId.ifBlank { "General" },
            sellerId = this.userId,
            sellerName = this.sellerName.ifBlank { "Unknown Seller" },
            description = this.description,
            imageUrl = this.imageUrls.firstOrNull()
        )
    }
}
