package com.example.tinycell.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.components.PriceTag

/**
 * LISTING DETAIL SCREEN
 *
 * Carousell-inspired detail view:
 * - Large hero image at top (full-width)
 * - Prominent price display
 * - Clear information hierarchy (title → price → seller → description)
 * - Vertically scrollable for long descriptions
 * - Ready for action buttons (Chat, Buy, Add to Cart)
 *
 * Architecture:
 * - Gets listing data from ListingDetailViewModel
 * - Stateless Composable (no business logic)
 * - Uses Material3 components for consistent theming
 *
 * Future enhancements:
 * - Image gallery with swipe (if multiple images)
 * - Seller profile button
 * - Chat/Contact seller CTA
 * - Similar listings carousel
 * - Favorite/bookmark action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(listingId: String) {
    // Get application context to initialize database
    val context = LocalContext.current

    // Create ViewModel with database dependencies
    // TODO: Replace with proper DI (Hilt/Koin) in production
    val viewModel: ListingDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context)
                val repository = ListingRepository(database.listingDao())
                @Suppress("UNCHECKED_CAST")
                return ListingDetailViewModel(repository, listingId) as T
            }
        }
    )

    val listing by viewModel.listing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()

    // Show loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Handle listing data (null safety)
    listing?.let { listingData ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Listing Details") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Hero Image Section
                // Large image placeholder (ready for AsyncImage/Coil integration)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Replace with AsyncImage when Coil is available
                    // AsyncImage(model = listing.imageUrl, contentDescription = listing.title)
                    Text(
                        text = "📷",
                        fontSize = 72.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }

                // Content Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Title - Prominent, bold
                    Text(
                        text = listingData.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price - Large, primary color (Carousell style)
                    PriceTag(
                        price = listingData.price,
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Info Card - Category & Seller
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Category
                            InfoRow(
                                label = "Category",
                                value = listingData.category
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Seller
                            InfoRow(
                                label = "Seller",
                                value = listingData.sellerName
                            )
                        }
                    }

                    // Description Section (if available)
                    listingData.description?.let { desc ->
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )
                    }

                    // Spacing for bottom actions (future: Chat, Buy buttons)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    } ?: run {
        // Listing not found state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Listing not found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * INFO ROW COMPONENT
 *
 * Reusable component for displaying label-value pairs
 * (e.g., Category: Electronics, Seller: John Doe)
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
