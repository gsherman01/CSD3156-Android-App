package com.example.tinycell.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * HomeViewModel - ViewModel for the home/browse screen.
 *
 * Updated to support Remote Sync status.
 */
class HomeViewModel(private val repository: ListingRepository) : ViewModel() {

    // [PHASE 3]: State for Pull-to-Refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * All marketplace listings as reactive Flow.
     * UI observes this using collectAsState() in Compose.
     */
    val listings: Flow<List<Listing>> = repository.allListings

    /**
     * [PHASE 3]: Manual Sync Trigger
     * Used by the UI for Pull-to-Refresh or initial loading.
     */
    fun refreshListings() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.syncFromRemote()
            } catch (e: Exception) {
                // [TODO_ERROR_HANDLING]: Emit a specific error state for the UI
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun searchListings(query: String): Flow<List<Listing>> {
        return repository.searchListings(query)
    }

    fun filterByCategory(categoryId: String): Flow<List<Listing>> {
        return repository.getListingsByCategory(categoryId)
    }
}
