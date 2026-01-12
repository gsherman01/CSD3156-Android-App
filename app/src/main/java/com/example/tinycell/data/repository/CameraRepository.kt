package com.example.tinycell.data.repository

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * TODO: Camera Integration Roadmap
 * 1. [PERMISSIONS]: Implement runtime permission checking logic before accessing camera methods.
 * 2. [PROVIDER]: Initialize ProcessCameraProvider to bind the camera lifecycle to the UI.
 * 3. [IMAGE_CAPTURE]: Implement 'takePhoto' logic using ImageCapture.OnImageSavedCallback.
 * 4. [PREVIEW]: Provide a SurfaceProvider to the UI for the CameraPreview Composable.
 */
class CameraRepository(private val context: Context) {

    suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        // Use the Runnable-based addListener provided by the ListenableFuture interface
        cameraProviderFuture.addListener({
            try {
                continuation.resume(cameraProviderFuture.get())
            } catch (e: Exception) {
                // TODO: Handle initialization error properly in the next iteration
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // TODO: Implement capture and analysis methods once UI is scaffolded
}

