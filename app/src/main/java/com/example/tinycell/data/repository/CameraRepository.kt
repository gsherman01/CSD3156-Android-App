package com.example.tinycell.data.repository

import android.content.ContentValues
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
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
    private val _lastCapturedImageUri = MutableStateFlow<Uri?>(null)
    val lastCapturedImageUri: StateFlow<Uri?> = _lastCapturedImageUri.asStateFlow()
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
    fun takePhoto(imageCapture: ImageCapture, onSuccess: () -> Unit) = callbackFlow<Uri> {
        // use date for now, maybe listing id or something in the future so that it theres no conflicts on the server content provider side
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.current.platformLocale).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TinyCell")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let {
                        uri -> trySend(uri)
                        _lastCapturedImageUri.value = uri
                        onSuccess()
                    } ?: close(ImageCaptureException( ImageCapture.ERROR_UNKNOWN, "No URI returned", null ))
                }
                override fun onError(exception: ImageCaptureException) {
                    close(exception)
                }
            }
        )
        awaitClose{ }
    }

    fun clearLastCapturedImageUri() {
        _lastCapturedImageUri.value = null
    }
}

