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
 * Updated ProfileViewModel with focus on personalization.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val appContainer: AppContainer
) : ViewModel() {

    private val _userId = MutableStateFlow(authRepository.getCurrentUserId() ?: "Not Logged In")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _userName = MutableStateFlow(authRepository.getCurrentUserName() ?: "Anonymous")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    /**
     * Show the profile edit dialog.
     */
    fun showEditDialog() {
        _showEditDialog.value = true
    }

    /**
     * Hide the profile edit dialog.
     */
    fun hideEditDialog() {
        _showEditDialog.value = false
    }

    /**
     * Update the user's display name.
     */
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
}
