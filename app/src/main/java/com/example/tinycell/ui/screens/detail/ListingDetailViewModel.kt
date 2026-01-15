package com.example.tinycell.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.Listing
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
    private val listingId: String
) : ViewModel() {

    private val _listing = MutableStateFlow<Listing?>(null)
    val listing: StateFlow<Listing?> = _listing

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadListing()
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
     * Refresh listing data.
     */
    fun refresh() {
        loadListing()
    }
}
