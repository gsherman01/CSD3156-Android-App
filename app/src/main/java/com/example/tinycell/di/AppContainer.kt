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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "AppContainer"

/**
 * Dependency Injection Container.
 */
class AppContainer(private val context: Context) {

    init {
        Log.d(TAG, "Initializing AppContainer...")
    }

    private val firestore: FirebaseFirestore by lazy {
        Log.d(TAG, "Creating Firestore instance")
        FirebaseFirestore.getInstance()
    }
    
    private val storage: FirebaseStorage by lazy {
        Log.d(TAG, "Creating Firebase Storage instance")
        FirebaseStorage.getInstance()
    }
    
    private val auth: FirebaseAuth by lazy {
        Log.d(TAG, "Creating Firebase Auth instance")
        FirebaseAuth.getInstance()
    }

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
        Log.d(TAG, "Creating ListingRepository")
        ListingRepository(
            database.listingDao(),
            database.userDao(),
            remoteListingRepository,
            remoteImageRepository,
            authRepository
        )
    }

    private val firestoreChatDataSource: FirestoreChatDataSource by lazy {
        Log.d(TAG, "Creating FirestoreChatDataSource")
        FirestoreChatDataSource(firestore)
    }

    val chatRepository: ChatRepository by lazy {
        Log.d(TAG, "Creating ChatRepository")
        ChatRepositoryImpl(
            firestoreChatDataSource,
            database.chatMessageDao()
        )
    }

    private val database: AppDatabase by lazy {
        Log.d(TAG, "Initializing Room Database")
        AppDatabase.getDatabase(context)
    }

    fun initializeData() {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "initializeData: Starting background tasks")
                authRepository.signInAnonymously()
                Log.d(TAG, "initializeData: Auth signed in")
                
                seedDatabase()
                Log.d(TAG, "initializeData: Database seeded")
                
                performRemoteSync()
                Log.d(TAG, "initializeData: Remote sync triggered")
            } catch (e: Exception) {
                Log.e(TAG, "initializeData: Failed", e)
            }
        }
    }

    suspend fun seedDatabase() {
        try {
            val uDao = database.userDao()
            val lDao = database.listingDao()
            val userId = authRepository.getCurrentUserId() ?: "anonymous"
            val currentName = authRepository.getCurrentUserName() ?: "Anonymous"
            val currentTime = System.currentTimeMillis()

            Log.d(TAG, "Seeding user: $userId ($currentName)")
            uDao.insert(UserEntity(id = userId, name = currentName, email = "", createdAt = currentTime))

            // Seed Categories with Icons
            val categories = listOf(
                CategoryEntity(id = "General", name = "General", icon = "📦"),
                CategoryEntity(id = "Electronics", name = "Electronics", icon = "📱"),
                CategoryEntity(id = "Fashion", name = "Fashion", icon = "👗"),
                CategoryEntity(id = "Home", name = "Home", icon = "🏠"),
                CategoryEntity(id = "Toys", name = "Toys", icon = "🧸"),
                CategoryEntity(id = "Books", name = "Books", icon = "📚")
            )
            categories.forEach { category ->
                lDao.insertCategory(category)
            }

            Log.d(TAG, "Database seeded successfully with ${categories.size} categories")
        } catch (e: Exception) {
            Log.e(TAG, "seedDatabase: Failed", e)
        }
    }

    /**
     * Generate sample listings for debugging.
     * Can be called from the Admin Debug Panel.
     */
    suspend fun generateSampleListings(count: Int = 5) {
        try {
            val uDao = database.userDao()
            val lDao = database.listingDao()
            val userId = authRepository.getCurrentUserId() ?: "anonymous"
            val userName = authRepository.getCurrentUserName() ?: "Anonymous"
            val currentTime = System.currentTimeMillis()

            // Ensure the current user exists in the database to avoid FK constraint errors
            if (uDao.getUserById(userId) == null) {
                Log.d(TAG, "Creating user entry for: $userId ($userName)")
                uDao.insert(UserEntity(
                    id = userId,
                    name = userName,
                    email = "",
                    createdAt = currentTime
                ))
            }

            val categories = listOf("General", "Electronics", "Fashion", "Home", "Toys", "Books")
            val sampleData = listOf(
                Triple("iPhone 14 Pro", 899.99, "Electronics"),
                Triple("MacBook Pro M2", 1499.99, "Electronics"),
                Triple("Sony WH-1000XM5", 349.99, "Electronics"),
                Triple("iPad Air", 599.99, "Electronics"),
                Triple("Winter Jacket", 79.99, "Fashion"),
                Triple("Running Shoes", 89.99, "Fashion"),
                Triple("Designer Sunglasses", 149.99, "Fashion"),
                Triple("Coffee Table", 199.99, "Home"),
                Triple("Office Chair", 249.99, "Home"),
                Triple("Table Lamp", 45.99, "Home"),
                Triple("LEGO Star Wars Set", 129.99, "Toys"),
                Triple("Board Game Collection", 59.99, "Toys"),
                Triple("Kindle Paperwhite", 139.99, "Books"),
                Triple("Cookbook Set", 34.99, "Books"),
                Triple("Vintage Camera", 299.99, "General"),
                Triple("Bicycle", 349.99, "General"),
                Triple("Gaming Console", 499.99, "Electronics"),
                Triple("Smart Watch", 279.99, "Electronics"),
                Triple("Leather Bag", 119.99, "Fashion"),
                Triple("Plant Pot Set", 29.99, "Home")
            )

            val descriptions = listOf(
                "Gently used, excellent condition",
                "Like new, barely used",
                "Great condition, some minor wear",
                "Brand new, still in box",
                "Well maintained, works perfectly",
                "Lightly used, no defects",
                "Perfect for daily use",
                "Excellent quality, looks great",
                "Barely used, original packaging",
                "Good as new, no issues"
            )

            repeat(count) { index ->
                val sample = sampleData[index % sampleData.size]
                val listing = com.example.tinycell.data.local.entity.ListingEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    title = sample.first,
                    description = descriptions[index % descriptions.size],
                    price = sample.second,
                    userId = userId,
                    sellerName = userName,
                    categoryId = sample.third,
                    location = "Local Pickup",
                    imageUrls = "",
                    createdAt = currentTime - (index * 60000), // Stagger timestamps
                    isSold = false
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
            Log.d(TAG, "Starting remote sync")
            listingRepository.syncFromRemote()
            Log.d(TAG, "Remote sync completed")
        } catch (e: Exception) {
            Log.e(TAG, "performRemoteSync: Failed", e)
        }
    }
}
