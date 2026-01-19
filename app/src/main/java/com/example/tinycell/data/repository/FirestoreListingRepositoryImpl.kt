package com.example.tinycell.data.repository

import com.example.tinycell.data.remote.model.ListingDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * [LEARNING_POINT: REAL-TIME UPDATES]
 * We use 'callbackFlow' instead of 'flow' because Firestore's 'addSnapshotListener'
 * is a multi-shot callback. callbackFlow allows us to convert these continuous
 * updates into a Kotlin Coroutine Flow, keeping the UI reactive.
 */
class FirestoreListingRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RemoteListingRepository {

    private val listingsCollection = firestore.collection("listings")

    override fun getRemoteListings(): Flow<List<ListingDto>> = callbackFlow {
        // [TODO_FIREBASE_INTEGRATION]: Implement error handling logic for
        // snapshot listener (e.g., logging permission issues).
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

        // Ensure listener is removed when the Flow collection stops
        awaitClose { subscription.remove() }
    }

    override suspend fun uploadListing(listing: ListingDto): Result<Unit> = try {
        // [LEARNING_POINT: DOCUMENT ID]
        // We set the document ID to match the DTO ID for consistency between
        // Local Room and Remote Firestore.
        listingsCollection.document(listing.id).set(listing).await()
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