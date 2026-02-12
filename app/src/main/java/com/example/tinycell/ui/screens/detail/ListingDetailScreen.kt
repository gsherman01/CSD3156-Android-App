package com.example.tinycell.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.data.repository.ChatRepository
import com.example.tinycell.data.repository.FavouriteRepository
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.components.PriceTag
import com.example.tinycell.ui.components.ListingStatusBadge
import kotlinx.coroutines.launch

/**
 * LISTING DETAIL SCREEN
 * Updated to support Public Profile navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    repository: ListingRepository,
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    favouriteRepository: FavouriteRepository,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (chatRoomId: String, listingId: String, listingTitle: String, otherUserId: String, otherUserName: String) -> Unit,
    onNavigateToPublicProfile: (userId: String, userName: String) -> Unit // Added missing parameter
) {
    val currentUserId = authRepository.getCurrentUserId() ?: ""

    val viewModel: ListingDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ListingDetailViewModel(repository, favouriteRepository, listingId, currentUserId) as T
            }
        }
    )

    val listing by viewModel.listing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFavourited by viewModel.isFavourited.collectAsState()
    val favouriteCount by viewModel.favouriteCount.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isStartingChat by remember { mutableStateOf(false) }

    val favoriteScale by animateFloatAsState(
        targetValue = if (isFavourited) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "favoriteScale"
    )

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    listing?.let { listingData ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Listing Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            if (favouriteCount > 0) {
                                Text(text = favouriteCount.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(end = 4.dp))
                            }
                            IconButton(onClick = { viewModel.toggleFavourite() }) {
                                Icon(imageVector = if (isFavourited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Favorite", tint = if (isFavourited) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.scale(favoriteScale))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer, titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState)) {
                if (!listingData.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = listingData.imageUrl, contentDescription = listingData.title, modifier = Modifier.fillMaxWidth().height(300.dp), contentScale = ContentScale.Crop)
                } else {
                    ImagePlaceholderBox()
                }

                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(text = listingData.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    ListingStatusBadge(isSold = listingData.isSold, status = listingData.status)
                    Spacer(modifier = Modifier.height(12.dp))
                    PriceTag(price = listingData.price, textStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(20.dp))

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            InfoRow(label = "Category", value = listingData.category)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // [FIX]: Make Seller row clickable to navigate to public profile
                            InfoRow(
                                label = "Seller", 
                                value = listingData.sellerName,
                                modifier = Modifier.clickable { onNavigateToPublicProfile(listingData.sellerId, listingData.sellerName) }
                            )
                        }
                    }

                    listingData.description?.let { desc ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Description", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = desc, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    val isCurrentUserSeller = currentUserId == listingData.sellerId
                    if (!isCurrentUserSeller && !listingData.isSold) {
                        Button(
                            onClick = {
                                isStartingChat = true
                                coroutineScope.launch {
                                    try {
                                        val chatRoom = chatRepository.getOrCreateChatRoom(listingId = listingData.id, listingTitle = listingData.title, buyerId = currentUserId, sellerId = listingData.sellerId)
                                        onNavigateToChat(chatRoom.id, listingData.id, listingData.title, listingData.sellerId, listingData.sellerName)
                                    } finally { isStartingChat = false }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !isStartingChat,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isStartingChat) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp) } 
                            else {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Chat with Seller", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = "Listing not found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ImagePlaceholderBox() {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        Text(text = "📷", fontSize = 72.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    }
}

@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}
