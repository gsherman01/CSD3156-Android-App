package com.example.tinycell.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.FavouriteRepository
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ListingDetailViewModel - ViewModel for listing detail screen.
 *
 * Manages the state of a single listing detail view.
 * Uses repository pattern with Room database for data persistence.
 */
class ListingDetailViewModel(
    private val repository: ListingRepository,
    private val favouriteRepository: FavouriteRepository,
    private val listingId: String,
    private val currentUserId: String
) : ViewModel() {

    private val _listing = MutableStateFlow<Listing?>(null)
    val listing: StateFlow<Listing?> = _listing

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFavourited = MutableStateFlow(false)
    val isFavourited: StateFlow<Boolean> = _isFavourited

    private val _favouriteCount = MutableStateFlow(0)
    val favouriteCount: StateFlow<Int> = _favouriteCount

    init {
        loadListing()
        checkFavouriteStatus()
        loadFavouriteCount()
    }

    /**
     * Load listing details from database.
     */
    private fun loadListing() {
        viewModelScope.launch {
            _isLoading.value = true
            _listing.value = repository.getListingById(listingId)
            _isLoading.value = false
        }
    }

    /**
     * Check if the current user has favourited this listing.
     */
    private fun checkFavouriteStatus() {
        viewModelScope.launch {
            _isFavourited.value = favouriteRepository.isFavourite(currentUserId, listingId)
        }
    }

    /**
     * Load the total favourite count for this listing.
     */
    private fun loadFavouriteCount() {
        viewModelScope.launch {
            _favouriteCount.value = favouriteRepository.getFavouriteCountForListing(listingId)
        }
    }

    /**
     * Toggle favourite status for this listing.
     */
    fun toggleFavourite() {
        viewModelScope.launch {
            favouriteRepository.toggleFavourite(currentUserId, listingId)
            // Update local state
            checkFavouriteStatus()
            loadFavouriteCount()
        }
    }

    /**
     * Refresh listing data.
     */
    fun refresh() {
        loadListing()
        checkFavouriteStatus()
        loadFavouriteCount()
    }
}
