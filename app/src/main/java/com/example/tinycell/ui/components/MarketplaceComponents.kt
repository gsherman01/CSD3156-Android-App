package com.example.tinycell.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tinycell.data.model.Listing

/**
 * LISTING CARD COMPONENT
 *
 * Carousell-inspired marketplace listing card.
 * Design principles:
 * - Image-first layout (large square placeholder)
 * - Prominent price display
 * - Compact, scannable information
 * - Clear visual separation between cards
 *
 * Usage: Display in LazyColumn/LazyVerticalGrid for browsing experience
 */
@Composable
fun ListingCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image section with status badge overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                ImagePlaceholder(
                    imageUrl = listing.imageUrl,
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Status badge overlay (top-right corner)
                if (listing.isSold || listing.status != "AVAILABLE") {
                    ListingStatusBadge(
                        isSold = listing.isSold,
                        status = listing.status,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }

            // Content section: title, price, seller
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title - Max 2 lines with ellipsis (Carousell style)
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price - Prominent display (key Carousell feature)
                PriceTag(
                    price = listing.price,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Seller info - Subtle, secondary information
                Text(
                    text = listing.sellerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * PRICE TAG COMPONENT
 *
 * Displays price with currency formatting.
 * Design: Bold, prominent, uses primary color to catch attention.
 *
 * Reusable across listing cards, detail screens, and checkout flows.
 */
@Composable
fun PriceTag(
    price: Double,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold
    ),
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = "$${"%.2f".format(price)}",
        style = textStyle,
        color = color,
        modifier = modifier
    )
}

/**
 * IMAGE PLACEHOLDER COMPONENT
 *
 * Displays listing images using Coil's AsyncImage with proper loading states.
 * Shows:
 * - Actual image when URL is valid
 * - Placeholder while loading
 * - Error state with camera icon if image fails to load or URL is null
 */
@Composable
fun ImagePlaceholder(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    if (!imageUrl.isNullOrBlank()) {
        // Load actual image with Coil
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        // No image URL provided
        PlaceholderBox(modifier = modifier.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)))
    }
}

/**
 * Placeholder box displayed when image is loading, failed, or not available
 */
@Composable
private fun PlaceholderBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📷",
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    }
}

/**
 * LISTING STATUS BADGE
 *
 * Displays the current status of a listing with appropriate color coding.
 * - SOLD: Red background, indicates listing is no longer available
 * - PENDING: Orange background, indicates listing has active offers
 * - AVAILABLE: Green background, indicates listing is ready for purchase
 */
@Composable
fun ListingStatusBadge(
    isSold: Boolean,
    status: String,
    modifier: Modifier = Modifier
) {
    val (text, containerColor, contentColor) = when {
        isSold -> Triple(
            "SOLD",
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError
        )
        status == "PENDING" -> Triple(
            "UNDER OFFER",
            Color(0xFFFF9800), // Orange
            Color.White
        )
        else -> Triple(
            "AVAILABLE",
            Color(0xFF4CAF50), // Green
            Color.White
        )
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        modifier = modifier.height(24.dp)
    )
}
