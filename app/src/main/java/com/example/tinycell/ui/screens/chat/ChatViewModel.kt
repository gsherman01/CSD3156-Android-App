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

/**
 * ViewModel for the Chat screen.
 * Fetches listing details for the product context header.
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

            // Fetch listing details for the header
            val listing = listingRepository.getListingById(listingId)

            // Listen for incoming messages
            chatRepository.getMessages(chatRoomId).collect { messages ->
                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    listing = listing,
                    isSeller = listing?.sellerId == currentUserId,
                    isLoading = false
                )
            }
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
                listingId = listingId,
                senderId = currentUserId,
                message = _messageText.value.trim(),
                otherUserId = otherUserId
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
                listingRepository.makeOffer(listingId, amount)
                toggleOfferDialog(false)
                _offerAmount.value = ""
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun acceptOffer(offerId: String) {
        viewModelScope.launch {
            listingRepository.acceptOffer(offerId)
        }
    }

    fun rejectOffer(offerId: String) {
        viewModelScope.launch {
            listingRepository.rejectOffer(offerId)
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

/**
 * UI State for Chat Screen
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val listing: Listing? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSeller: Boolean = false,
    val showOfferDialog: Boolean = false
)

/**
 * Factory for ChatViewModel to pass arguments.
 */
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
