package com.example.tinycell.ui.screens.myfavorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for My Favorites Screen.
 * Manages user's favorited listings.
 */
class MyFavoritesViewModel(
    private val favouriteRepository: FavouriteRepository,
    private val userId: String
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Flow of user's favorite listings from repository.
     * Automatically updates when favorites change.
     */
    val favoriteListings: Flow<List<Listing>> =
        favouriteRepository.getUserFavouriteListings(userId)

    /**
     * Remove a listing from favorites.
     */
    fun removeFavorite(listingId: String) {
        viewModelScope.launch {
            favouriteRepository.removeFavourite(userId, listingId)
        }
    }

    /**
     * Get the count of favorites.
     */
    suspend fun getFavoriteCount(): Int {
        return favouriteRepository.getFavouriteCount(userId)
    }
}
