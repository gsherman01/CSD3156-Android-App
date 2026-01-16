package com.example.tinycell.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.screens.create.CreateListingViewModel

/**
 * [TODO_DI_INTEGRATION]:
 * - ACTION: If Hilt or Koin is added later, replace this manual factory.
 * What is Dependency Injection ?
 *
 * Compilation: This file is required because
 * CreateListingViewModel does not have a zero-argument constructor.
 * Without this factory, calling viewModel() in a Composable will cause a runtime crash.
 */

class CreateListingViewModelFactory(
    private val repository: ListingRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateListingViewModel::class.java)) {
            return CreateListingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}