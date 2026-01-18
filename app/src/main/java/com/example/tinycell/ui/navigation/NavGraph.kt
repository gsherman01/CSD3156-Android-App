package com.example.tinycell.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.screens.create.CreateListingScreen
import com.example.tinycell.ui.screens.detail.ListingDetailScreen
import com.example.tinycell.ui.screens.home.HomeScreen
import com.example.tinycell.ui.screens.profile.ProfileScreen
import com.example.tinycell.ui.screens.camera.CameraScreen

@Composable
fun TinyCellNavHost(
    navController: NavHostController,
    listingRepository: ListingRepository,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Main Marketplace View
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail.createRoute(listingId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.CreateListing.route)
                },
                listingRepository = listingRepository
            )
        }

        // Feature: Create New Listing
        composable(Screen.CreateListing.route) {
            CreateListingScreen(
                repository = listingRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Feature: Listing Details
        //yay it is fixed, I believe I had to integrate the dependency issues
        // and then it is better.
        composable(Screen.ListingDetail.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            ListingDetailScreen(
                listingId = listingId,
                repository = listingRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("camera"){
            CameraScreen()
        }
    }
}