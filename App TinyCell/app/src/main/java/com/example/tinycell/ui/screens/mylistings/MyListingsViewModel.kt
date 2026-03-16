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
 * Filter types for My Listings
 */
enum class ListingFilter {
    ALL, AVAILABLE, SOLD
}

/**
 * UI State for My Listings Screen.
 */
data class MyListingsUiState(
    val listings: List<ListingWithChats> = emptyList(),
    val filteredListings: List<ListingWithChats> = emptyList(),
    val currentFilter: ListingFilter = ListingFilter.ALL,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for My Listings Screen.
 * Enhanced with management actions (Mark as Sold, Delete) and filtering.
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

                // Get user's listings
                listingRepository.getListingsByUser(currentUserId).collect { listings ->
                    val listingsWithChats = mutableListOf<ListingWithChats>()

                    for (listing in listings) {
                        val chatRooms = chatRepository.getChatRoomsForListing(listing.id).first()
                        val chatCount = chatRooms.size

                        var totalUnread = 0
                        for (chatRoom in chatRooms) {
                            val unreadCount = chatRepository.getUnreadCountForChatRoom(chatRoom.id, currentUserId).first()
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
                    applyFilter(_uiState.value.currentFilter)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load: ${e.message}"
                )
            }
        }
    }

    fun setFilter(filter: ListingFilter) {
        _uiState.value = _uiState.value.copy(currentFilter = filter)
        applyFilter(filter)
    }

    private fun applyFilter(filter: ListingFilter) {
        val all = _uiState.value.listings
        val filtered = when (filter) {
            ListingFilter.ALL -> all
            ListingFilter.AVAILABLE -> all.filter { !it.listing.isSold }
            ListingFilter.SOLD -> all.filter { it.listing.isSold }
        }
        _uiState.value = _uiState.value.copy(filteredListings = filtered)
    }

    fun markAsSold(listingId: String) {
        viewModelScope.launch {
            try {
                listingRepository.markListingAsSold(listingId)
                // The flow collector will automatically refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to mark as sold: ${e.message}")
            }
        }
    }

    fun deleteListing(listing: Listing) {
        viewModelScope.launch {
            try {
                listingRepository.deleteListing(listing)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to delete: ${e.message}")
            }
        }
    }

    fun refresh() {
        loadMyListings()
    }
}
