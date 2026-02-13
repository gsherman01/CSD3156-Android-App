package com.example.tinycell.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.data.repository.ChatRepository
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.ui.screens.allchats.AllChatsScreen
import com.example.tinycell.ui.screens.camera.CameraScreen
import com.example.tinycell.ui.screens.chat.ChatScreen
import com.example.tinycell.ui.screens.create.CreateListingScreen
import com.example.tinycell.ui.screens.detail.ListingDetailScreen
import com.example.tinycell.ui.screens.home.HomeScreen
import com.example.tinycell.ui.screens.listingchats.ListingChatsScreen
import com.example.tinycell.ui.screens.myfavorites.MyFavoritesScreen
import com.example.tinycell.ui.screens.mylistings.MyListingsScreen
import com.example.tinycell.ui.screens.profile.ProfileScreen
import com.example.tinycell.ui.screens.publicprofile.PublicProfileScreen
import com.example.tinycell.ui.screens.notifications.NotificationScreen

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
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { id -> navController.navigate(Screen.ListingDetail.createRoute(id)) },
                onNavigateToCreate = { navController.navigate(Screen.CreateListing.route) },
                onNavigateToMyFavorites = { navController.navigate(Screen.MyFavorites.route) },
                onNavigateToPublicProfile = { userId, userName -> 
                    navController.navigate(Screen.PublicProfile.createRoute(userId, userName)) 
                },
                onNavigateToNotifications = { 
                    navController.navigate(Screen.Notifications.route) 
                },
                listingRepository = listingRepository,
                favouriteRepository = appContainer.favouriteRepository,
                authRepository = authRepository
            )
        }

        composable(Screen.CreateListing.route) {
            CreateListingScreen(repository = listingRepository, onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ListingDetail.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            ListingDetailScreen(
                listingId = listingId, repository = listingRepository, authRepository = authRepository,
                chatRepository = chatRepository, favouriteRepository = appContainer.favouriteRepository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { chatRoomId, lId, title, otherUserId, otherUserName ->
                    navController.navigate(Screen.Chat.createRoute(chatRoomId, lId, title, otherUserId, otherUserName))
                },
                onNavigateToPublicProfile = { userId, userName ->
                    navController.navigate(Screen.PublicProfile.createRoute(userId, userName))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authRepository = authRepository,
                onNavigateToMyListings = { navController.navigate(Screen.MyListings.route) },
                appContainer = appContainer
            )
        }

        composable(Screen.AllChats.route) {
            AllChatsScreen(
                chatRepository = chatRepository, authRepository = authRepository,
                onNavigateToChat = { chatRoomId, lId, title, otherUserId, otherUserName ->
                    navController.navigate(Screen.Chat.createRoute(chatRoomId, lId, title, otherUserId, otherUserName))
                }
            )
        }

        composable(Screen.Chat.route) { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            val listingTitle = backStackEntry.arguments?.getString("listingTitle")?.let { Screen.Chat.decodeTitle(it) } ?: ""
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("otherUserName")?.let { Screen.Chat.decodeName(it) } ?: ""
            ChatScreen(
                chatRoomId = chatRoomId, listingId = listingId, listingTitle = listingTitle,
                otherUserId = otherUserId, otherUserName = otherUserName,
                currentUserId = authRepository.getCurrentUserId() ?: "",
                chatRepository = chatRepository, listingRepository = listingRepository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPublicProfile = { userId, userName ->
                    navController.navigate(Screen.PublicProfile.createRoute(userId, userName))
                }
            )
        }

        composable(Screen.MyListings.route) {
            MyListingsScreen(
                listingRepository = listingRepository, chatRepository = chatRepository, authRepository = authRepository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToListingChats = { lId, title -> navController.navigate(Screen.ListingChats.createRoute(lId, title)) }
            )
        }

        composable(Screen.MyFavorites.route) {
            MyFavoritesScreen(
                favouriteRepository = appContainer.favouriteRepository, authRepository = authRepository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.ListingDetail.createRoute(id)) }
            )
        }

        composable(Screen.ListingChats.route) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            val listingTitle = backStackEntry.arguments?.getString("listingTitle")?.let { Screen.ListingChats.decodeTitle(it) } ?: ""
            ListingChatsScreen(
                listingId = listingId, listingTitle = listingTitle, chatRepository = chatRepository, authRepository = authRepository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { chatRoomId, lId, title, otherUserId, otherUserName ->
                    navController.navigate(Screen.Chat.createRoute(chatRoomId, lId, title, otherUserId, otherUserName))
                }
            )
        }

        composable(Screen.PublicProfile.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName")?.let { Screen.PublicProfile.decodeName(it) } ?: ""
            PublicProfileScreen(
                userId = userId, userName = userName, listingRepository = listingRepository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.ListingDetail.createRoute(id)) }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                listingRepository = listingRepository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.ListingDetail.createRoute(id)) }
            )
        }
    }
}
