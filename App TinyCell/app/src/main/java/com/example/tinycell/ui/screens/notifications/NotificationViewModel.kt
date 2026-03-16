package com.example.tinycell.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.local.entity.NotificationEntity
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val isLoading: Boolean = true
)

class NotificationViewModel(
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            listingRepository.getNotifications().collect { list ->
                _uiState.value = _uiState.value.copy(
                    notifications = list,
                    isLoading = false
                )
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            listingRepository.markNotificationsRead()
        }
    }
}

class NotificationViewModelFactory(
    private val listingRepository: ListingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationViewModel(listingRepository) as T
    }
}
