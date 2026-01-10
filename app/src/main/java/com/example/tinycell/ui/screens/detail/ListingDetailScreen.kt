package com.example.tinycell.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ListingDetailScreen(listingId: String) {
    val viewModel = ListingDetailViewModel(listingId)

    viewModel.listing?.let {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(it.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Price: $${it.price}")
            Text("Category: ${it.category}")
            Text("Seller: ${it.sellerName}")
        }
    }
}
