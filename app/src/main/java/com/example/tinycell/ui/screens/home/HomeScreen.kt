
package com.example.tinycell.ui.screens.home
//package com.example.tinycell.ui.screens.profile //  added this

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.material3.ExperimentalMaterial3Api

// these 2 added to surface class types
import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.tinycell.ui.screens.profile.ProfileViewModel // BUGGED WRONG TYPE
import com.example.tinycell.ui.screens.home.HomeViewModel


//experimental feature  of Material 3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel();
    //val viewModel: ProfileViewModel = viewModel<ProfileViewModel>();
    //val viewModel = HomeViewModel()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("TinySell") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(viewModel.listings) { listing ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            navController.navigate("detail/${listing.id}")
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(listing.title, style = MaterialTheme.typography.titleMedium)
                        Text("$${listing.price}")
                        Text("Seller: ${listing.sellerName}")
                    }
                }
            }
        }
    }
}
