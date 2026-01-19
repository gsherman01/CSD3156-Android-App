package com.example.tinycell.di

import android.content.Context
import androidx.room.Room
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.data.repository.CameraRepository

//Firestore integration
import com.example.tinycell.data.repository.RemoteListingRepository
import com.example.tinycell.data.repository.FirestoreListingRepositoryImpl

import com.example.tinycell.data.remote.datasource.FirestoreListingDataSource
import com.example.tinycell.data.remote.datasource.FirestoreUserDataSource
import com.example.tinycell.data.remote.datasource.FirestoreChatDataSource
import com.google.firebase.firestore.FirebaseFirestore


import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.UserEntity
import kotlinx.coroutines.launch

/**
 * [LEARNING_POINT: SERVICE LOCATOR PATTERN]
 * AppContainer acts as a central hub for all dependencies. By using 'lazy',
 * we ensure that heavy objects like the Room Database or Firestore are
 * only created when they are actually needed by a screen.
 * [PHASE 2]: Dependency Injection Container.
 * Updated to include Firestore Data Sources.
 */
class AppContainer(private val context: Context) {

    // Initialize Firestore Instance
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // --- DATA SOURCES ---

    private val listingDataSource: FirestoreListingDataSource by lazy {
        FirestoreListingDataSource(firestore)
    }

    private val userDataSource: FirestoreUserDataSource by lazy {
        FirestoreUserDataSource(firestore)
    }

    private val chatDataSource: FirestoreChatDataSource by lazy {
        FirestoreChatDataSource(firestore)
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

    /**
     * [TODO_NETWORKING_INTEGRATION]:
     * This handles all Cloud Firestore interactions.
     */
    val remoteListingRepository: RemoteListingRepository by lazy {
        FirestoreListingRepositoryImpl(firestore)
    }

    val listingRepository: ListingRepository by lazy {
        ListingRepository(database.listingDao(), remoteListingRepository)
    }

    val cameraRepository: CameraRepository by lazy {
        CameraRepository(context.applicationContext)
    }

    // Seeding logic remains for local development
    fun seedDatabase() {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
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
                        CategoryEntity(
                            id = catName,
                            name = catName,
                            icon = null
                        )
                    )
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
}
