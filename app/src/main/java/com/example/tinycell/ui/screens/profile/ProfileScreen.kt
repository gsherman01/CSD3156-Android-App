package com.example.tinycell.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.List
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
    onNavigateToMyListings: () -> Unit = {}
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(authRepository) as T
            }
        }
    )

    val userId by viewModel.userId.collectAsState()
    val userName by viewModel.userName.collectAsState()
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
            onClick = onNavigateToMyListings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("My Listings")
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                onReset = { viewModel.resetToRealAuth() }
            )
        }
    }
}

@Composable
fun AdminDebugPanel(
    onSwitchUser: (String) -> Unit,
    onReset: () -> Unit
) {
    var customId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
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
    }
}
