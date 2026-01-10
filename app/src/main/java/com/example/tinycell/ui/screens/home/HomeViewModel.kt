//class HomeViewModel {
//}
package com.example.tinycell.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.tinycell.data.repository.ListingRepository

class HomeViewModel : ViewModel() {
    private val repository = ListingRepository()
    val listings = repository.getListings()
}


