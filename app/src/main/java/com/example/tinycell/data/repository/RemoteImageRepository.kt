package com.example.tinycell.data.repository

/**
 * [PHASE 3.5]: Remote Image Repository Interface.
 * Contract for uploading local media to the cloud.
 */
interface RemoteImageRepository {
    /**
     * Uploads a local file to cloud storage.
     * @param localPath The absolute path or URI of the local image.
     * @return Result containing the public Download URL if successful.
     */
    suspend fun uploadImage(localPath: String): Result<String>
    
    /**
     * Uploads multiple images.
     */
    suspend fun uploadImages(localPaths: List<String>): Result<List<String>>
}
