
package com.example.tinycell.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.tinycell.ui.screens.create.CreateListingScreen
import com.example.tinycell.ui.screens.detail.ListingDetailScreen
import com.example.tinycell.ui.screens.home.HomeScreen
import com.example.tinycell.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable(
            "detail/{listingId}",
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) {
            ListingDetailScreen(it.arguments?.getString("listingId")!!)
        }
        composable("create") {
            CreateListingScreen()
        }
        composable("profile") {
            ProfileScreen()
        }
    }
}
