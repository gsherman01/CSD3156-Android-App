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
            // Image section (placeholder for now, ready for AsyncImage/Coil)
            ImagePlaceholder(
                imageUrl = listing.imageUrl,
                contentDescription = listing.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

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
 * Displays a placeholder box for listing images.
 * Future: Replace Box with AsyncImage when Coil is available.
 *
 * Example integration:
 * AsyncImage(
 *     model = imageUrl,
 *     contentDescription = contentDescription,
 *     modifier = modifier,
 *     contentScale = ContentScale.Crop,
 *     placeholder = { ImagePlaceholder(...) },
 *     error = { ImagePlaceholder(...) }
 * )
 */
@Composable
fun ImagePlaceholder(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    // Placeholder box with subtle gradient effect
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // When imageUrl is available and Coil/AsyncImage is added, replace this Box
        // with actual image loading
        Text(
            text = "📷",
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    }
}
