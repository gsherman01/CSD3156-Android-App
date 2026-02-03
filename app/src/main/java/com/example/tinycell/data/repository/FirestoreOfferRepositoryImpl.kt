package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.remote.model.OfferDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreOfferRepo"

/**
 * Firestore implementation of [RemoteOfferRepository].
 * Bodies transplanted verbatim from the former offer block in
 * FirestoreListingRepositoryImpl; no behavioural changes.
 */
class FirestoreOfferRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RemoteOfferRepository {

    private val offersCollection = firestore.collection("offers")

    override suspend fun createOffer(offer: OfferDto): Result<Unit> = try {
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
