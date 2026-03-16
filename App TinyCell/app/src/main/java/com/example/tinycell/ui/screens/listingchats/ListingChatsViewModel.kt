package com.example.tinycell.ui.screens.listingchats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.ChatRoom
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Chat room with unread count for UI display.
 */
data class ChatRoomWithUnread(
    val chatRoom: ChatRoom,
    val unreadCount: Int
)

/**
 * UI State for Listing Chats Screen.
 */
data class ListingChatsUiState(
    val chatRooms: List<ChatRoomWithUnread> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for Listing Chats Screen.
 * Shows all chat rooms for a specific listing with unread counts.
 */
class ListingChatsViewModel(
    private val listingId: String,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListingChatsUiState())
    val uiState: StateFlow<ListingChatsUiState> = _uiState.asStateFlow()

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val currentUserId = authRepository.getCurrentUserId()

                if (currentUserId.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Not logged in"
                    )
                    return@launch
                }

                // Get chat rooms for this listing
                chatRepository.getChatRoomsForListing(listingId).collect { chatRooms ->
                    // For each chat room, get unread count
                    val chatRoomsWithUnread = mutableListOf<ChatRoomWithUnread>()

                    for (chatRoom in chatRooms) {
                        // Get unread count for this chat room (take first emission)
                        val unreadCount = chatRepository.getUnreadCountForChatRoom(chatRoom.id, currentUserId)
                            .first()

                        chatRoomsWithUnread.add(
                            ChatRoomWithUnread(
                                chatRoom = chatRoom,
                                unreadCount = unreadCount
                            )
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        chatRooms = chatRoomsWithUnread,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load chats: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadChatRooms()
    }
}
