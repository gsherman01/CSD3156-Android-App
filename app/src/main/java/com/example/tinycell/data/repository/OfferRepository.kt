package com.example.tinycell.data.repository

import com.example.tinycell.data.local.entity.OfferEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the full Offer lifecycle.
 *
 * Every mutating method follows the same discipline:
 *   1. Snapshot current Room state.
 *   2. Apply Room writes (local-first).
 *   3. Push to Firestore.  On failure → roll back Room to the snapshot.
 *   4. Emit a SYSTEM chat message (fire-and-forget; does NOT trigger rollback).
 *
 * All public methods return [Result<Unit>] so callers can react to failures
 * without catching raw exceptions.
 */
interface OfferRepository {

    /**
     * Creates a new offer.
     * - Persists the offer in Room with status = SENT.
     * - Pushes the offer document to Firestore via set().
     * - Transitions the related listing to PENDING in both Room and Firestore.
     * - Emits SYSTEM message "Offer sent" into the buyer↔seller chat.
     */
    suspend fun createOffer(offer: OfferEntity): Result<Unit>

    /**
     * Accepts an existing offer.
     * - Validates that the offer is currently SENT; returns failure otherwise.
     * - Sets offer status → ACCEPTED and listing status → SOLD / isSold = true.
     * - Persists both changes to Firestore.
     * - Emits SYSTEM message "Offer accepted".
     */
    suspend fun acceptOffer(offerId: String, listingId: String): Result<Unit>

    /**
     * Rejects an existing offer.
     * - Sets offer status → REJECTED in Room and Firestore.
     * - If no other SENT offers remain for the listing, reverts listing to AVAILABLE.
     * - Emits SYSTEM message "Offer rejected".
     */
    suspend fun rejectOffer(offerId: String): Result<Unit>

    /**
     * Observes all offers for a given listing (Room Flow, real-time).
     */
    fun getOffersByListing(listingId: String): Flow<List<OfferEntity>>
}
