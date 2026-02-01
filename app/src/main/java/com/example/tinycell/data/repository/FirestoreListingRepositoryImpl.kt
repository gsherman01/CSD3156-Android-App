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
 * Optimized for real-time batched loading and sync strategy.
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
                    // Check if a composite index is required
                    if (error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        Log.e(TAG, "INDEX REQUIRED: Check Logcat for URL to create composite index.")
                    }
                    Log.e(TAG, "Remote listener error: ${error.message}")
                    return@addSnapshotListener
                }

                val items = snapshot?.toObjects(ListingDto::class.java) ?: emptyList()
                Log.d(TAG, "Remote listener: Received ${items.size} active items from Cloud")
                trySend(items)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun fetchListings(): List<ListingDto> {
        return try {
            Log.d(TAG, "Fetching active listings (isSold == false) from Firestore...")
            val snapshot = listingsCollection
                .whereEqualTo("isSold", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val items = snapshot.toObjects(ListingDto::class.java)
            Log.d(TAG, "Successfully fetched ${items.size} active listings from Firestore")
            items
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch listings: ${e.message}")
            emptyList()
        }
    }

    override suspend fun fetchListingsBatch(pageSize: Int, lastTimestamp: Long?): List<ListingDto> {
        return try {
            Log.d(TAG, "Fetching batch of $pageSize listings from Firestore...")
            var query = listingsCollection
                .whereEqualTo("isSold", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            if (lastTimestamp != null && lastTimestamp > 0) {
                query = query.startAfter(lastTimestamp)
            }

            val snapshot = query.get().await()
            val items = snapshot.toObjects(ListingDto::class.java)
            Log.d(TAG, "Successfully fetched batch of ${items.size} listings from Firestore")
            items
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch listing batch: ${e.message}")
            emptyList()
        }
    }

    override suspend fun uploadListing(listing: ListingDto): Result<Unit> = try {
        Log.d(TAG, "Attempting to upload listing: ${listing.title} (ID: ${listing.id})")
        val docRef = if (listing.id.isBlank()) listingsCollection.document() else listingsCollection.document(listing.id)
        val finalListing = if (listing.id.isBlank()) listing.copy(id = docRef.id) else listing
        docRef.set(finalListing).await()
        Log.d(TAG, "SUCCESS: Listing uploaded to Firestore with ID: ${docRef.id}")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "FAILURE: Could not upload to Firestore: ${e.message}")
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
