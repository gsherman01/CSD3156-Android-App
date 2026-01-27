package com.example.tinycell.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.repository.AuthRepository

/**
 * [PHASE 5.6]: Enhanced Profile Screen with Admin/Debug Controls.
 * Allows switching users for single-device testing.
 */
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    appContainer: com.example.tinycell.di.AppContainer
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(authRepository, appContainer) as T
            }
        }
    )

    val userId by viewModel.userId.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isGeneratingListings by viewModel.isGeneratingListings.collectAsState()
    var showDebugMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header ---
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "User Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display Current UID and Name
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Username:", style = MaterialTheme.typography.labelSmall)
                Text(text = userName, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Internal UID:", style = MaterialTheme.typography.labelSmall)
                Text(text = userId, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Standard Options ---
        Button(
            onClick = { viewModel.signOut() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Sign Out (Reset Auth)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Admin/Debug Section ---
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = { showDebugMenu = !showDebugMenu },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
        ) {
            Icon(Icons.Default.BugReport, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (showDebugMenu) "Hide Admin Options" else "Show Admin Options")
        }

        if (showDebugMenu) {
            AdminDebugPanel(
                onSwitchUser = { viewModel.switchUser(it) },
                onReset = { viewModel.resetToRealAuth() },
                onGenerateListings = { count -> viewModel.generateSampleListings(count) },
                isGenerating = isGeneratingListings
            )
        }
    }
}

@Composable
fun AdminDebugPanel(
    onSwitchUser: (String) -> Unit,
    onReset: () -> Unit,
    onGenerateListings: (Int) -> Unit,
    isGenerating: Boolean
) {
    var customId by remember { mutableStateOf("") }
    var listingCount by remember { mutableStateOf("5") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        // User Switching Section
        Text(
            text = "Admin Switch User",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Simulate another user (e.g. 'user_admin', 'buyer_1'). This bypasses real Auth for testing purposes.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = customId,
            onValueChange = { customId = it },
            label = { Text("Mock User ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { if (customId.isNotBlank()) onSwitchUser(customId) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply ID")
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(4.dp))
                Text("Reset")
            }
        }

        // Divider
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))

        // Generate Listings Section
        Text(
            text = "Generate Sample Listings",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create sample marketplace listings for testing. Listings will be created under the current user.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = listingCount,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        listingCount = it
                    }
                },
                label = { Text("Count") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isGenerating
            )

            Button(
                onClick = {
                    val count = listingCount.toIntOrNull() ?: 5
                    onGenerateListings(count.coerceIn(1, 20))
                },
                modifier = Modifier.weight(1f),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Generate")
                }
            }
        }
    }
}
