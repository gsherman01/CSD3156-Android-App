/*
package com.example.tinycell.ui.screens.create

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
*/
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
 * Handles form state and submission logic.
 */
class CreateListingViewModel(private val repository: ListingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateListingUiState())
    val uiState: StateFlow<CreateListingUiState> = _uiState.asStateFlow()

    /*
    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }
    */

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(price = value)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onCategoryChange(value: String) {
        _uiState.value = _uiState.value.copy(category = value)
    }


    /* OOLD OLD GRANDFATHER OLD
    /**
     * TODO: Camera Integrator Hook
     * - Call this function when a photo is taken or selected from the gallery.
     */
    fun addImage(path: String) {
        val currentList = _uiState.value.imagePaths.toMutableList()
        currentList.add(path)
        _uiState.value = _uiState.value.copy(imagePaths = currentList)
    }

    fun submitListing() {
        viewModelScope.launch {
            repository.saveListing(
                name = _uiState.value.title,
                imagePaths = _uiState.value.imagePaths
            )
            _uiState.value = _uiState.value.copy(isSubmitted = true)
        }
    }
    */


    /**
     * [TODO_HARDWARE_INTEGRATION]:
     * - ACTION: Camera lead to call this method with the final local file URI.
     * - CONTEXT: Ensure URI is persistent (internal storage) before passing here.
     */
    fun addImage(path: String) {
        val currentImages = _uiState.value.imagePaths.toMutableList()
        currentImages.add(path)
        _uiState.value = _uiState.value.copy(imagePaths = currentImages)
    }

    /**
     * [TODO_DATABASE_INTEGRATION]:
     * - ACTION: DB lead to verify ListingEntity constraints in AppDatabase.
     *
     * [TODO_NETWORKING_INTEGRATION]:
     * - ACTION: Networking lead to implement WorkManager or Retrofit sync
     *   after successful local Room insertion.
     *
     * [TODO_VALIDATION]:
     * - ACTION: Add more complex validation (e.g., minimum character counts,
     *   image count limits) as required by business logic.
     */
    fun submit() {
        val currentState = _uiState.value

        // Robust Parsing & Validation
        val cleanedPrice = currentState.price.trim().replace("$", "")
        val priceDouble = cleanedPrice.toDoubleOrNull()

        // Basic validation check before proceeding
        if (currentState.title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Title cannot be empty")
            return
        }

        if (priceDouble == null || priceDouble < 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid price")
            return
        }

        viewModelScope.launch {
            try {
                repository.createNewListing(
                    title = currentState.title.trim(),
                    price = priceDouble,
                    description = currentState.description.trim(),
                    category = currentState.category,
                    imagePaths = currentState.imagePaths
                )
                _uiState.value = _uiState.value.copy(isSuccess = true, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save: ${e.message}")
            }
        }
    }// end of submit function
}// end of function




/*
the old one
data class CreateListingUiState(
    val title: String = "",
    val imagePaths: List<String> = emptyList(),
    val isSubmitted: Boolean = false
)
*/

/**
 * UI State for the Create Listing screen.
 */
data class CreateListingUiState(
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val category: String = "General",
    val imagePaths: List<String> = emptyList(),
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)


/* VERY SUPER OLD ..
class CreateListingViewModel : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price

    fun onTitleChange(newValue: String) {
        _title.value = newValue
    }

    fun onPriceChange(newValue: String) {
        _price.value = newValue
    }

    fun submitListing() {
        // TODO: Save listing to database
    }
}

*/
