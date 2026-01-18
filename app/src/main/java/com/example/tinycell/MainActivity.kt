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
        /*

        super: This refers to the parent class (ComponentActivity).
        It tells the Android system to execute the standard setup code
        required to initialize an Activity.

        savedInstanceState: This is a Bundle object that contains
         the activity's previously saved state
         (e.g., if the screen was rotated).
         If the activity is being started for the first time,this is null.
         */
        super.onCreate(savedInstanceState)

        // Initialize the container with the application context
        appContainer = AppContainer(applicationContext)

        setContent {
            TinyCellTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the listing repository to the NavHost
                    // create the navhost here. and passing in the repos.
                    TinyCellNavHost(
                        navController = navController,
                        listingRepository = appContainer.listingRepository
                    )
                }
            }
        }
    }
}