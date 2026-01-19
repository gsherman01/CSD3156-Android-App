package com.example.tinycell.di

import android.content.Context
import androidx.room.Room
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.data.repository.CameraRepository
import com.example.tinycell.data.repository.RemoteListingRepository
import com.example.tinycell.data.repository.RemoteImageRepository
import com.example.tinycell.data.repository.FirestoreListingRepositoryImpl
import com.example.tinycell.data.repository.FirebaseStorageRepositoryImpl
import com.example.tinycell.data.remote.datasource.FirestoreListingDataSource
import com.example.tinycell.data.remote.datasource.FirestoreUserDataSource
import com.example.tinycell.data.remote.datasource.FirestoreChatDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [PHASE 3.5]: Dependency Injection Container.
 * Updated to include Firebase Storage and Remote Image Repository.
 */
class AppContainer(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    // --- DATA SOURCES ---

    private val listingDataSource: FirestoreListingDataSource by lazy {
        FirestoreListingDataSource(firestore)
    }

    // --- DATABASE ---

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "tinycell_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    // --- REPOSITORIES ---

    val remoteListingRepository: RemoteListingRepository by lazy {
        FirestoreListingRepositoryImpl(firestore)
    }

    val remoteImageRepository: RemoteImageRepository by lazy {
        FirebaseStorageRepositoryImpl(context.applicationContext, storage)
    }

    val listingRepository: ListingRepository by lazy {
        ListingRepository(
            database.listingDao(),
            remoteListingRepository,
            remoteImageRepository // Injected for image uploads
        )
    }

    val cameraRepository: CameraRepository by lazy {
        CameraRepository(context.applicationContext)
    }

    fun initializeData() {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            seedDatabase()
            performRemoteSync()
        }
    }

    suspend fun seedDatabase() {
        try {
            val dao = database.listingDao()
            val currentTime = System.currentTimeMillis()

            dao.insertUser(
                UserEntity(
                    id = "user_1",
                    name = "Default User",
                    email = "user1@example.com",
                    profilePicUrl = null,
                    createdAt = currentTime
                )
            )

            val categories = listOf("General", "Electronics", "Fashion", "Home", "Toys", "Books")
            categories.forEach { catName ->
                dao.insertCategory(
                    CategoryEntity(id = catName, name = catName, icon = null)
                )
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun performRemoteSync() {
        try {
            listingRepository.syncFromRemote()
        } catch (e: Exception) {
            // Log error
        }
    }
}
