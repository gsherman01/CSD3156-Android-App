package com.example.tinycell.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Creating a New Listing.
 * Polished with strict validation and location support per UX Audit.
 * [UPDATE]: Images are now optional.
 */
class CreateListingViewModel(private val repository: ListingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateListingUiState())
    val uiState: StateFlow<CreateListingUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value, errorMessage = null)
    }

    fun onPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(price = value, errorMessage = null)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value, errorMessage = null)
    }

    fun onLocationChange(value: String) {
        _uiState.value = _uiState.value.copy(location = value, errorMessage = null)
    }

    fun onCategoryChange(value: String) {
        if (_uiState.value.availableCategories.contains(value)) {
            _uiState.value = _uiState.value.copy(category = value)
        }
    }

    fun addImage(path: String) {
        val currentImages = _uiState.value.imagePaths.toMutableList()
        currentImages.add(path)
        _uiState.value = _uiState.value.copy(imagePaths = currentImages, errorMessage = null)
    }

    fun removeImage(path: String) {
        val currentImages = _uiState.value.imagePaths.toMutableList()
        currentImages.remove(path)
        _uiState.value = _uiState.value.copy(imagePaths = currentImages)
    }

    /**
     * Enhanced submission logic with strict validation.
     * Images are optional to lower friction for new listings.
     */
    fun submit() {
        val currentState = _uiState.value
        
        // 1. Title Validation (Min 3 chars)
        if (currentState.title.trim().length < 3) {
            _uiState.value = _uiState.value.copy(errorMessage = "Title must be at least 3 characters")
            return
        }

        // 2. Price Validation (> 0)
        val cleanedPrice = currentState.price.trim().replace("$", "")
        val priceDouble = cleanedPrice.toDoubleOrNull()
        if (priceDouble == null || priceDouble <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid price greater than 0")
            return
        }

        // 3. Description Validation (Min 10 chars)
        if (currentState.description.trim().length < 10) {
            _uiState.value = _uiState.value.copy(errorMessage = "Description must be at least 10 characters")
            return
        }

        // [OPTIONAL]: Images are no longer mandatory.

        // If all valid, proceed to save
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                repository.createNewListing(
                    title = currentState.title.trim(),
                    price = priceDouble,
                    description = currentState.description.trim(),
                    category = currentState.category,
                    imagePaths = currentState.imagePaths,
                    location = currentState.location.trim().ifBlank { null }
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }
}

/**
 * UI State for the Create Listing screen.
 */
data class CreateListingUiState(
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val location: String = "",
    val category: String = "General",
    val availableCategories: List<String> = listOf(
        "General", "Electronics", "Fashion", "Home", "Toys", "Books"
    ),
    val imagePaths: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
