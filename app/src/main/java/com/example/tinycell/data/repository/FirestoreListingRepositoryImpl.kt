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
 * Firestore Implementation of RemoteListingRepository.
 * [STABILITY UPDATED]: Handles missing indices without crashing the app.
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
                        Log.e(TAG, "CRITICAL: Firestore Index Required. Use the link in Logcat to create it.")
                    } else {
                        Log.e(TAG, "Remote listener error: ${error.message}")
                    }
                    // [STABILITY]: Don't close the flow, just send an empty list so the app doesn't crash
                    trySend(emptyList())
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
            Log.e(TAG, "Fetch listings failed (Likely missing index): ${e.message}")
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
            Log.e(TAG, "Fetch listings batch failed: ${e.message}")
            emptyList()
        }
    }

    override suspend fun uploadListing(listing: ListingDto): Result<Unit> = try {
        val docId = listing.id.ifBlank { listingsCollection.document().id }
        val finalListing = if (listing.id.isBlank()) listing.copy(id = docId) else listing
        listingsCollection.document(docId).set(finalListing).await()
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

    override suspend fun sendOffer(offer: OfferDto): Result<Unit> = try {
        val offerId = offer.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val finalOffer = if (offer.id.isBlank()) offer.copy(id = offerId) else offer
        offersCollection.document(offerId).set(finalOffer).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateOfferStatus(offerId: String, status: String): Result<Unit> = try {
        offersCollection.document(offerId).update("status", status).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getOffersForListing(listingId: String): Flow<List<OfferDto>> = callbackFlow {
        val subscription = offersCollection
            .whereEqualTo("listingId", listingId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(OfferDto::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }
}
