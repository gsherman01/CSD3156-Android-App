package com.example.tinycell

import android.app.Application
import android.util.Log
import com.example.tinycell.di.AppContainer

private const val TAG = "MarketplaceApp"

/**
 * Custom Application class for TinyCell.
 */
class MarketplaceApp : Application() {

    // [FIX]: Make it nullable or handle initialization carefully to prevent UninitializedPropertyAccessException
    private var _container: AppContainer? = null
    val container: AppContainer get() = _container ?: throw IllegalStateException("AppContainer not initialized")

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Starting application...")
        
        try {
            // 1. Initialize Dependency Container
            _container = AppContainer(this)
            Log.d(TAG, "onCreate: AppContainer initialized successfully")
            
            // 2. Trigger Startup Sync
            container.initializeData()
            Log.d(TAG, "onCreate: initializeData() triggered")
            
        } catch (e: Exception) {
            // This will log the actual reason (like a missing Firebase config) 
            // instead of just closing the app silently.
            Log.e(TAG, "onCreate: Critical failure during initialization", e)
        }
    }
}
