package com.example.tinycell.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tinycell.data.model.ChatMessage
import com.example.tinycell.data.model.Listing
import com.example.tinycell.data.repository.ChatRepository
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.components.ListingStatusBadge
import com.example.tinycell.ui.components.PriceTag
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat Screen - Professional Marketplace Flow.
 * Features: Banners, Guidance, One-time Reviews, and Profile Links.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRoomId: String, listingId: String, listingTitle: String,
    otherUserId: String, otherUserName: String, currentUserId: String,
    chatRepository: ChatRepository, listingRepository: ListingRepository,
    onNavigateBack: () -> Unit,
    onNavigateToPublicProfile: (String, String) -> Unit // New callback
) {
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(chatRepository, listingRepository, chatRoomId, listingId, currentUserId, otherUserId)
    )

    val uiState by viewModel.uiState.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val offerAmount by viewModel.offerAmount.collectAsState()
    val listState = rememberLazyListState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.sendImage(it.toString()) } }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) { listState.animateScrollToItem(uiState.messages.size - 1) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    if (uiState.showOfferDialog) {
        OfferInputDialog(amount = offerAmount, onAmountChange = viewModel::onOfferAmountChanged, onDismiss = { viewModel.toggleOfferDialog(false) }, onConfirm = { viewModel.sendOffer() })
    }

    if (uiState.showReviewDialog) {
        ReviewDialog(onDismiss = { viewModel.toggleReviewDialog(false) }, onSubmit = { rating, comment -> viewModel.submitReview(rating, comment) })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.clickable { onNavigateToPublicProfile(otherUserId, otherUserName) }) {
                        Text(text = otherUserName, style = MaterialTheme.typography.titleMedium)
                        Text(text = "View Profile", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer, titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                messageText = messageText, onMessageTextChanged = viewModel::onMessageTextChanged,
                onSendClick = { viewModel.sendMessage() }, onOfferClick = { viewModel.toggleOfferDialog(true) },
                onImageClick = { imagePickerLauncher.launch("image/*") }, isSeller = uiState.isSeller, isSold = uiState.listing?.status == "SOLD"
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // High-Visibility Transaction Banner
            uiState.listing?.let { listing ->
                TransactionBanner(
                    listing = listing, isSeller = uiState.isSeller, hasReviewed = uiState.hasReviewed,
                    onCompleteSale = { viewModel.markAsSold() }, onReview = { viewModel.toggleReviewDialog(true) }
                )
                ChatProductHeader(listing = listing, onProfileClick = { onNavigateToPublicProfile(otherUserId, otherUserName) })
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.messages.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        state = listState, modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = uiState.messages, key = { it.id }) { message ->
                            when (message.messageType) {
                                "OFFER" -> {
                                    val status = uiState.offerStatuses[message.offerId] ?: "PENDING"
                                    OfferCard(
                                        message = message, status = status, isLatest = message.offerId == uiState.activeOfferId,
                                        isCurrentUser = message.senderId == currentUserId, isSeller = uiState.isSeller,
                                        listingIsSold = uiState.listing?.status == "SOLD",
                                        onAccept = { viewModel.acceptOffer(message.offerId ?: "") },
                                        onReject = { viewModel.rejectOffer(message.offerId ?: "") }
                                    )
                                }
                                "IMAGE" -> ImageMessageBubble(message = message, isCurrentUser = message.senderId == currentUserId)
                                else -> MessageBubble(message = message, isCurrentUser = message.senderId == currentUserId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionBanner(
    listing: Listing, isSeller: Boolean, hasReviewed: Boolean,
    onCompleteSale: () -> Unit, onReview: () -> Unit
) {
    when (listing.status) {
        "RESERVED" -> {
            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Item is Reserved", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("Click 'Complete' once the transaction is finished.", style = MaterialTheme.typography.bodySmall)
                    }
                    if (isSeller) {
                        Button(onClick = onCompleteSale, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                            Text("Complete")
                        }
                    }
                }
            }
        }
        "SOLD" -> {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                    Spacer(Modifier.width(12.dp))
                    Text("Transaction Completed", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    if (!hasReviewed) {
                        Button(onClick = onReview) { Text("Rate Now") }
                    } else {
                        Text("Reviewed ✅", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatProductHeader(listing: Listing, onProfileClick: () -> Unit) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!listing.imageUrl.isNullOrBlank()) {
                AsyncImage(model = listing.imageUrl, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text("📷", fontSize = 20.sp) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = listing.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriceTag(price = listing.price, textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(8.dp))
                    ListingStatusBadge(isSold = listing.isSold, status = listing.status)
                }
            }
            TextButton(onClick = onProfileClick) { Text("Seller Profile", fontSize = 12.sp) }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun OfferCard(
    message: ChatMessage, status: String, isLatest: Boolean,
    isCurrentUser: Boolean, isSeller: Boolean, listingIsSold: Boolean,
    onAccept: () -> Unit, onReject: () -> Unit
) {
    val (headerText, color, icon) = when (status) {
        "ACCEPTED" -> Triple("OFFER ACCEPTED", Color(0xFF4CAF50), Icons.Default.CheckCircle)
        "REJECTED" -> Triple("OFFER REJECTED", MaterialTheme.colorScheme.error, Icons.Default.Cancel)
        else -> if (isLatest && !listingIsSold) Triple("ACTIVE OFFER", MaterialTheme.colorScheme.tertiary, Icons.Default.LocalOffer)
                else Triple("EXPIRED OFFER", MaterialTheme.colorScheme.outline, Icons.Default.LocalOffer)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start) {
        Card(
            colors = CardDefaults.cardColors(containerColor = if (status == "PENDING" && isLatest && !listingIsSold) MaterialTheme.colorScheme.tertiaryContainer else color.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp), modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = headerText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = message.message, style = MaterialTheme.typography.headlineSmall, color = if (status == "PENDING" && !isLatest) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface)
                if (isSeller && !isCurrentUser && status == "PENDING" && isLatest && !listingIsSold) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.weight(1f)) { Text("Accept") }
                        OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Reject") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Rate your experience") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(5) { i -> IconButton(onClick = { rating = i + 1 }) { Icon(if (i < rating) Icons.Default.Star else Icons.Outlined.StarBorder, null, tint = if (i < rating) Color(0xFFFFC107) else Color.Gray) } }
                }
                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Add a comment") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), minLines = 3)
            }
        },
        confirmButton = { Button(onClick = { onSubmit(rating, comment) }) { Text("Submit") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ImageMessageBubble(message: ChatMessage, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.widthIn(max = 240.dp).padding(vertical = 4.dp)) {
            AsyncImage(model = message.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
private fun ChatInputBar(
    messageText: String, onMessageTextChanged: (String) -> Unit,
    onSendClick: () -> Unit, onOfferClick: () -> Unit, onImageClick: () -> Unit,
    isSeller: Boolean, isSold: Boolean
) {
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!isSold) {
                IconButton(onClick = onImageClick) { Icon(Icons.Default.AddPhotoAlternate, "Send Image", tint = MaterialTheme.colorScheme.primary) }
                if (!isSeller) { IconButton(onClick = onOfferClick) { Icon(Icons.Default.LocalOffer, "Make Offer", tint = MaterialTheme.colorScheme.primary) } }
            }
            OutlinedTextField(value = messageText, onValueChange = onMessageTextChanged, placeholder = { Text(if (isSold) "Item sold" else "Type a message...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), maxLines = 4, enabled = !isSold)
            IconButton(onClick = onSendClick, enabled = messageText.isNotBlank() && !isSold) { Icon(Icons.AutoMirrored.Filled.Send, "Send") }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val bg = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val tc = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(modifier = Modifier.widthIn(max = 280.dp).clip(RoundedCornerShape(12.dp)).background(bg).padding(12.dp)) {
            Text(text = message.message, style = MaterialTheme.typography.bodyMedium, color = tc)
            Text(text = formatTimestamp(message.timestamp), style = MaterialTheme.typography.labelSmall, color = tc.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.End))
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
private fun OfferInputDialog(amount: String, onAmountChange: (String) -> Unit, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Make an Offer") },
        text = { OutlinedTextField(value = amount, onValueChange = onAmountChange, label = { Text("Amount ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(onClick = onConfirm, enabled = amount.toDoubleOrNull() != null) { Text("Send Offer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
