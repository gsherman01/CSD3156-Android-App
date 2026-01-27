
package com.example.tinycell.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.components.ListingCard

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign

/**
 * HOME SCREEN - MARKETPLACE BROWSING (With Search & Filters)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit,
    listingRepository: ListingRepository
) {
    // Initialize ViewModel
    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(listingRepository) as T
            }
        }
    )

    // Collect States
    val listings by viewModel.listings.collectAsState(initial = emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchFilters by viewModel.searchFilters.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val showFilterSheet by viewModel.showFilterSheet.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TinySell") },
                actions = {
                    // Refresh Button
                    IconButton(onClick = { viewModel.refreshListings() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    // Profile Button
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Listing")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchFilters.query,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onClearQuery = { viewModel.clearSearch() },
                onFilterClick = { viewModel.toggleFilterSheet() },
                activeFilterCount = viewModel.getActiveFilterCount()
            )

            // Active Filters Chips
            if (searchFilters.selectedCategories.isNotEmpty() ||
                searchFilters.minPrice > 0.0 ||
                searchFilters.maxPrice < Double.MAX_VALUE ||
                searchFilters.minDate != null ||
                searchFilters.maxDate != null
            ) {
                ActiveFiltersRow(
                    filters = searchFilters,
                    categories = categories,
                    onClearAllFilters = { viewModel.clearAllFilters() },
                    onRemoveCategory = { viewModel.toggleCategoryFilter(it) },
                    onClearDateRange = { viewModel.clearDateRange() }
                )
            }

            // Listings Display
            Box(modifier = Modifier.fillMaxSize()) {
                if (listings.isEmpty() && !isRefreshing) {
                    EmptyState(onRefresh = { viewModel.refreshListings() })
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(
                            items = listings,
                            key = { listing -> listing.id }
                        ) { listing ->
                            ListingCard(
                                listing = listing,
                                onClick = { onNavigateToDetail(listing.id) }
                            )
                        }
                    }
                }

                // Loading Indicator
                if (isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Filter Bottom Sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                viewModel = viewModel,
                categories = categories,
                currentFilters = searchFilters,
                onDismiss = { viewModel.hideFilterSheet() }
            )
        }
    }
}

/**
 * Search Bar Component
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onFilterClick: () -> Unit,
    activeFilterCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search listings...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )

        // Filter Button with Badge
        BadgedBox(
            badge = {
                if (activeFilterCount > 0) {
                    Badge { Text(activeFilterCount.toString()) }
                }
            }
        ) {
            FilledIconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filters")
            }
        }
    }
}

/**
 * Active Filters Display Row
 */
@Composable
fun ActiveFiltersRow(
    filters: SearchFilters,
    categories: List<com.example.tinycell.data.local.entity.CategoryEntity>,
    onClearAllFilters: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onClearDateRange: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category Filter Chips (Multiple)
        items(filters.selectedCategories.toList()) { categoryId ->
            val category = categories.find { it.id == categoryId }
            val displayText = "${category?.icon ?: ""} ${category?.name ?: categoryId}"

            AssistChip(
                onClick = { onRemoveCategory(categoryId) },
                label = { Text(displayText) },
                leadingIcon = { Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) }
            )
        }

        // Price Filter Chip
        if (filters.minPrice > 0.0 || filters.maxPrice < Double.MAX_VALUE) {
            item {
                val priceText = when {
                    filters.maxPrice < Double.MAX_VALUE && filters.minPrice > 0.0 ->
                        "$${filters.minPrice.toInt()}-$${filters.maxPrice.toInt()}"
                    filters.maxPrice < Double.MAX_VALUE ->
                        "Under $${filters.maxPrice.toInt()}"
                    else ->
                        "Over $${filters.minPrice.toInt()}"
                }
                AssistChip(
                    onClick = {},
                    label = { Text(priceText) },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // Date Range Filter Chip
        if (filters.minDate != null || filters.maxDate != null) {
            item {
                val dateText = formatDateRange(filters.minDate, filters.maxDate)
                AssistChip(
                    onClick = { onClearDateRange() },
                    label = { Text(dateText) },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // Clear All Button
        item {
            AssistChip(
                onClick = onClearAllFilters,
                label = { Text("Clear All") },
                leadingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * Filter Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    viewModel: HomeViewModel,
    categories: List<com.example.tinycell.data.local.entity.CategoryEntity>,
    currentFilters: SearchFilters,
    onDismiss: () -> Unit
) {
    var minPrice by remember { mutableStateOf(currentFilters.minPrice.toString()) }
    var maxPrice by remember { mutableStateOf(if (currentFilters.maxPrice == Double.MAX_VALUE) "" else currentFilters.maxPrice.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(onClick = { viewModel.clearAllFilters() }) {
                    Text("Clear All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Section
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Category selection hint
            if (currentFilters.selectedCategories.isNotEmpty()) {
                Text(
                    text = "${currentFilters.selectedCategories.size} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category options (multi-select)
                items(categories) { category ->
                    FilterChip(
                        selected = category.id in currentFilters.selectedCategories,
                        onClick = { viewModel.toggleCategoryFilter(category.id) },
                        label = { Text("${category.icon ?: ""} ${category.name}") },
                        leadingIcon = if (category.id in currentFilters.selectedCategories) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Clear categories button (only show if categories are selected)
            if (currentFilters.selectedCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { viewModel.clearCategoryFilters() }) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear Categories")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Range Section
            Text(
                text = "Price Range",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                            minPrice = it
                        }
                    },
                    label = { Text("Min Price") },
                    prefix = { Text("$") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                            maxPrice = it
                        }
                    },
                    label = { Text("Max Price") },
                    prefix = { Text("$") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date Range Section
            DateRangeFilterSection(
                viewModel = viewModel,
                currentFilters = currentFilters
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Apply Button
            Button(
                onClick = {
                    // Apply price filters
                    val min = minPrice.toDoubleOrNull() ?: 0.0
                    val max = maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE
                    viewModel.updatePriceRange(min, max)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
        }
    }
}

/**
 * Date Range Filter Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterSection(
    viewModel: HomeViewModel,
    currentFilters: SearchFilters
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Text(
        text = "Date Range",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Start Date
        OutlinedButton(
            onClick = { showStartDatePicker = true },
            modifier = Modifier.weight(1f)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "From",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = currentFilters.minDate?.let { formatDate(it) } ?: "Any",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // End Date
        OutlinedButton(
            onClick = { showEndDatePicker = true },
            modifier = Modifier.weight(1f)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "To",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = currentFilters.maxDate?.let { formatDate(it) } ?: "Any",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Clear date range button (only show if dates are selected)
    if (currentFilters.minDate != null || currentFilters.maxDate != null) {
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { viewModel.clearDateRange() }) {
            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Clear Date Range")
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentFilters.minDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            viewModel.updateDateRange(
                                minDate = selectedDate,
                                maxDate = currentFilters.maxDate
                            )
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentFilters.maxDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            // Set time to end of day
                            val endOfDay = selectedDate + (24 * 60 * 60 * 1000 - 1)
                            viewModel.updateDateRange(
                                minDate = currentFilters.minDate,
                                maxDate = endOfDay
                            )
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Helper function to format date for display
 */
fun formatDate(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val year = calendar.get(java.util.Calendar.YEAR)
    return "$month/$day/$year"
}

/**
 * Helper function to format date range for chip display
 */
fun formatDateRange(minDate: Long?, maxDate: Long?): String {
    return when {
        minDate != null && maxDate != null -> "${formatDate(minDate)} - ${formatDate(maxDate)}"
        minDate != null -> "After ${formatDate(minDate)}"
        maxDate != null -> "Before ${formatDate(maxDate)}"
        else -> "Any Date"
    }
}

/**
 * Empty State Display
 */
@Composable
fun EmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No listings found",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try adjusting your filters or search terms",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}
