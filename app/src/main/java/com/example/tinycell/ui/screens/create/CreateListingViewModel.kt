package com.example.tinycell.ui.screens.create

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
