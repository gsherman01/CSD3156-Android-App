package com.example.tinycell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.tinycell.di.AppContainer
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

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the listing repository to the NavHost
                    TinyCellNavHost(
                        navController = navController,
                        listingRepository = appContainer.listingRepository
                    )
                }
            }
        }
    }
}
