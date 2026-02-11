package com.example.tinycell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
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

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
 * Bottom Navigation Item Definition.
 * Updated to match Carousell flow: Home, Sell, Chats, Profile.
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.CreateListing.route, Icons.Default.AddCircle, "Sell"), // Updated
    BottomNavItem(Screen.AllChats.route, Icons.Default.Chat, "Chats"),
    BottomNavItem(Screen.Profile.route, Icons.Default.Person, "Profile")
)

/**
 * Main Scaffold with Polished Bottom Navigation.
 */
@Composable
fun MainScaffoldWithBottomNav(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Logic: Show bottom nav only on top-level destinations
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
                                if (!selected) { // Prevent re-navigating to the same tab
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
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
