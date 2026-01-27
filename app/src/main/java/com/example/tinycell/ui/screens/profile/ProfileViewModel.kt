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
 * [PHASE 5.6]: Updated ProfileViewModel with Admin/Auth Debugging.
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

    fun generateSampleListings(count: Int = 5) {
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
