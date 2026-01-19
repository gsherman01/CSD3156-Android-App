package com.example.tinycell.data.repository

import android.content.Context

//bitmap to compress image
import android.graphics.Bitmap
import android.graphics.BitmapFactory

//firebase storage stuff
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

/**
 * [PHASE 3.5]: Firebase Storage Implementation with Image Optimization.
 * Handles compressing and uploading physical files to the cloud.
 */
class FirebaseStorageRepositoryImpl(
    private val context: Context,
    private val storage: FirebaseStorage
) : RemoteImageRepository {

    private val storageRef: StorageReference = storage.reference.child("listings")

    override suspend fun uploadImage(localPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Compress the image before uploading
            val compressedData = compressImage(localPath) ?: return@withContext Result.failure(Exception("Compression failed"))

            // 2. Generate a unique filename
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(fileName)

            // 3. Upload bytes to Firebase Storage
            imageRef.putBytes(compressedData).await()

            // 4. Get the public download URL
            val downloadUrl = imageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * [LEARNING_POINT: IMAGE OPTIMIZATION]
     * High-res camera photos are often 5MB+. 
     * This function resizes and compresses them to ~200KB for faster cloud syncing.
     */
    private fun compressImage(path: String): ByteArray? {
        return try {
            val file = File(path)
            if (!file.exists()) return null

            // Load bitmap with scaling options to save memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            // Target dimensions (e.g., max 1080p)
            val reqWidth = 1080
            val reqHeight = 1080
            
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            
            val bitmap = BitmapFactory.decodeFile(path, options) ?: return null
            
            // Compress to JPEG with 70% quality
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val result = outputStream.toByteArray()
            bitmap.recycle() // Free memory
            result
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height: Int = options.outHeight
        val width: Int = options.outWidth
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
        val urls = localPaths.map { path ->
            val result = uploadImage(path)
            if (result.isFailure) throw result.exceptionOrNull()!!
            result.getOrThrow()
        }
        Result.success(urls)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
