package com.example.tinycell.ui.screens.detail

import androidx.lifecycle.ViewModel
import com.example.tinycell.data.repository.ListingRepository

class ListingDetailViewModel(listingId: String) : ViewModel() {
    private val repository = ListingRepository()
    val listing = repository.getListingById(listingId)
}
