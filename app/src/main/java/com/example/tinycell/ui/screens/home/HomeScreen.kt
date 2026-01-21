
package com.example.tinycell.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh

/**
 * HOME SCREEN - MARKETPLACE BROWSING (Full Featured)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProfile: () -> Unit, // Added for navigation
    listingRepository: ListingRepository
) {
    // 1. Initialize ViewModel
    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(listingRepository) as T
            }
        }
    )

    // 2. Collect States
    val listings by viewModel.listings.collectAsState(initial = emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TinySell") },
                actions = {
                    // Refresh Button (Manual Sync)
                    IconButton(onClick = { viewModel.refreshListings() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    // Profile Button (Admin/Debug Access)
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // 3. Display Listings
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

            // Simple Loading Overlay for Refreshing
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No listings found", style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onRefresh) {
            Text("Try Refreshing")
        }
    }
}
