package com.example.tinycell.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.FavouriteRepository
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Filter state data class to hold all filter parameters
 */
data class SearchFilters(
    val query: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val minPrice: Double = 0.0,
    val maxPrice: Double = Double.MAX_VALUE,
    val minDate: Long? = null,
    val maxDate: Long? = null
)

/**
 * HomeViewModel - Updated with Notification tracking.
 */
class HomeViewModel(
    private val repository: ListingRepository,
    private val favouriteRepository: FavouriteRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    private val _searchFilters = MutableStateFlow(SearchFilters())
    val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    private val _favouriteStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favouriteStates: StateFlow<Map<String, Boolean>> = _favouriteStates.asStateFlow()

    /**
     * [NEW]: Real-time unread notification count.
     */
    val unreadNotificationCount: StateFlow<Int> = repository.getUnreadNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val listings: Flow<List<Listing>> = _searchFilters.flatMapLatest { filters ->
        repository.searchWithFilters(
            query = if (filters.query.isBlank()) "%" else filters.query,
            categoryIds = filters.selectedCategories.toList(),
            minPrice = filters.minPrice,
            maxPrice = filters.maxPrice,
            minDate = filters.minDate ?: 0L,
            maxDate = filters.maxDate ?: System.currentTimeMillis()
        )
    }

    init {
        loadCategories()
        loadFavouriteStates()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = repository.getAllCategories()
                _categories.value = cats
            } catch (e: Exception) { }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchFilters.update { it.copy(query = query) }
    }

    fun toggleCategoryFilter(categoryId: String) {
        _searchFilters.update { currentFilters ->
            val currentCategories = currentFilters.selectedCategories.toMutableSet()
            if (categoryId in currentCategories) currentCategories.remove(categoryId) else currentCategories.add(categoryId)
            currentFilters.copy(selectedCategories = currentCategories)
        }
    }

    fun clearCategoryFilters() {
        _searchFilters.update { it.copy(selectedCategories = emptySet()) }
    }

    fun updatePriceRange(min: Double, max: Double) {
        _searchFilters.update { it.copy(minPrice = min, maxPrice = max) }
    }

    fun updateDateRange(minDate: Long?, maxDate: Long?) {
        _searchFilters.update { it.copy(minDate = minDate, maxDate = maxDate) }
    }

    fun clearDateRange() {
        _searchFilters.update { it.copy(minDate = null, maxDate = null) }
    }

    fun clearAllFilters() {
        _searchFilters.value = SearchFilters()
    }

    fun clearSearch() {
        _searchFilters.update { it.copy(query = "") }
    }

    fun toggleFilterSheet() { _showFilterSheet.value = !_showFilterSheet.value }
    fun hideFilterSheet() { _showFilterSheet.value = false }

    fun refreshListings() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try { repository.syncFromRemote() } catch (e: Exception) { } finally { _isRefreshing.value = false }
        }
    }

    fun getActiveFilterCount(): Int {
        val filters = _searchFilters.value
        var count = 0
        if (filters.selectedCategories.isNotEmpty()) count += filters.selectedCategories.size
        if (filters.minPrice > 0.0 || filters.maxPrice < Double.MAX_VALUE) count++
        if (filters.minDate != null || filters.maxDate != null) count++
        return count
    }

    private fun loadFavouriteStates() {
        viewModelScope.launch {
            listings.collect { listingList ->
                val states = mutableMapOf<String, Boolean>()
                listingList.forEach { listing ->
                    states[listing.id] = favouriteRepository.isFavourite(currentUserId, listing.id)
                }
                _favouriteStates.value = states
            }
        }
    }

    fun toggleFavourite(listingId: String) {
        viewModelScope.launch {
            favouriteRepository.toggleFavourite(currentUserId, listingId)
            _favouriteStates.value = _favouriteStates.value.toMutableMap().apply {
                this[listingId] = !(this[listingId] ?: false)
            }
        }
    }
}
