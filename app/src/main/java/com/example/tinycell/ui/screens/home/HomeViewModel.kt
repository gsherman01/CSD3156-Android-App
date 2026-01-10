//class HomeViewModel {
//}
package com.example.tinycell.ui.screens.home
import androidx.lifecycle.ViewModel
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.data.model.Listing

/*
// FIRST EMPTY VERSION
class HomeViewModel : ViewModel() {
    private val repository = ListingRepository()
    val listings = repository.getListings()
}
*/

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
//import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val repository = ListingRepository()

    private val _listings = MutableStateFlow(repository.getListings())
    val listings: StateFlow<List<Listing>> = _listings
}

