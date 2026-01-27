package com.example.tinycell.ui.screens.mylistings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.data.repository.ChatRepository
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * UI State for a listing with chat statistics.
 */
data class ListingWithChats(
    val listing: Listing,
    val chatCount: Int,
    val totalUnreadCount: Int
)

/**
 * UI State for My Listings Screen.
 */
data class MyListingsUiState(
    val listings: List<ListingWithChats> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for My Listings Screen.
 * Shows seller's listings with chat room counts and unread message counts.
 */
class MyListingsViewModel(
    private val listingRepository: ListingRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyListingsUiState())
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    init {
        loadMyListings()
    }

    private fun loadMyListings() {
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

                // Get user's listings - collect as Flow
                listingRepository.getListingsByUser(currentUserId).collect { listings ->
                    // Process each listing to get chat stats
                    val listingsWithChats = mutableListOf<ListingWithChats>()

                    for (listing in listings) {
                        // Get chat rooms for this listing (take first emission)
                        val chatRooms = chatRepository.getChatRoomsForListing(listing.id)
                            .first()

                        val chatCount = chatRooms.size

                        // Get unread count for each chat room
                        var totalUnread = 0
                        for (chatRoom in chatRooms) {
                            val unreadCount = chatRepository.getUnreadCountForChatRoom(chatRoom.id, currentUserId)
                                .first()
                            totalUnread += unreadCount
                        }

                        listingsWithChats.add(
                            ListingWithChats(
                                listing = listing,
                                chatCount = chatCount,
                                totalUnreadCount = totalUnread
                            )
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        listings = listingsWithChats,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load listings: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadMyListings()
    }
}
