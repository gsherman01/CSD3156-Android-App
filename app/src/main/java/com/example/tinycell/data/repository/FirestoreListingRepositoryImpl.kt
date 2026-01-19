package com.example.tinycell.data.repository

import com.example.tinycell.data.remote.model.ListingDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * [PHASE 3]: Firestore Implementation of RemoteListingRepository.
 * Handles the actual calls to the Firebase SDK.
 */
class FirestoreListingRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RemoteListingRepository {

    private val listingsCollection = firestore.collection("listings")

    /**
     * [LEARNING_POINT: SNAPSHOT LISTENERS]
     * Provides real-time updates as they happen in the cloud.
     */
    override fun getRemoteListings(): Flow<List<ListingDto>> = callbackFlow {
        val subscription = listingsCollection
            .whereEqualTo("isSold", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.toObjects(ListingDto::class.java) ?: emptyList()
                trySend(items)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * [LEARNING_POINT: ONE-SHOT FETCH]
     * Used for initial sync to avoid keeping a long-lived listener if not needed.
     */
    override suspend fun fetchListings(): List<ListingDto> {
        return try {
            val snapshot = listingsCollection.whereEqualTo("isSold", false).get().await()
            snapshot.toObjects(ListingDto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun uploadListing(listing: ListingDto): Result<Unit> = try {
        // Use ID from DTO if provided, otherwise Firestore auto-generates
        val docRef = if (listing.id.isBlank()) listingsCollection.document() else listingsCollection.document(listing.id)
        val finalListing = if (listing.id.isBlank()) listing.copy(id = docRef.id) else listing
        
        docRef.set(finalListing).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getListingById(id: String): ListingDto? {
        return try {
            val snapshot = listingsCollection.document(id).get().await()
            snapshot.toObject(ListingDto::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
