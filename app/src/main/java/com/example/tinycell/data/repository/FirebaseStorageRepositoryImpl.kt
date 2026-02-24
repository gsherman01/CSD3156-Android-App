package com.example.tinycell.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

private const val TAG = "FirebaseStorage"

/**
 * [PHASE 6]: Refined Storage Logic.
 * Handles both File paths and Content URIs for reliable image uploads.
 */
class FirebaseStorageRepositoryImpl(
    private val context: Context,
    private val storage: FirebaseStorage
) : RemoteImageRepository {

    // Diagnostic log to verify the connection to the correct Firebase bucket
    init {
        Log.d(TAG, "Initializing Storage Repository. Target Bucket: ${storage.reference.bucket}")
    }

    override suspend fun uploadImage(localPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val compressedData = compressImage(localPath) 
                ?: return@withContext Result.failure(Exception("Compression failed for path: $localPath"))

            val fileName = "listing_${UUID.randomUUID()}.jpg"
            
            // Files are stored in the 'listings' folder in the root of your bucket
            val imageRef = storage.reference.child("listings").child(fileName)

            Log.d(TAG, "Starting upload to path: ${imageRef.path}")
            imageRef.putBytes(compressedData).await()
            
            val downloadUrl = imageRef.downloadUrl.await()
            Log.d(TAG, "Upload successful! Accessible at: $downloadUrl")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed for $localPath: ${e.message}")
            Result.failure(e)
        }
    }

    private fun compressImage(path: String): ByteArray? {
        return try {
            val uri = Uri.parse(path)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            options.inSampleSize = calculateInSampleSize(options, 1080, 1080)
            options.inJustDecodeBounds = false
            
            inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            if (bitmap == null) return null
            
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val result = outputStream.toByteArray()
            bitmap.recycle()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compress image at $path: ${e.message}")
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    override suspend fun uploadImages(localPaths: List<String>): Result<List<String>> = try {
        Log.d(TAG, "Processing batch upload for ${localPaths.size} images.")
        val urls = localPaths.map { path ->
            val result = uploadImage(path)
            if (result.isFailure) throw result.exceptionOrNull()!!
            result.getOrThrow()
        }
        Result.success(urls)
    } catch (e: Exception) {
        Log.e(TAG, "Batch upload interrupted: ${e.message}")
        Result.failure(e)
    }
}
