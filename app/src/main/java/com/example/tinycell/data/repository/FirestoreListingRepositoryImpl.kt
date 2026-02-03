package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.remote.model.ListingDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRemote"

/**
 * [PHASE 3]: Firestore Implementation of RemoteListingRepository.
 * Offer cloud operations have been extracted to FirestoreOfferRepositoryImpl.
 */
class FirestoreListingRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RemoteListingRepository {

    private val listingsCollection = firestore.collection("listings")

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
     * [PHASE 6]: Targeted update of a listing's status and isSold flag.
     * Avoids a full set() so that other fields written by other clients are not clobbered.
     */
    override suspend fun updateListingStatus(listingId: String, status: String, isSold: Boolean): Result<Unit> = try {
        Log.d(TAG, "Cloud: Updating listing $listingId -> status=$status, isSold=$isSold")
        listingsCollection.document(listingId).update(
            mapOf("status" to status, "isSold" to isSold)
        ).await()
        Log.d(TAG, "Cloud: SUCCESS: Listing status updated.")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Cloud: FAILURE: Could not update listing status: ${e.message}")
        Result.failure(e)
    }
}
