package com.example.tinycell.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

/**
 * [PHASE 6]: Refined Storage Logic.
 * Organizes images by userId for better management and security.
 */
class FirebaseStorageRepositoryImpl(
    private val context: Context,
    private val storage: FirebaseStorage
) : RemoteImageRepository {

    private val baseRef: StorageReference = storage.reference.child("listings")

    /**
     * [PHASE 6]: Upload with User Context.
     * We don't take UID as parameter here to keep the interface simple; 
     * instead, we assume the caller or Auth state is handled.
     * For now, it puts everything in 'listings/public'.
     */
    override suspend fun uploadImage(localPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val compressedData = compressImage(localPath) ?: return@withContext Result.failure(Exception("Compression failed"))

            val fileName = "${UUID.randomUUID()}.jpg"
            // Organized folder structure
            val imageRef = baseRef.child("public").child(fileName)

            imageRef.putBytes(compressedData).await()
            val downloadUrl = imageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun compressImage(path: String): ByteArray? {
        return try {
            val file = File(path)
            if (!file.exists()) return null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(options, 1080, 1080)
            options.inJustDecodeBounds = false
            
            val bitmap = BitmapFactory.decodeFile(path, options) ?: return null
            
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val result = outputStream.toByteArray()
            bitmap.recycle()
            result
        } catch (e: Exception) {
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
