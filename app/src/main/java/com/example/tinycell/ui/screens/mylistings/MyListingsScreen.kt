package com.example.tinycell.ui.screens.mylistings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.data.repository.ChatRepository
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.components.PriceTag
import com.example.tinycell.ui.components.ListingStatusBadge

/**
 * My Listings Screen - Updated to 2-column grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    listingRepository: ListingRepository,
    chatRepository: ChatRepository,
    authRepository: AuthRepository,
    onNavigateBack: () -> Unit,
    onNavigateToListingChats: (listingId: String, listingTitle: String) -> Unit
) {
    val viewModel: MyListingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MyListingsViewModel(listingRepository, chatRepository, authRepository) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Listings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            FilterRow(
                currentFilter = uiState.currentFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.filteredListings.isEmpty()) {
                    EmptyState()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.filteredListings) { item ->
                            MyListingCard(
                                listingWithChats = item,
                                onClick = { onNavigateToListingChats(item.listing.id, item.listing.title) },
                                onMarkAsSold = { viewModel.markAsSold(item.listing.id) },
                                onDelete = { viewModel.deleteListing(item.listing) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(currentFilter: ListingFilter, onFilterSelected: (ListingFilter) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ListingFilter.values()) { filter ->
            FilterChip(selected = currentFilter == filter, onClick = { onFilterSelected(filter) }, label = { Text(filter.name) })
        }
    }
}

/**
 * A compact card for the 2-column My Listings grid.
 */
@Composable
private fun MyListingCard(
    listingWithChats: ListingWithChats,
    onClick: () -> Unit,
    onMarkAsSold: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                // [FIXED]: Access the first image from the list
                val thumbnail = listingWithChats.listing.imageUrls.firstOrNull()
                if (!thumbnail.isNullOrBlank()) {
                    AsyncImage(model = thumbnail, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text("📷") }
                }
                ListingStatusBadge(isSold = listingWithChats.listing.isSold, status = listingWithChats.listing.status)
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = listingWithChats.listing.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                PriceTag(price = listingWithChats.listing.price)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Chat, "Chats", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${listingWithChats.chatCount} chats", style = MaterialTheme.typography.labelSmall)
                    if (listingWithChats.totalUnreadCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Badge { Text("${listingWithChats.totalUnreadCount}") }
                    }
                }
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                if (!listingWithChats.listing.isSold) {
                    TextButton(onClick = onMarkAsSold, contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sell", fontSize = 12.sp)
                    }
                }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error), contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No listings found", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
