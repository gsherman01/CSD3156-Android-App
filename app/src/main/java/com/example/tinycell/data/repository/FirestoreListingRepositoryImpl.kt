package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.remote.model.ListingDto
import com.example.tinycell.data.remote.model.OfferDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRemote"

/**
 * [PHASE 3/6]: Firestore Implementation of RemoteListingRepository.
 * Handles listings and the formal offer system.
 */
class FirestoreListingRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RemoteListingRepository {

    private val listingsCollection = firestore.collection("listings")
    private val offersCollection = firestore.collection("offers")

    override fun getRemoteListings(): Flow<List<ListingDto>> = callbackFlow {
        val subscription = listingsCollection
            .whereEqualTo("isSold", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        Log.e(TAG, "INDEX REQUIRED: Check Logcat for URL.")
                    }
                    Log.e(TAG, "Remote listener error: ${error.message}")
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(ListingDto::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun fetchListings(): List<ListingDto> {
        return try {
            val snapshot = listingsCollection
                .whereEqualTo("isSold", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(ListingDto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchListingsBatch(pageSize: Int, lastTimestamp: Long?): List<ListingDto> {
        return try {
            var query = listingsCollection
                .whereEqualTo("isSold", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
            if (lastTimestamp != null && lastTimestamp > 0) {
                query = query.startAfter(lastTimestamp)
            }
            val snapshot = query.get().await()
            snapshot.toObjects(ListingDto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun uploadListing(listing: ListingDto): Result<Unit> = try {
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

    /**
     * [PHASE 6]: Offer System - Implementation
     */
    override suspend fun sendOffer(offer: OfferDto): Result<Unit> = try {
        // [FIX]: Ensure the document ID in Firestore matches the offer.id exactly.
        val offerId = offer.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val finalOffer = if (offer.id.isBlank()) offer.copy(id = offerId) else offer
        
        Log.d(TAG, "Cloud: Creating offer document: $offerId")
        offersCollection.document(offerId).set(finalOffer).await()
        
        Log.d(TAG, "Cloud: SUCCESS: Offer document created.")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Cloud: FAILURE: Could not create offer: ${e.message}")
        Result.failure(e)
    }

    override suspend fun updateOfferStatus(offerId: String, status: String): Result<Unit> = try {
        Log.d(TAG, "Cloud: Updating offer $offerId status to $status")
        
        // Use set with merge or update. update() fails if doc doesn't exist (NOT_FOUND).
        // Using set(merge=true) is safer for eventual consistency.
        offersCollection.document(offerId).update("status", status).await()
        
        Log.d(TAG, "Cloud: SUCCESS: Offer status updated.")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Cloud: FAILURE: Could not update offer: ${e.message}")
        Result.failure(e)
    }

    override fun getOffersForListing(listingId: String): Flow<List<OfferDto>> = callbackFlow {
        val subscription = offersCollection
            .whereEqualTo("listingId", listingId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(OfferDto::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }
}
