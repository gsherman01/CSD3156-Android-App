package com.example.tinycell.di

import android.content.Context
import android.util.Log
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.UserEntity
import com.example.tinycell.data.remote.datasource.FirestoreChatDataSource
import com.example.tinycell.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "AppContainer"

/**
 * Dependency Injection Container.
 */
class AppContainer(private val context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        Log.d(TAG, "Initializing AppContainer...")
    }

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepositoryImpl(auth)
    }

    val remoteListingRepository: RemoteListingRepository by lazy {
        FirestoreListingRepositoryImpl(firestore)
    }

    val remoteImageRepository: RemoteImageRepository by lazy {
        FirebaseStorageRepositoryImpl(context.applicationContext, storage)
    }

    val remoteNotificationRepository: RemoteNotificationRepository by lazy {
        FirestoreNotificationRepositoryImpl(firestore)
    }

    val listingRepository: ListingRepository by lazy {
        ListingRepository(
            listingDao = database.listingDao(),
            userDao = database.userDao(),
            offerDao = database.offerDao(),
            reviewDao = database.reviewDao(),
            notificationDao = database.notificationDao(),
            favouriteDao = database.favouriteDao(),
            remoteRepo = remoteListingRepository,
            remoteNotificationRepo = remoteNotificationRepository,
            imageRepo = remoteImageRepository,
            authRepo = authRepository
        )
    }

    private val firestoreChatDataSource: FirestoreChatDataSource by lazy {
        FirestoreChatDataSource(firestore)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(
            firestoreChatDataSource = firestoreChatDataSource,
            chatMessageDao = database.chatMessageDao(),
            userDao = database.userDao(),
            listingDao = database.listingDao(),
            remoteListingRepository = remoteListingRepository,
            remoteImageRepository = remoteImageRepository
        )
    }

    val favouriteRepository: FavouriteRepository by lazy {
        FavouriteRepository(database.favouriteDao())
    }

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    /**
     * Resilient startup initialization.
     */
    fun initializeData() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "initializeData: Authenticating...")
                authRepository.signInAnonymously()
                
                Log.d(TAG, "initializeData: Seeding DB...")
                seedDatabase()
                
                // [RESILIENCE]: Wrap sync bridges in individual try-catches
                launch {
                    try {
                        Log.d(TAG, "initializeData: Starting Listing Sync...")
                        listingRepository.startRealTimeSync(this)
                    } catch (e: Exception) { Log.e(TAG, "Listing Sync failed", e) }
                }

                launch {
                    try {
                        Log.d(TAG, "initializeData: Starting Notification Sync...")
                        listingRepository.startNotificationSync(this)
                    } catch (e: Exception) { Log.e(TAG, "Notification Sync failed", e) }
                }
                
                try {
                    Log.d(TAG, "initializeData: Performing Remote Fetch...")
                    performRemoteSync()
                } catch (e: Exception) { Log.e(TAG, "Remote Fetch failed", e) }

            } catch (e: Exception) {
                Log.e(TAG, "Critical Initialization failed", e)
            }
        }
    }

    suspend fun seedDatabase() {
        try {
            val uDao = database.userDao()
            val lDao = database.listingDao()
            val userId = authRepository.getCurrentUserId() ?: "anonymous"
            val currentName = authRepository.getCurrentUserName() ?: "Anonymous"
            
            // Ensure current user record exists locally
            uDao.insert(UserEntity(id = userId, name = currentName, email = "", createdAt = System.currentTimeMillis()))

            val categories = listOf(
                CategoryEntity(id = "General", name = "General", icon = "📦"),
                CategoryEntity(id = "Electronics", name = "Electronics", icon = "📱"),
                CategoryEntity(id = "Fashion", name = "Fashion", icon = "👗"),
                CategoryEntity(id = "Home", name = "Home", icon = "🏠"),
                CategoryEntity(id = "Toys", name = "Toys", icon = "🧸"),
                CategoryEntity(id = "Books", name = "Books", icon = "📚")
            )
            categories.forEach { lDao.insertCategory(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Seeding failed", e)
        }
    }

    private suspend fun performRemoteSync() {
        listingRepository.syncFromRemote()
    }
}
