package com.example.tinycell.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Updated ProfileViewModel with dynamic sample data generation.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val appContainer: AppContainer
) : ViewModel() {

    private val _userId = MutableStateFlow(authRepository.getCurrentUserId() ?: "Not Logged In")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _userName = MutableStateFlow(authRepository.getCurrentUserName() ?: "Anonymous")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isGeneratingListings = MutableStateFlow(false)
    val isGeneratingListings: StateFlow<Boolean> = _isGeneratingListings.asStateFlow()

    // [NEW]: State for the input field in Admin Panel
    private val _sampleCountInput = MutableStateFlow("5")
    val sampleCountInput: StateFlow<String> = _sampleCountInput.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    fun onSampleCountChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _sampleCountInput.value = value
        }
    }

    fun showEditDialog() { _showEditDialog.value = true }
    fun hideEditDialog() { _showEditDialog.value = false }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            authRepository.updateUserName(newName)
            _userName.value = authRepository.getCurrentUserName() ?: "Anonymous"
            hideEditDialog()
        }
    }

    fun switchUser(newId: String) {
        authRepository.setDebugUserId(newId)
        _userId.value = authRepository.getCurrentUserId() ?: "Not Logged In"
        _userName.value = authRepository.getCurrentUserName() ?: "Anonymous"
    }

    fun resetToRealAuth() {
        authRepository.setDebugUserId(null)
        _userId.value = authRepository.getCurrentUserId() ?: "Not Logged In"
        _userName.value = authRepository.getCurrentUserName() ?: "Anonymous"
    }

    fun signOut() {
        authRepository.signOut()
        viewModelScope.launch {
            authRepository.signInAnonymously()
            _userId.value = authRepository.getCurrentUserId() ?: "Not Logged In"
            _userName.value = authRepository.getCurrentUserName() ?: "Anonymous"
        }
    }

    /**
     * Triggers sample data generation.
     */
    fun generateSampleListings() {
        val count = _sampleCountInput.value.toIntOrNull() ?: 5
        viewModelScope.launch {
            _isGeneratingListings.value = true
            try {
                appContainer.generateSampleListings(count)
            } finally {
                _isGeneratingListings.value = false
            }
        }
    }
}
