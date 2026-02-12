package com.example.tinycell.ui.screens.myfavorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.data.repository.FavouriteRepository
import com.example.tinycell.ui.components.ListingCard

/**
 * My Favorites Screen - Updated to 2-column grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFavoritesScreen(
    favouriteRepository: FavouriteRepository,
    authRepository: AuthRepository,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val currentUserId = authRepository.getCurrentUserId() ?: ""

    val viewModel: MyFavoritesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MyFavoritesViewModel(favouriteRepository, currentUserId) as T
            }
        }
    )

    val listings by viewModel.favoriteListings.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            listings.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No favorites yet", style = MaterialTheme.typography.bodyLarge)
                        Text("Tap the heart icon on listings to save them here", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = listings,
                        key = { it.id }
                    ) { listing ->
                        ListingCard(
                            listing = listing,
                            onClick = { onNavigateToDetail(listing.id) },
                            isFavourited = true,
                            onFavouriteClick = { viewModel.removeFavorite(listing.id) }
                        )
                    }
                }
            }
        }
    }
}
