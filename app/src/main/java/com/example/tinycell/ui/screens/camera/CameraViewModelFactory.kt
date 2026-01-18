package com.example.tinycell.ui.screens.camera

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tinycell.MarketplaceApp
import com.example.tinycell.data.repository.CameraRepository

/**
 * TODO: Dependency Injection Documentation
 * 1. [DI_FRAMEWORK]: If the project grows, migrate this to Hilt or Koin to avoid manual factory boilerplate.
 * 2. [REPOSITORY_SHARING]: Ensure the same instance of CameraRepository is used if multiple screens require camera access.
 */
class CameraViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val cameraRepository = (context.applicationContext as MarketplaceApp).cameraRepository
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(cameraRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}