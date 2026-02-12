package com.example.tinycell.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.local.entity.NotificationEntity
import com.example.tinycell.data.repository.ListingRepository
import java.text.SimpleDateFormat
import java.util.*

/**
 * Notification Bulletin Screen - Shows recent alerts for offers and price changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    listingRepository: ListingRepository,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(listingRepository)
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bulletin") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("Mark all read")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.notifications.isEmpty()) {
            EmptyBulletin()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { onNavigateToDetail(notification.referenceId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: NotificationEntity, onClick: () -> Unit) {
    val (icon, color) = when (notification.type) {
        "OFFER_MADE" -> Icons.Default.LocalOffer to Color(0xFF2196F3)
        "OFFER_ACCEPTED" -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        "PRICE_CHANGE" -> Icons.Default.PriceCheck to Color(0xFFFF9800)
        else -> Icons.Default.Notifications to Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface 
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon
            Box(
                modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(notification.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!notification.isRead) {
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            }
        }
    }
}

@Composable
private fun EmptyBulletin() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Notifications, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No new notifications", style = MaterialTheme.typography.titleMedium)
    }
}
