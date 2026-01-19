package com.example.tinycell.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * [PHASE 5.6]: Updated ProfileViewModel with Admin/Auth Debugging.
 */
class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _userId = MutableStateFlow(authRepository.getCurrentUserId() ?: "Not Logged In")
    val userId: StateFlow<String> = _userId.asStateFlow()

    fun switchUser(newId: String) {
        authRepository.setDebugUserId(newId)
        _userId.value = authRepository.getCurrentUserId() ?: "Not Logged In"
    }

    fun resetToRealAuth() {
        authRepository.setDebugUserId(null)
        _userId.value = authRepository.getCurrentUserId() ?: "Not Logged In"
    }

    fun signOut() {
        authRepository.signOut()
        viewModelScope.launch {
            authRepository.signInAnonymously()
            _userId.value = authRepository.getCurrentUserId() ?: "Not Logged In"
        }
    }
}
