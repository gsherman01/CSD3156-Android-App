package com.example.tinycell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tinycell.ui.navigation.NavGraph
import com.example.tinycell.ui.theme.TinyCellTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TinyCellTheme {
                NavGraph()
            }
        }
    }
}
