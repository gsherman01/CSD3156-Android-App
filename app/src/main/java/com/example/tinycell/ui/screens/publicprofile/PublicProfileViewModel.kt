package com.example.tinycell.ui.screens.publicprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PublicProfileUiState(
    val listings: List<Listing> = emptyList(),
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
        loadUserListings()
    }

    private fun loadUserListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                listingRepository.getListingsByUser(userId).collect { userListings ->
                    _uiState.value = _uiState.value.copy(
                        listings = userListings.filter { !it.isSold },
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
}

class PublicProfileViewModelFactory(
    private val listingRepository: ListingRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PublicProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PublicProfileViewModel(listingRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
