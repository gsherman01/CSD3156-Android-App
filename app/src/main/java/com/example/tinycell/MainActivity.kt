package com.example.tinycell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.local.DatabaseSeeder
import com.example.tinycell.ui.navigation.NavGraph
import com.example.tinycell.ui.theme.TinyCellTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Seed database with sample data on first launch
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            val seeder = DatabaseSeeder(database)
            seeder.seedDatabase()
        }

        setContent {
            TinyCellTheme {
                NavGraph()
            }
        }
    }
}
