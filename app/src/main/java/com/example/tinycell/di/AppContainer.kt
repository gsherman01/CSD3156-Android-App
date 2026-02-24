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
            remoteImageRepository = remoteImageRepository,
            authRepository = authRepository
        )
    }

    val favouriteRepository: FavouriteRepository by lazy {
        FavouriteRepository(database.favouriteDao())
    }

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    fun initializeData() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                authRepository.signInAnonymously()
                seedDatabase()
                
                listingRepository.startRealTimeSync(applicationScope)
                listingRepository.startNotificationSync(applicationScope)
                
                performRemoteSync()
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
            }
        }
    }

    suspend fun seedDatabase() {
        try {
            val uDao = database.userDao()
            val lDao = database.listingDao()
            val userId = authRepository.getCurrentUserId() ?: "anonymous"
            val currentName = authRepository.getCurrentUserName() ?: "Anonymous"
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

    /**
     * [RESTORED]: Generate sample listings for debugging.
     */
    suspend fun generateSampleListings(count: Int = 5) {
        try {
            val uDao = database.userDao()
            val lDao = database.listingDao()
            val userId = authRepository.getCurrentUserId() ?: "anonymous"
            val userName = authRepository.getCurrentUserName() ?: "Anonymous"
            val currentTime = System.currentTimeMillis()

            if (uDao.getUserById(userId) == null) {
                uDao.insert(UserEntity(id = userId, name = userName, email = "", createdAt = currentTime))
            }

            val sampleData = listOf(
                Triple("iPhone 14 Pro", 899.99, "Electronics"),
                Triple("MacBook Pro M2", 1499.99, "Electronics"),
                Triple("Sony WH-1000XM5", 349.99, "Electronics"),
                Triple("Winter Jacket", 79.99, "Fashion"),
                Triple("Coffee Table", 199.99, "Home"),
                Triple("LEGO Set", 129.99, "Toys")
            )

            repeat(count) { index ->
                val sample = sampleData[index % sampleData.size]
                val listing = com.example.tinycell.data.local.entity.ListingEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    title = sample.first,
                    description = "Sample Listing #$index",
                    price = sample.second,
                    userId = userId,
                    sellerName = userName,
                    categoryId = sample.third,
                    location = "Local Pickup",
                    imageUrls = "",
                    createdAt = currentTime - (index * 60000),
                    isSold = false,
                    status = "AVAILABLE"
                )
                lDao.insert(listing)
            }
            Log.d(TAG, "Generated $count sample listings")
        } catch (e: Exception) {
            Log.e(TAG, "generateSampleListings: Failed", e)
        }
    }

    private suspend fun performRemoteSync() {
        try {
            listingRepository.syncFromRemote()
        } catch (e: Exception) {
            Log.e(TAG, "Remote sync failed", e)
        }
    }
}
