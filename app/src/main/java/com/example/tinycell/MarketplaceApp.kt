package com.example.tinycell

import android.app.Application
import com.example.tinycell.data.repository.CameraRepository

class MarketplaceApp : Application() {
    // singleton remove if migrating to koin or something
    val cameraRepository by lazy { CameraRepository(this) }
}
