
package com.example.tinycell.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
    // ViewModel provides StateFlow of listings
    val viewModel: HomeViewModel = viewModel()
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
