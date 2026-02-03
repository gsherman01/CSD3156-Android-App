package com.example.tinycell.data.repository

import com.example.tinycell.data.remote.model.OfferDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote data-source contract for the Offer system.
 * Extracted from RemoteListingRepository so that listing and offer
 * cloud operations are owned by independent interfaces.
 */
interface RemoteOfferRepository {

    /**
     * Persists a new offer document in Firestore using set().
     * The caller is responsible for populating offer.id before invocation.
     */
    suspend fun createOffer(offer: OfferDto): Result<Unit>

    /**
     * Updates only the status field of an existing offer document.
     * Returns failure if the document does not exist.
     */
    suspend fun updateOfferStatus(offerId: String, status: String): Result<Unit>

    /**
     * Streams real-time offer updates for a given listing.
     */
    fun getOffersForListing(listingId: String): Flow<List<OfferDto>>
}
