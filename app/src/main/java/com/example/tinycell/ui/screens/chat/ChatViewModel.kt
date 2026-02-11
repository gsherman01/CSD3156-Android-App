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
 * Enhanced to track offer results (ACCEPTED/REJECTED).
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

            // 1. Observe Listing
            val listingFlow = listingRepository.getListingFlow(listingId)
            
            // 2. Observe Messages
            val messagesFlow = chatRepository.getMessagesFlow(chatRoomId)
            
            // 3. Observe Offers for this listing to get real-time results (ACCEPTED/REJECTED)
            val offersFlow = listingRepository.getOffersForListing(listingId)

            combine(listingFlow, messagesFlow, offersFlow) { listing, messages, offers ->
                // Map offer ID to its status
                val statuses = offers.associate { it.id to it.status }
                
                // Find the latest offer from the buyer
                val latestOffer = messages
                    .filter { it.messageType == "OFFER" && it.senderId == otherUserId }
                    .maxByOrNull { it.timestamp }

                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    listing = listing,
                    isSeller = listing?.sellerId == currentUserId,
                    activeOfferId = latestOffer?.offerId,
                    offerStatuses = statuses,
                    isLoading = false
                )
            }.collect()
        }
    }

    fun onMessageTextChanged(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        if (_messageText.value.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(
                chatRoomId = chatRoomId,
                senderId = currentUserId,
                receiverId = otherUserId,
                listingId = listingId,
                message = _messageText.value.trim()
            )
            _messageText.value = ""
        }
    }

    fun sendOffer() {
        val amount = _offerAmount.value.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = _uiState.value.copy(error = "Invalid offer amount")
            return
        }
        
        viewModelScope.launch {
            try {
                val offerId = UUID.randomUUID().toString()
                listingRepository.makeOffer(listingId, amount, offerId)
                chatRepository.sendOfferMessage(
                    chatRoomId = chatRoomId,
                    senderId = currentUserId,
                    receiverId = otherUserId,
                    listingId = listingId,
                    amount = amount,
                    offerId = offerId
                )
                toggleOfferDialog(false)
                _offerAmount.value = ""
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun acceptOffer(offerId: String) {
        viewModelScope.launch {
            try {
                listingRepository.acceptOffer(offerId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to accept offer: ${e.message}")
            }
        }
    }

    fun rejectOffer(offerId: String) {
        viewModelScope.launch {
            try {
                listingRepository.rejectOffer(offerId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to reject offer: ${e.message}")
            }
        }
    }

    fun onOfferAmountChanged(text: String) {
        _offerAmount.value = text
    }

    fun toggleOfferDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showOfferDialog = show)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val listing: Listing? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSeller: Boolean = false,
    val showOfferDialog: Boolean = false,
    val activeOfferId: String? = null,
    val offerStatuses: Map<String, String> = emptyMap() // Track status of each offer
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
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(
                chatRepository,
                listingRepository,
                chatRoomId,
                listingId,
                currentUserId,
                otherUserId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
