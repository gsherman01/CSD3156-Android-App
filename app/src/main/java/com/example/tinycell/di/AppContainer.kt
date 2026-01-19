package com.example.tinycell.di

import android.content.Context
import androidx.room.Room
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.data.repository.CameraRepository
import com.example.tinycell.data.repository.RemoteListingRepository
import com.example.tinycell.data.repository.FirestoreListingRepositoryImpl
import com.example.tinycell.data.remote.datasource.FirestoreListingDataSource
import com.example.tinycell.data.remote.datasource.FirestoreUserDataSource
import com.example.tinycell.data.remote.datasource.FirestoreChatDataSource
import com.google.firebase.firestore.FirebaseFirestore


import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [PHASE 3]: Dependency Injection Container.
 * Updated to handle Startup Sync for the Local-First strategy.
 */
class AppContainer(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
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

    val listingRepository: ListingRepository by lazy {
        ListingRepository(database.listingDao(), remoteListingRepository)
    }

    val cameraRepository: CameraRepository by lazy {
        CameraRepository(context.applicationContext)
    }

    /**
     * [LEARNING_POINT: INITIALIZATION STRATEGY]
     * We combine local seeding (for primary keys/constraints) with remote sync.
     */
    fun initializeData() {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            // 1. Seed local data first (Required for Foreign Key integrity)
            seedDatabase()
            
            // 2. Sync from Firestore (Hydrate Room with real data)
            performRemoteSync()
        }
    }

    private suspend fun seedDatabase() {
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
            // Log seeding error
        }
    }

    /**
     * [PHASE 3]: Background Remote Sync
     */
    private suspend fun performRemoteSync() {
        try {
            listingRepository.syncFromRemote()
        } catch (e: Exception) {
            // [TODO_ERROR_HANDLING]: Implement a notification or retry snackbar in UI
        }
    }
}
