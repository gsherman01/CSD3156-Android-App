package com.example.tinycell.di

import android.content.Context
import androidx.room.Room
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.data.repository.CameraRepository
import com.example.tinycell.data.repository.RemoteListingRepository
import com.example.tinycell.data.repository.RemoteImageRepository
import com.example.tinycell.data.repository.AuthRepository
import com.example.tinycell.data.repository.FirestoreListingRepositoryImpl
import com.example.tinycell.data.repository.FirebaseStorageRepositoryImpl
import com.example.tinycell.data.repository.FirebaseAuthRepositoryImpl
import com.example.tinycell.data.remote.datasource.FirestoreListingDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [PHASE 5.5]: Dependency Injection Container with Simple Auth.
 */
class AppContainer(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // --- REPOSITORIES ---

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepositoryImpl(auth)
    }

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
            remoteImageRepository,
            authRepository // Injected for UID management
        )
    }

    // --- DATABASE ---

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "tinycell_db"
        ).fallbackToDestructiveMigration().build()
    }

    val cameraRepository: CameraRepository by lazy {
        CameraRepository(context.applicationContext)
    }

    /**
     * [PHASE 5.5]: Initialize App with Anonymous Auth.
     */
    fun initializeData() {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            // 1. Sign in anonymously to satisfy security rules
            authRepository.signInAnonymously()
            
            // 2. Seed and Sync
            seedDatabase()
            performRemoteSync()
        }
    }

    suspend fun seedDatabase() {
        try {
            val dao = database.listingDao()
            val userId = authRepository.getCurrentUserId() ?: "user_1"
            val currentTime = System.currentTimeMillis()

            dao.insertUser(
                UserEntity(
                    id = userId,
                    name = "User",
                    email = "user@example.com",
                    profilePicUrl = null,
                    createdAt = currentTime
                )
            )

            val categories = listOf("General", "Electronics", "Fashion", "Home", "Toys", "Books")
            categories.forEach { catName ->
                dao.insertCategory(CategoryEntity(id = catName, name = catName, icon = null))
            }
        } catch (e: Exception) {}
    }

    private suspend fun performRemoteSync() {
        try {
            listingRepository.syncFromRemote()
        } catch (e: Exception) {}
    }
}
