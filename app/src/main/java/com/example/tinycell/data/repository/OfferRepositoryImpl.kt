package com.example.tinycell.data.repository

import android.util.Log
import com.example.tinycell.data.local.dao.ListingDao
import com.example.tinycell.data.local.dao.OfferDao
import com.example.tinycell.data.local.entity.OfferEntity
import com.example.tinycell.data.remote.model.toDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val TAG = "OfferRepositoryImpl"

/**
 * Concrete implementation of [OfferRepository].
 *
 * Rollback discipline (Room only; Firestore writes that already succeeded
 * are left for eventual consistency):
 *   - Before any Room write, snapshot the current entity state.
 *   - If a subsequent Firestore call fails, replay the snapshot back into Room.
 *
 * SYSTEM chat messages are emitted at the end of each flow.  A failure there
 * is logged but does NOT trigger a rollback of the offer/listing state — the
 * offer is already persisted and the chat event is informational.
 */
class OfferRepositoryImpl(
    private val offerDao: OfferDao,
    private val listingDao: ListingDao,
    private val remoteOfferRepo: RemoteOfferRepository,
    private val remoteListingRepo: RemoteListingRepository,
    private val chatRepo: ChatRepository
) : OfferRepository {

    // ------------------------------------------------------------------
    // createOffer
    // ------------------------------------------------------------------

    override suspend fun createOffer(offer: OfferEntity): Result<Unit> = withContext(Dispatchers.IO) {
        // Enforce status contract; caller may omit it.
        val offerToInsert = offer.copy(status = "SENT")

        // 1. Snapshot current listing — needed for rollback and for the chat room title.
        val previousListing = listingDao.getListingById(offerToInsert.listingId)
            ?: return@withContext Result.failure(
                IllegalStateException("Listing ${offerToInsert.listingId} not found in local DB")
            )

        // 2. Room write: insert offer.
        offerDao.insert(offerToInsert)
        Log.d(TAG, "Room: Offer ${offerToInsert.id} inserted with status SENT")

        // 3. Firestore write: create offer document via set().
        val remoteOfferResult = remoteOfferRepo.createOffer(offerToInsert.toDto())
        if (remoteOfferResult.isFailure) {
            // Rollback: remove the offer we just inserted.
            offerDao.delete(offerToInsert)
            Log.e(TAG, "Rollback: deleted offer ${offerToInsert.id} from Room after Firestore failure")
            return@withContext Result.failure(
                remoteOfferResult.exceptionOrNull() ?: Exception("Failed to create offer in Firestore")
            )
        }

        // 4. Room write: transition listing → PENDING.
        listingDao.updateStatus(offerToInsert.listingId, "PENDING", false)
        Log.d(TAG, "Room: Listing ${offerToInsert.listingId} status set to PENDING")

        // 5. Firestore write: transition listing → PENDING.
        val remoteListingResult = remoteListingRepo.updateListingStatus(offerToInsert.listingId, "PENDING", false)
        if (remoteListingResult.isFailure) {
            // Rollback: revert listing status and delete offer from Room.
            listingDao.updateStatus(offerToInsert.listingId, previousListing.status, previousListing.isSold)
            offerDao.delete(offerToInsert)
            Log.e(TAG, "Rollback: reverted listing and deleted offer after Firestore listing update failure")
            return@withContext Result.failure(
                remoteListingResult.exceptionOrNull() ?: Exception("Failed to update listing status in Firestore")
            )
        }

        // 6. SYSTEM message — fire-and-forget.
        emitSystemMessage(
            listingId = offerToInsert.listingId,
            listingTitle = previousListing.title,
            buyerId = offerToInsert.buyerId,
            sellerId = offerToInsert.sellerId,
            senderId = offerToInsert.buyerId,
            receiverId = offerToInsert.sellerId,
            message = "Offer sent",
            offerId = offerToInsert.id
        )

        Result.success(Unit)
    }

    // ------------------------------------------------------------------
    // acceptOffer
    // ------------------------------------------------------------------

    override suspend fun acceptOffer(offerId: String, listingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Fetch and validate.
        val offer = offerDao.getOfferById(offerId)
            ?: return@withContext Result.failure(IllegalStateException("Offer $offerId not found"))
        if (offer.status != "SENT") {
            return@withContext Result.failure(
                IllegalStateException("Offer $offerId is in state '${offer.status}'; only SENT offers can be accepted")
            )
        }

        // 2. Snapshot listing.
        val previousListing = listingDao.getListingById(listingId)
            ?: return@withContext Result.failure(
                IllegalStateException("Listing $listingId not found in local DB")
            )

        // 3. Room writes.
        offerDao.updateStatus(offerId, "ACCEPTED")
        listingDao.updateStatus(listingId, "SOLD", true)
        Log.d(TAG, "Room: Offer $offerId → ACCEPTED, Listing $listingId → SOLD")

        // 4. Firestore: update offer status.
        val offerResult = remoteOfferRepo.updateOfferStatus(offerId, "ACCEPTED")
        if (offerResult.isFailure) {
            // Rollback both Room writes.
            offerDao.updateStatus(offerId, "SENT")
            listingDao.updateStatus(listingId, previousListing.status, previousListing.isSold)
            Log.e(TAG, "Rollback: reverted offer and listing after Firestore offer update failure")
            return@withContext Result.failure(
                offerResult.exceptionOrNull() ?: Exception("Failed to update offer status in Firestore")
            )
        }

        // 5. Firestore: update listing status.
        val listingResult = remoteListingRepo.updateListingStatus(listingId, "SOLD", true)
        if (listingResult.isFailure) {
            // Rollback both Room writes.  The offer is already ACCEPTED in Firestore;
            // that will be reconciled on next sync (eventual consistency).
            offerDao.updateStatus(offerId, "SENT")
            listingDao.updateStatus(listingId, previousListing.status, previousListing.isSold)
            Log.e(TAG, "Rollback: reverted offer and listing after Firestore listing update failure")
            return@withContext Result.failure(
                listingResult.exceptionOrNull() ?: Exception("Failed to update listing status in Firestore")
            )
        }

        // 6. SYSTEM message.
        emitSystemMessage(
            listingId = listingId,
            listingTitle = previousListing.title,
            buyerId = offer.buyerId,
            sellerId = offer.sellerId,
            senderId = offer.sellerId,      // seller is the actor
            receiverId = offer.buyerId,
            message = "Offer accepted",
            offerId = offerId
        )

        Result.success(Unit)
    }

    // ------------------------------------------------------------------
    // rejectOffer
    // ------------------------------------------------------------------

    override suspend fun rejectOffer(offerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Fetch offer.
        val offer = offerDao.getOfferById(offerId)
            ?: return@withContext Result.failure(IllegalStateException("Offer $offerId not found"))

        // 2. Snapshot listing.
        val previousListing = listingDao.getListingById(offer.listingId)
            ?: return@withContext Result.failure(
                IllegalStateException("Listing ${offer.listingId} not found in local DB")
            )

        // 3. Room write: reject offer.
        offerDao.updateStatus(offerId, "REJECTED")
        Log.d(TAG, "Room: Offer $offerId → REJECTED")

        // 4. Firestore write: reject offer.
        val offerResult = remoteOfferRepo.updateOfferStatus(offerId, "REJECTED")
        if (offerResult.isFailure) {
            // Rollback: restore offer status.
            offerDao.updateStatus(offerId, offer.status)
            Log.e(TAG, "Rollback: reverted offer status after Firestore failure")
            return@withContext Result.failure(
                offerResult.exceptionOrNull() ?: Exception("Failed to update offer status in Firestore")
            )
        }

        // 5. Check if any other SENT offers remain for this listing.
        val remainingSent = offerDao.getSentOffersByListing(offer.listingId)
        if (remainingSent.isEmpty()) {
            // No active offers left → revert listing to AVAILABLE.
            listingDao.updateStatus(offer.listingId, "AVAILABLE", false)
            Log.d(TAG, "Room: Listing ${offer.listingId} reverted to AVAILABLE (no remaining SENT offers)")

            val listingResult = remoteListingRepo.updateListingStatus(offer.listingId, "AVAILABLE", false)
            if (listingResult.isFailure) {
                // Rollback listing in Room to its previous state.
                // The offer rejection in Firestore already succeeded and stays.
                listingDao.updateStatus(offer.listingId, previousListing.status, previousListing.isSold)
                Log.e(TAG, "Rollback: reverted listing status after Firestore listing update failure")
                // We still return success for the offer itself; the listing revert is best-effort.
                // Log the issue so operators can reconcile.
                Log.w(TAG, "Listing ${offer.listingId} may be out of sync with Firestore until next sync cycle")
            }
        }

        // 6. SYSTEM message.
        emitSystemMessage(
            listingId = offer.listingId,
            listingTitle = previousListing.title,
            buyerId = offer.buyerId,
            sellerId = offer.sellerId,
            senderId = offer.sellerId,      // seller is the actor
            receiverId = offer.buyerId,
            message = "Offer rejected",
            offerId = offerId
        )

        Result.success(Unit)
    }

    // ------------------------------------------------------------------
    // getOffersByListing
    // ------------------------------------------------------------------

    override fun getOffersByListing(listingId: String): Flow<List<OfferEntity>> =
        offerDao.getOffersByListing(listingId)

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Ensures the chat room exists (idempotent), then posts a SYSTEM message.
     * Failures are logged only — they must not unwind the offer/listing state.
     */
    private suspend fun emitSystemMessage(
        listingId: String,
        listingTitle: String,
        buyerId: String,
        sellerId: String,
        senderId: String,
        receiverId: String,
        message: String,
        offerId: String
    ) {
        try {
            val chatRoom = chatRepo.getOrCreateChatRoom(listingId, listingTitle, buyerId, sellerId)
            chatRepo.sendSystemMessage(
                chatRoomId = chatRoom.id,
                senderId = senderId,
                receiverId = receiverId,
                listingId = listingId,
                message = message,
                offerId = offerId
            )
            Log.d(TAG, "SYSTEM message '$message' emitted to chat room ${chatRoom.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to emit SYSTEM message '$message': ${e.message}")
        }
    }
}
