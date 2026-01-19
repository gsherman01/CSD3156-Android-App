package com.example.tinycell

import android.app.Application
import com.example.tinycell.di.AppContainer

/**
 * Custom Application class for TinyCell.
 *
 * [LEARNING_POINT: APP LIFECYCLE]
 * We initialize the AppContainer here to ensure dependencies are available
 * throughout the app's lifecycle. We also trigger the initial data sync.
 */
class MarketplaceApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Dependency Container
        container = AppContainer(this)
        
        // 2. [PHASE 3]: Trigger Startup Sync
        // This seeds local data and hydrates Room from Firestore
        container.initializeData()
    }
}
