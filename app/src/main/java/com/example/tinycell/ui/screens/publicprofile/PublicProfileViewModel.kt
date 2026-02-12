package com.example.tinycell.ui.screens.publicprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.local.entity.ReviewEntity
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PublicProfileUiState(
    val listings: List<Listing> = emptyList(),
    val reviews: List<ReviewEntity> = emptyList(),
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class PublicProfileViewModel(
    private val listingRepository: ListingRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val listingsFlow = listingRepository.getListingsByUser(userId)
                val reviewsFlow = listingRepository.getReviewsForUser(userId)
                val ratingFlow = listingRepository.getAverageRating(userId)

                combine(listingsFlow, reviewsFlow, ratingFlow) { userListings, userReviews, avgRating ->
                    _uiState.value = _uiState.value.copy(
                        listings = userListings.filter { !it.isSold },
                        reviews = userReviews,
                        averageRating = avgRating ?: 0.0,
                        reviewCount = userReviews.size,
                        isLoading = false
                    )
                }.collect()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }
}

class PublicProfileViewModelFactory(
    private val listingRepository: ListingRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PublicProfileViewModel(listingRepository, userId) as T
    }
}
