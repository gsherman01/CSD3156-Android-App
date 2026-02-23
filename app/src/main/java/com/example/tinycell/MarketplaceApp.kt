package com.example.tinycell

import android.app.Application
import android.util.Log
import com.example.tinycell.di.AppContainer
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

private const val TAG = "MarketplaceApp"

/**
 * Custom Application class for TinyCell.
 * Updated with Firebase App Check for enhanced security.
 */
class MarketplaceApp : Application() {

    private var _container: AppContainer? = null
    val container: AppContainer get() = _container ?: throw IllegalStateException("AppContainer not initialized")

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Starting application...")
        
        try {
            // 1. Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // 2. Initialize App Check with Play Integrity
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "onCreate: Firebase App Check initialized with Play Integrity")

            // 3. Initialize Dependency Container
            _container = AppContainer(this)
            Log.d(TAG, "onCreate: AppContainer initialized successfully")
            
            // 4. Trigger Startup Sync
            container.initializeData()
            Log.d(TAG, "onCreate: initializeData() triggered")
            
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Critical failure during initialization", e)
        }
    }
}
