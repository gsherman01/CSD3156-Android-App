package com.example.tinycell.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val viewModel = ListingDetailViewModel(listingId)
    val scrollState = rememberScrollState()

    // Handle listing data (null safety)
    viewModel.listing?.let { listing ->
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
                        text = listing.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price - Large, primary color (Carousell style)
                    PriceTag(
                        price = listing.price,
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
                                value = listing.category
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Seller
                            InfoRow(
                                label = "Seller",
                                value = listing.sellerName
                            )
                        }
                    }

                    // Description Section (if available)
                    listing.description?.let { desc ->
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
