package com.example.tinycell.ui.components

import android.text.format.DateUtils
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tinycell.data.model.Listing

/**
 * LISTING CARD COMPONENT (2-Column Optimized)
 */
@Composable
fun ListingCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavourited: Boolean = false,
    onFavouriteClick: (() -> Unit)? = null,
    onSellerClick: ((String, String) -> Unit)? = null
) {
    val favoriteScale by animateFloatAsState(
        targetValue = if (isFavourited) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "favoriteScale"
    )

    Card(
        modifier = modifier.fillMaxWidth().padding(4.dp), 
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                ImagePlaceholder(imageUrl = listing.imageUrls.firstOrNull(), contentDescription = listing.title, modifier = Modifier.fillMaxSize())
                
                if (onFavouriteClick != null) {
                    IconButton(
                        onClick = onFavouriteClick,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp).background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = CircleShape)
                    ) {
                        Icon(imageVector = if (isFavourited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Favorite", tint = if (isFavourited) Color.Red else MaterialTheme.colorScheme.onSurface, modifier = Modifier.scale(favoriteScale).size(18.dp))
                    }
                }
                if (listing.isSold || listing.status != "AVAILABLE") {
                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)) {
                        ListingStatusBadge(isSold = listing.isSold, status = listing.status)
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                Text(
                    text = listing.sellerName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(enabled = onSellerClick != null) { 
                        onSellerClick?.invoke(listing.sellerId, listing.sellerName) 
                    }
                )

                Text(text = listing.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                PriceTag(price = listing.price, textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 4.dp))
                Text(text = getRelativeTime(listing.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

private fun getRelativeTime(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString()
}

@Composable
fun ImagePlaceholder(imageUrl: String?, contentDescription: String, modifier: Modifier = Modifier) {
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(model = imageUrl, contentDescription = contentDescription, modifier = modifier.clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)), contentScale = ContentScale.Crop)
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)), contentAlignment = Alignment.Center) { Text("📷", fontSize = 32.sp) }
    }
}

/**
 * LISTING STATUS BADGE
 * [RESERVED_SUPPORTED]: Added distinct color for RESERVED state.
 */
@Composable
fun ListingStatusBadge(isSold: Boolean, status: String) {
    val color = when {
        isSold || status == "SOLD" -> MaterialTheme.colorScheme.error
        status == "RESERVED" -> Color(0xFF00BCD4) // Cyan for Reserved
        status == "PENDING" -> Color(0xFFFF9800) // Orange for Offer made
        else -> Color(0xFF4CAF50) // Green for Available
    }
    val text = when {
        isSold || status == "SOLD" -> "SOLD"
        status == "RESERVED" -> "RESERVED"
        status == "PENDING" -> "OFFER"
        else -> "NEW"
    }
    Surface(color = color, shape = RoundedCornerShape(4.dp)) {
        Text(text = text, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PriceTag(price: Double, modifier: Modifier = Modifier, textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) {
    Text(text = "$${"%.2f".format(price)}", style = textStyle, color = MaterialTheme.colorScheme.primary, modifier = modifier)
}
