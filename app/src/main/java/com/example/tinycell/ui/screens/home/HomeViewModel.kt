package com.example.tinycell.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.ListingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Filter state data class to hold all filter parameters
 */
data class SearchFilters(
    val query: String = "",
    val selectedCategories: Set<String> = emptySet(), // Empty set = all categories
    val minPrice: Double = 0.0,
    val maxPrice: Double = Double.MAX_VALUE,
    val minDate: Long? = null, // null = no date filter
    val maxDate: Long? = null  // null = no date filter
)

/**
 * HomeViewModel - ViewModel for the home/browse screen.
 *
 * Updated with comprehensive search and filter capabilities:
 * - Text search (title/description)
 * - Category filter
 * - Price range filter
 * - Date range filter
 * - Multiple filters can be applied simultaneously
 */
class HomeViewModel(private val repository: ListingRepository) : ViewModel() {

    // Categories loaded from database
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    // Filter state
    private val _searchFilters = MutableStateFlow(SearchFilters())
    val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()

    // UI State
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    /**
     * Listings filtered based on current search and filter criteria.
     * Reactively updates when filters change.
     */
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
    }

    /**
     * Load available categories from database
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = repository.getAllCategories()
                _categories.value = cats
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchFilters.update { it.copy(query = query) }
    }

    /**
     * Toggle category filter (add or remove from selection)
     */
    fun toggleCategoryFilter(categoryId: String) {
        _searchFilters.update { currentFilters ->
            val currentCategories = currentFilters.selectedCategories.toMutableSet()
            if (categoryId in currentCategories) {
                currentCategories.remove(categoryId)
            } else {
                currentCategories.add(categoryId)
            }
            currentFilters.copy(selectedCategories = currentCategories)
        }
    }

    /**
     * Clear all category filters
     */
    fun clearCategoryFilters() {
        _searchFilters.update { it.copy(selectedCategories = emptySet()) }
    }

    /**
     * Update price range filter
     */
    fun updatePriceRange(min: Double, max: Double) {
        _searchFilters.update {
            it.copy(
                minPrice = min,
                maxPrice = max
            )
        }
    }

    /**
     * Update date range filter
     */
    fun updateDateRange(minDate: Long?, maxDate: Long?) {
        _searchFilters.update {
            it.copy(
                minDate = minDate,
                maxDate = maxDate
            )
        }
    }

    /**
     * Clear date range filter
     */
    fun clearDateRange() {
        _searchFilters.update {
            it.copy(
                minDate = null,
                maxDate = null
            )
        }
    }

    /**
     * Clear all filters
     */
    fun clearAllFilters() {
        _searchFilters.value = SearchFilters()
    }

    /**
     * Clear only search query (keep filters)
     */
    fun clearSearch() {
        _searchFilters.update { it.copy(query = "") }
    }

    /**
     * Toggle filter sheet visibility
     */
    fun toggleFilterSheet() {
        _showFilterSheet.value = !_showFilterSheet.value
    }

    fun hideFilterSheet() {
        _showFilterSheet.value = false
    }

    /**
     * Manual sync/refresh from remote
     */
    fun refreshListings() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.syncFromRemote()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Get active filter count for UI badge
     */
    fun getActiveFilterCount(): Int {
        val filters = _searchFilters.value
        var count = 0
        if (filters.selectedCategories.isNotEmpty()) count += filters.selectedCategories.size
        if (filters.minPrice > 0.0 || filters.maxPrice < Double.MAX_VALUE) count++
        if (filters.minDate != null || filters.maxDate != null) count++
        return count
    }
}
