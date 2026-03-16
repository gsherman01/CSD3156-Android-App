package com.example.tinycell.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * CameraRepository
 *
 * Handles CameraX interactions:
 * - CameraProvider acquisition
 * - Image capture
 * - MediaStore persistence
 *
 * Kotlin 1.9.24 safe
 */
class CameraRepository(private val context: Context) {

    /**
     * Suspends until CameraProvider is available.
     */
    suspend fun getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener(
                {
                    try {
                        continuation.resume(cameraProviderFuture.get())
                    } catch (e: Exception) {
                        // TODO: Surface error handling in ViewModel later
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }

    /**
     * Captures a photo and emits the saved image Uri.
     */
    fun takePhoto(imageCapture: ImageCapture) = callbackFlow<Uri> {

        val name = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/TinyCell"
                )
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    output: ImageCapture.OutputFileResults
                ) {
                    val uri = output.savedUri
                    if (uri != null) {
                        trySend(uri)
                    } else {
                        close(
                            ImageCaptureException(
                                ImageCapture.ERROR_UNKNOWN,
                                "Image saved but Uri was null",
                                null
                            )
                        )
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    close(exception)
                }
            }
        )

        awaitClose { /* no-op */ }
    }
}
