package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.remote.model.ListingDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRemote"

/**
 * [PHASE 3]: Firestore Implementation of RemoteListingRepository.
 * Handles one-shot fetches for startup and manual refresh.
 */
class FirestoreListingRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RemoteListingRepository {

    private val listingsCollection = firestore.collection("listings")

    override fun getRemoteListings(): Flow<List<ListingDto>> = callbackFlow {
        // [SYNC_BEHAVIOR]: Real-time listener with isSold filter restored.
        val subscription = listingsCollection
            .whereEqualTo("isSold", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Remote listener error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.toObjects(ListingDto::class.java) ?: emptyList()
                Log.d(TAG, "Remote listener: Received ${items.size} active items from Cloud")
                trySend(items)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * [TODO_SQL_CHECK]: Ensure 'isSold' logic matches Room (0/1 vs true/false).
     * This fetch is used for startup and manual pull-to-refresh sync.
     */
    override suspend fun fetchListings(): List<ListingDto> {
        return try {
            Log.d(TAG, "Fetching active listings (isSold == false) from Firestore...")
            
            // Restoring the specific filter for production logic
            val snapshot = listingsCollection
                .whereEqualTo("isSold", false)
                .get()
                .await()
                
            val items = snapshot.toObjects(ListingDto::class.java)
            
            Log.d(TAG, "Successfully fetched ${items.size} active listings from Firestore")
            
            // Detailed log for validation
            items.forEachIndexed { index, item ->
                Log.d(TAG, "Active Item #$index: ID=${item.id}, Title=${item.title}")
            }
            
            items
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch listings: ${e.message}")
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
