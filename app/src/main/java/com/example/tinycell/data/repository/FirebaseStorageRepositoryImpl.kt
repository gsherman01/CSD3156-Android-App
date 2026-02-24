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

    private val baseRef: StorageReference = storage.reference.child("listings")

    override suspend fun uploadImage(localPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val compressedData = compressImage(localPath) 
                ?: return@withContext Result.failure(Exception("Compression failed for path: $localPath"))

            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = baseRef.child("public").child(fileName)

            imageRef.putBytes(compressedData).await()
            val downloadUrl = imageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}")
            Result.failure(e)
        }
    }

    private fun compressImage(path: String): ByteArray? {
        return try {
            val uri = Uri.parse(path)
            
            // [FIX]: Use ContentResolver to handle content:// URIs from Camera/Gallery
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
            Log.e(TAG, "Compression error: ${e.message}")
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
