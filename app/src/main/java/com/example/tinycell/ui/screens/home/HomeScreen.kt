
package com.example.tinycell.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.components.ListingCard

/**
 * HOME SCREEN - MARKETPLACE BROWSING
 *
 * Carousell-inspired browsing experience:
 * - Vertical scrolling list of listings (optimized for mobile)
 * - Image-first card layout for quick scanning
 * - Prominent pricing and seller info
 * - Tap-to-view-details interaction
 *
 * Architecture:
 * - Collects StateFlow from HomeViewModel
 * - Stateless Composable (no business logic)
 * - Delegates navigation to NavController
 * - Uses reusable ListingCard component
 *
 * Future enhancements ready:
 * - Pull-to-refresh
 * - Search bar integration
 * - Category filters
 * - Loading/error states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // Get application context to initialize database
    val context = androidx.compose.ui.platform.LocalContext.current

    // Create ViewModel with database dependencies
    // TODO: Replace with proper DI (Hilt/Koin) in production
    val viewModel: HomeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = com.example.tinycell.data.local.AppDatabase.getDatabase(context)
                val repository = com.example.tinycell.data.repository.ListingRepository(database.listingDao())
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
        }
    )

    val listings by viewModel.listings.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TinySell") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        // Marketplace listing grid
        // Note: Using LazyColumn for vertical scrolling (Carousell pattern)
        // Alternative: LazyVerticalGrid for multi-column tablet layouts
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(
                items = listings,
                key = { listing -> listing.id }  // Optimize recomposition
            ) { listing ->
                // Reusable listing card component
                ListingCard(
                    listing = listing,
                    onClick = {
                        // Navigate to detail screen with listing ID
                        navController.navigate("detail/${listing.id}")
                    }
                )
            }
        }
    }
}
