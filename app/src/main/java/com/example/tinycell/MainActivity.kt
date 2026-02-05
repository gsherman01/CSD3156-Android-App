package com.example.tinycell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tinycell.di.AppContainer
import com.example.tinycell.ui.navigation.Screen
import com.example.tinycell.ui.navigation.TinyCellNavHost
import com.example.tinycell.ui.theme.TinyCellTheme


class MainActivity : ComponentActivity() {

    // Manual DI Container
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [FIX]: Access the container from MarketplaceApp to ensure single instance
        // and that initializeData() has been triggered.
        appContainer = (application as MarketplaceApp).container

        setContent {
            TinyCellTheme {
                val navController = rememberNavController()

                MainScaffoldWithBottomNav(
                    navController = navController,
                    appContainer = appContainer
                )
            }
        }
    }
}

/**
 * Bottom Navigation Item Definition
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.AllChats.route, Icons.Default.Chat, "Chats"),
    BottomNavItem(Screen.MyListings.route, Icons.Default.List, "My Listings"),
    BottomNavItem(Screen.Profile.route, Icons.Default.Person, "Profile")
)

/**
 * Main Scaffold with Bottom Navigation
 * Shows bottom nav only on top-level screens
 */
@Composable
fun MainScaffoldWithBottomNav(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if bottom nav should be shown
    val bottomNavRoutes = bottomNavItems.map { it.route }
    val shouldShowBottomNav = currentDestination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination to avoid building up a large stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(text = item.label)
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            TinyCellNavHost(
                navController = navController,
                listingRepository = appContainer.listingRepository,
                authRepository = appContainer.authRepository,
                chatRepository = appContainer.chatRepository,
                appContainer = appContainer
            )
        }
    }
}
