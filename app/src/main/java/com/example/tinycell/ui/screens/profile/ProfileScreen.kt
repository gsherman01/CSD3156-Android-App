package com.example.tinycell.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.repository.AuthRepository

/**
 * Profile Screen with dynamic sample data generation.
 */
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onNavigateToMyListings: () -> Unit = {},
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
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val isGenerating by viewModel.isGeneratingListings.collectAsState()
    val sampleCount by viewModel.sampleCountInput.collectAsState()
    var showDebugMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Person, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text = "User Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Username:", style = MaterialTheme.typography.labelSmall)
                    Text(text = userName, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Internal UID:", style = MaterialTheme.typography.labelSmall)
                    Text(text = userId, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = { viewModel.showEditDialog() }) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToMyListings, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("My Listings")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { viewModel.signOut() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Sign Out (Reset Auth)")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        TextButton(onClick = { showDebugMenu = !showDebugMenu }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
            Icon(Icons.Default.BugReport, null)
            Spacer(Modifier.width(8.dp))
            Text(if (showDebugMenu) "Hide Admin Options" else "Show Admin Options")
        }

        if (showDebugMenu) {
            AdminDebugPanel(
                sampleCount = sampleCount,
                isGenerating = isGenerating,
                onCountChange = { viewModel.onSampleCountChange(it) },
                onGenerate = { viewModel.generateSampleListings() },
                onSwitchUser = { viewModel.switchUser(it) },
                onReset = { viewModel.resetToRealAuth() }
            )
        }
    }

    if (showEditDialog) {
        ProfileEditDialog(currentName = userName, onDismiss = { viewModel.hideEditDialog() }, onConfirm = { viewModel.updateUserName(it) })
    }
}

@Composable
fun AdminDebugPanel(
    sampleCount: String,
    isGenerating: Boolean,
    onCountChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onSwitchUser: (String) -> Unit,
    onReset: () -> Unit
) {
    var customId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(16.dp)
    ) {
        Text(text = "Mock Listings", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = sampleCount, onValueChange = onCountChange,
                label = { Text("Count") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onGenerate, enabled = !isGenerating && sampleCount.isNotEmpty()) {
                if (isGenerating) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Generate")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Admin Switch User", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
        OutlinedTextField(value = customId, onValueChange = { customId = it }, label = { Text("Mock User ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (customId.isNotBlank()) onSwitchUser(customId) }, modifier = Modifier.weight(1f)) { Text("Apply ID") }
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, null); Spacer(modifier = Modifier.width(4.dp)); Text("Reset") }
        }
    }
}

@Composable
fun ProfileEditDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var editedName by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = { OutlinedTextField(value = editedName, onValueChange = { editedName = it }, label = { Text("Display Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(onClick = { if (editedName.isNotBlank()) onConfirm(editedName) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
