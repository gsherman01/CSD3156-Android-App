package com.example.tinycell.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.data.repository.ChatRepository
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.screens.camera.CameraScreen
import com.example.tinycell.ui.screens.chat.ChatScreen
import com.example.tinycell.ui.screens.create.CreateListingScreen
import com.example.tinycell.ui.screens.detail.ListingDetailScreen
import com.example.tinycell.ui.screens.home.HomeScreen
import com.example.tinycell.ui.screens.listingchats.ListingChatsScreen
import com.example.tinycell.ui.screens.mylistings.MyListingsScreen
import com.example.tinycell.ui.screens.profile.ProfileScreen

@Composable
fun TinyCellNavHost(
    navController: NavHostController,
    listingRepository: ListingRepository,
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    appContainer: com.example.tinycell.di.AppContainer,
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
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
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
        composable(Screen.ListingDetail.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            ListingDetailScreen(
                listingId = listingId,
                repository = listingRepository,
                authRepository = authRepository,
                chatRepository = chatRepository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { chatRoomId, lId, listingTitle, otherUserId, otherUserName ->
                    navController.navigate(
                        Screen.Chat.createRoute(chatRoomId, lId, listingTitle, otherUserId, otherUserName)
                    )
                }
            )
        }

        // Feature: User Profile & Admin Debug
        composable(Screen.Profile.route) {
            ProfileScreen(
                authRepository = authRepository,
                onNavigateToMyListings = {
                    navController.navigate(Screen.MyListings.route)
                },
                appContainer = appContainer,

            )
        }

        composable("camera") {
            CameraScreen()
        }

        // Feature: Chat
        composable(Screen.Chat.route) { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            val listingTitle = backStackEntry.arguments?.getString("listingTitle")?.let {
                Screen.Chat.decodeTitle(it)
            } ?: ""
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("otherUserName")?.let {
                Screen.Chat.decodeName(it)
            } ?: ""
            val currentUserId = authRepository.getCurrentUserId() ?: ""

            ChatScreen(
                chatRoomId = chatRoomId,
                listingId = listingId,
                listingTitle = listingTitle,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                currentUserId = currentUserId,
                chatRepository = chatRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Feature: My Listings (Seller View)
        composable(Screen.MyListings.route) {
            MyListingsScreen(
                listingRepository = listingRepository,
                chatRepository = chatRepository,
                authRepository = authRepository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToListingChats = { listingId, listingTitle ->
                    navController.navigate(
                        Screen.ListingChats.createRoute(listingId, listingTitle)
                    )
                }
            )
        }

        // Feature: Listing Chats (All chats for a specific listing)
        composable(Screen.ListingChats.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            val listingTitle = backStackEntry.arguments?.getString("listingTitle")?.let {
                Screen.ListingChats.decodeTitle(it)
            } ?: ""

            ListingChatsScreen(
                listingId = listingId,
                listingTitle = listingTitle,
                chatRepository = chatRepository,
                authRepository = authRepository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { chatRoomId, lId, lTitle, otherUserId, otherUserName ->
                    navController.navigate(
                        Screen.Chat.createRoute(chatRoomId, lId, lTitle, otherUserId, otherUserName)
                    )
                }
            )
        }
    }
}
