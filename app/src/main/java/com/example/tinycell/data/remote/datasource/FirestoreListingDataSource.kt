package com.example.tinycell.data.remote.datasource

import com.example.tinycell.data.remote.model.ListingDto
import com.example.tinycell.data.model.Listing
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore Remote Data Source for Listings.
 * [PHASE 2]: Raw Firestore operations decoupled from the Repository.
 */
class FirestoreListingDataSource(
    private val firestore: FirebaseFirestore
) {
    private val listingsCollection = firestore.collection("listings")

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

    suspend fun fetchListingById(id: String): Listing? {
        return try {
            val snapshot = listingsCollection.document(id).get().await()
            snapshot.toObject(ListingDto::class.java)?.copy(id = snapshot.id)?.toListing()
        } catch (e: Exception) {
            null
        }
    }

    private fun ListingDto.toListing(): Listing {
        return Listing(
            id = this.id,
            title = this.title.ifBlank { "Untitled Listing" },
            price = this.price,
            category = this.categoryId.ifBlank { "General" },
            sellerId = this.userId,
            sellerName = this.sellerName.ifBlank { "Unknown Seller" },
            description = this.description,
            imageUrls = this.imageUrls, // [FIXED]: Mapping the full list for Carousel support
            isSold = this.isSold,
            status = this.status,
            createdAt = this.createdAt
        )
    }
}
