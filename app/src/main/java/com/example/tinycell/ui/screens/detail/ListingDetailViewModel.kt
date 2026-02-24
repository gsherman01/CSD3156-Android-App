package com.example.tinycell.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.FavouriteRepository
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ListingDetailViewModel - Updated with real-time reactive flow.
 */
class ListingDetailViewModel(
    private val repository: ListingRepository,
    private val favouriteRepository: FavouriteRepository,
    private val listingId: String,
    private val currentUserId: String
) : ViewModel() {

    // [FIX]: Observe the listing as a Flow for real-time state updates (Reserved/Sold)
    val listing: StateFlow<Listing?> = repository.getListingFlow(listingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFavourited = MutableStateFlow(false)
    val isFavourited: StateFlow<Boolean> = _isFavourited

    private val _favouriteCount = MutableStateFlow(0)
    val favouriteCount: StateFlow<Int> = _favouriteCount

    init {
        checkFavouriteStatus()
        loadFavouriteCount()
        // Once the flow starts emitting, we set loading to false
        viewModelScope.launch {
            listing.filterNotNull().first()
            _isLoading.value = false
        }
    }

    private fun checkFavouriteStatus() {
        viewModelScope.launch {
            _isFavourited.value = favouriteRepository.isFavourite(currentUserId, listingId)
        }
    }

    private fun loadFavouriteCount() {
        viewModelScope.launch {
            _favouriteCount.value = favouriteRepository.getFavouriteCountForListing(listingId)
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            favouriteRepository.toggleFavourite(currentUserId, listingId)
            checkFavouriteStatus()
            loadFavouriteCount()
        }
    }
}
