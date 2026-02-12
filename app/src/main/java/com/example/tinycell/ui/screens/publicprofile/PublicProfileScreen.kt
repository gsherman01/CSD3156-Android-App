package com.example.tinycell.ui.screens.publicprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.components.ListingCard
import com.example.tinycell.data.local.entity.ReviewEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Public Profile Screen - Shows user listings and reviews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    userName: String,
    listingRepository: ListingRepository,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val viewModel: PublicProfileViewModel = viewModel(
        factory = PublicProfileViewModelFactory(listingRepository, userId)
    )

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Listings, 1: Reviews

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userName) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Header Stats Card
            ProfileHeaderCard(uiState)

            // Tabs for Listings vs Reviews
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Listings (${uiState.listings.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Reviews (${uiState.reviewCount})") })
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (selectedTab == 0) {
                    UserListingsGrid(uiState.listings, onNavigateToDetail)
                } else {
                    UserReviewsList(uiState.reviews)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(uiState: PublicProfileUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "%.1f".format(uiState.averageRating), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Text(text = "from ${uiState.reviewCount} reviews", style = MaterialTheme.typography.bodySmall)
            }
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp)) {
                Text(text = "Active Seller", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun UserListingsGrid(listings: List<com.example.tinycell.data.model.Listing>, onDetail: (String) -> Unit) {
    if (listings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No active listings") }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(listings) { listing -> ListingCard(listing = listing, onClick = { onDetail(listing.id) }) }
        }
    }
}

@Composable
private fun UserReviewsList(reviews: List<ReviewEntity>) {
    if (reviews.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No reviews yet") }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(reviews) { review ->
                ReviewItem(review)
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun ReviewItem(review: ReviewEntity) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row {
                repeat(5) { i -> Icon(Icons.Default.Star, null, tint = if (i < review.rating) Color(0xFFFFC107) else Color.LightGray, modifier = Modifier.size(16.dp)) }
            }
            Text(text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(review.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Text(text = review.comment, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        Text(text = "Review as ${review.role.lowercase()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
    }
}
