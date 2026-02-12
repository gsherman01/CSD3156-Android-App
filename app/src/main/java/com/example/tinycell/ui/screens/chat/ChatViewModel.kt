package com.example.tinycell.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.ChatMessage
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.ChatRepository
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the Chat screen.
 * [FIXED]: Correct buyer identification, guidance text logic, and one-time review check.
 */
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val listingRepository: ListingRepository,
    private val chatRoomId: String,
    private val listingId: String,
    private val currentUserId: String,
    private val otherUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _offerAmount = MutableStateFlow("")
    val offerAmount: StateFlow<String> = _offerAmount.asStateFlow()

    init {
        loadChat()
    }

    private fun loadChat() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val listingFlow = listingRepository.getListingFlow(listingId)
            val messagesFlow = chatRepository.getMessagesFlow(chatRoomId)
            val offersFlow = listingRepository.getOffersForListing(listingId)
            
            // Observe if the current user has already reviewed this transaction
            val reviewFlow = listingRepository.getReviewForTransaction(currentUserId, listingId)

            combine(listingFlow, messagesFlow, offersFlow, reviewFlow) { listing, messages, offers, existingReview ->
                val statuses = offers.associate { it.id to it.status }
                val isSeller = listing?.sellerId == currentUserId
                
                // [FIX]: The buyer is the person who is NOT the listing creator (seller)
                // In our model, listing.sellerId is the creator.
                val sellerId = listing?.sellerId ?: ""
                val buyerId = if (isSeller) otherUserId else currentUserId
                
                // An offer is ACTIVE if it is the latest one sent by the buyer
                val latestOffer = messages
                    .filter { it.messageType == "OFFER" && it.senderId == buyerId }
                    .maxByOrNull { it.timestamp }

                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    listing = listing,
                    isSeller = isSeller,
                    activeOfferId = latestOffer?.offerId,
                    offerStatuses = statuses,
                    hasReviewed = existingReview != null, // Reactive check for review
                    isLoading = false
                )
            }.collect()
        }
    }

    fun onMessageTextChanged(text: String) { _messageText.value = text }

    fun sendMessage() {
        if (_messageText.value.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(chatRoomId, currentUserId, otherUserId, listingId, _messageText.value.trim())
            _messageText.value = ""
        }
    }

    fun sendImage(imagePath: String) {
        viewModelScope.launch {
            try {
                chatRepository.sendImageMessage(chatRoomId, currentUserId, otherUserId, listingId, imagePath)
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = "Failed to send image") }
        }
    }

    fun sendOffer() {
        val amount = _offerAmount.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            try {
                val offerId = UUID.randomUUID().toString()
                listingRepository.makeOffer(listingId, amount, offerId)
                chatRepository.sendOfferMessage(chatRoomId, currentUserId, otherUserId, listingId, amount, offerId)
                toggleOfferDialog(false)
                _offerAmount.value = ""
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun acceptOffer(offerId: String) {
        viewModelScope.launch {
            try { listingRepository.acceptOffer(offerId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = "Failed to accept") }
        }
    }

    fun markAsSold() {
        viewModelScope.launch {
            try { listingRepository.completeTransaction(listingId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = "Failed to mark sold") }
        }
    }

    fun submitReview(rating: Int, comment: String) {
        val role = if (_uiState.value.isSeller) "BUYER" else "SELLER"
        viewModelScope.launch {
            try {
                listingRepository.submitReview(listingId, currentUserId, otherUserId, rating, comment, role)
                _uiState.value = _uiState.value.copy(showReviewDialog = false)
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(error = "Failed to submit review") }
        }
    }

    fun rejectOffer(offerId: String) {
        viewModelScope.launch {
            try { listingRepository.rejectOffer(offerId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(error = "Failed to reject") }
        }
    }

    fun toggleOfferDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showOfferDialog = show) }
    fun toggleReviewDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showReviewDialog = show) }
    fun onOfferAmountChanged(text: String) { _offerAmount.value = text }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val listing: Listing? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSeller: Boolean = false,
    val hasReviewed: Boolean = false,
    val showOfferDialog: Boolean = false,
    val showReviewDialog: Boolean = false,
    val activeOfferId: String? = null,
    val offerStatuses: Map<String, String> = emptyMap()
)

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val listingRepository: ListingRepository,
    private val chatRoomId: String,
    private val listingId: String,
    private val currentUserId: String,
    private val otherUserId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(chatRepository, listingRepository, chatRoomId, listingId, currentUserId, otherUserId) as T
    }
}
