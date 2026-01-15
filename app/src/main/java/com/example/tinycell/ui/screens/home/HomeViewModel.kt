package com.example.tinycell.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.Flow

/**
 * HomeViewModel - ViewModel for the home/browse screen.
 *
 * Manages the state of marketplace listings.
 * Uses repository pattern with Room database for data persistence.
 *
 * TODO: Add dependency injection using ViewModelFactory or Hilt
 * For now, repository must be passed via constructor.
 */
class HomeViewModel(private val repository: ListingRepository) : ViewModel() {

    /**
     * All marketplace listings as reactive Flow.
     * UI observes this using collectAsState() in Compose.
     * Automatically updates when database changes.
     */
    val listings: Flow<List<Listing>> = repository.allListings

    /**
     * Search listings by query.
     * TODO: Expose this via UI state when search feature is implemented.
     */
    fun searchListings(query: String): Flow<List<Listing>> {
        return repository.searchListings(query)
    }

    /**
     * Filter listings by category.
     * TODO: Expose this via UI state when filter feature is implemented.
     */
    fun filterByCategory(categoryId: String): Flow<List<Listing>> {
        return repository.getListingsByCategory(categoryId)
    }
}

