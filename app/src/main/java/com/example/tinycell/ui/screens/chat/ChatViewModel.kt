package com.example.tinycell.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.ChatMessage
import com.example.tinycell.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private const val TAG = "ChatViewModel"

/**
 * UI State for Chat Screen
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for Chat Screen
 */
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatRoomId: String,
    private val listingId: String,
    private val currentUserId: String,
    private val otherUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    init {
        Log.d(TAG, "Initializing ChatViewModel for room: $chatRoomId")
        loadMessages()
        markMessagesAsRead()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessagesFlow(chatRoomId)
                .catch { e ->
                    Log.e(TAG, "Error loading messages: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load messages: ${e.message}"
                    )
                }
                .collect { messages ->
                    Log.d(TAG, "Received ${messages.size} messages")
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false,
                        error = null
                    )
                    // Mark new messages as read
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

    fun onMessageTextChanged(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isEmpty()) {
            Log.d(TAG, "Cannot send empty message")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Sending message: $text")
            _messageText.value = "" // Clear input immediately for better UX

            val result = chatRepository.sendMessage(
                chatRoomId = chatRoomId,
                senderId = currentUserId,
                receiverId = otherUserId,
                listingId = listingId,
                message = text
            )

            if (result.isFailure) {
                Log.e(TAG, "Failed to send message: ${result.exceptionOrNull()?.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to send message"
                )
                // Restore message text on failure
                _messageText.value = text
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * Factory for creating ChatViewModel with dependencies
 */
class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val chatRoomId: String,
    private val listingId: String,
    private val currentUserId: String,
    private val otherUserId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                chatRepository = chatRepository,
                chatRoomId = chatRoomId,
                listingId = listingId,
                currentUserId = currentUserId,
                otherUserId = otherUserId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
