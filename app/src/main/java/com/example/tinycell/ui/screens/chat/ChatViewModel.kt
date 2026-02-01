package com.example.tinycell.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.ChatMessage
import com.example.tinycell.data.repository.ChatRepository
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "ChatViewModel"

/**
 * UI State for Chat Screen
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showOfferDialog: Boolean = false,
    val isSeller: Boolean = false
)

/**
 * ViewModel for Chat Screen
 * Updated to support the Formal Offer System.
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
        Log.d(TAG, "Initializing ChatViewModel for room: $chatRoomId")
        checkIfSeller()
        loadMessages()
        markMessagesAsRead()
    }

    private fun checkIfSeller() {
        viewModelScope.launch {
            val listing = listingRepository.getListingById(listingId)
            _uiState.update { it.copy(isSeller = listing?.sellerId == currentUserId) }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessagesFlow(chatRoomId)
                .catch { e ->
                    Log.e(TAG, "Error loading messages: ${e.message}")
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load messages") }
                }
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages, isLoading = false, error = null) }
                    markMessagesAsRead()
                }
        }
    }

    private fun markMessagesAsRead() {
        viewModelScope.launch {
            try {
                chatRepository.markMessagesAsRead(chatRoomId, currentUserId)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking messages as read: ${e.message}")
            }
        }
    }

    fun onMessageTextChanged(text: String) { _messageText.value = text }
    fun onOfferAmountChanged(amount: String) { _offerAmount.value = amount }

    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _messageText.value = ""
            val result = chatRepository.sendMessage(chatRoomId, currentUserId, otherUserId, listingId, text)
            if (result.isFailure) _uiState.update { it.copy(error = "Failed to send message") }
        }
    }

    /**
     * [PHASE 6]: Sends a formal offer and a corresponding chat message card.
     */
    fun sendOffer() {
        val amount = _offerAmount.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            try {
                // 1. Create the offer record
                val offerId = java.util.UUID.randomUUID().toString()
                listingRepository.makeOffer(listingId, amount)
                
                // 2. Send the interactive chat card
                chatRepository.sendOfferMessage(
                    chatRoomId = chatRoomId,
                    senderId = currentUserId,
                    receiverId = otherUserId,
                    listingId = listingId,
                    amount = amount,
                    offerId = offerId
                )
                
                _offerAmount.value = ""
                toggleOfferDialog(false)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to send offer") }
            }
        }
    }

    fun acceptOffer(offerId: String) {
        viewModelScope.launch {
            try {
                listingRepository.acceptOffer(offerId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to accept offer") }
            }
        }
    }

    fun rejectOffer(offerId: String) {
        viewModelScope.launch {
            try {
                listingRepository.rejectOffer(offerId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to reject offer") }
            }
        }
    }

    fun toggleOfferDialog(show: Boolean) {
        _uiState.update { it.copy(showOfferDialog = show) }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}

/**
 * Factory for creating ChatViewModel with dependencies
 */
class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val listingRepository: ListingRepository,
    private val chatRoomId: String,
    private val listingId: String,
    private val currentUserId: String,
    private val otherUserId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(chatRepository, listingRepository, chatRoomId, listingId, currentUserId, otherUserId) as T
    }
}
