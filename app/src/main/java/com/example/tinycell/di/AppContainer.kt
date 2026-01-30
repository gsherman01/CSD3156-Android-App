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
                Log.d(TAG, "DEBUG: initializeData: Starting background tasks")
                authRepository.signInAnonymously()
                Log.d(TAG, "DEBUG: initializeData: Auth signed in as ${authRepository.getCurrentUserId()}")
                
                seedDatabase()
                Log.d(TAG, "DEBUG: initializeData: Database seeded")
                
                performRemoteSync()
                Log.d(TAG, "DEBUG: initializeData: Remote sync triggered")
            } catch (e: Exception) {
                Log.e(TAG, "DEBUG: initializeData: Failed", e)
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

            Log.d(TAG, "DEBUG: Seeding user: $userId ($currentName)")
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

            Log.d(TAG, "DEBUG: Database seeded successfully with ${categories.size} categories")
        } catch (e: Exception) {
            Log.e(TAG, "DEBUG: seedDatabase: Failed", e)
        }
    }

    /**
     * Generate sample listings for debugging.
     * Updated: Now uses the Repository to ensure samples are sent to Firestore.
     */
    suspend fun generateSampleListings(count: Int = 5) {
        try {
            val userId = authRepository.getCurrentUserId() ?: "anonymous"
            val userName = authRepository.getCurrentUserName() ?: "Anonymous"
            val currentTime = System.currentTimeMillis()

            Log.d(TAG, "DEBUG: Generating $count samples for User: $userId ($userName)")

            val sampleData = listOf(
                Triple("iPhone 14 Pro", 899.99, "Electronics"),
                Triple("MacBook Pro M2", 1499.99, "Electronics"),
                Triple("Sony WH-1000XM5", 349.99, "Electronics"),
                Triple("Winter Jacket", 79.99, "Fashion"),
                Triple("Coffee Table", 199.99, "Home"),
                Triple("LEGO Star Wars Set", 129.99, "Toys")
            )

            repeat(count) { index ->
                val sample = sampleData[index % sampleData.size]
                val listingEntity = com.example.tinycell.data.local.entity.ListingEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    title = sample.first,
                    description = "Sample Listing #$index generated for validation",
                    price = sample.second,
                    userId = userId,
                    sellerName = userName,
                    categoryId = sample.third,
                    location = "Cloud Sync Test",
                    imageUrls = "", // Empty for samples
                    createdAt = currentTime - (index * 60000),
                    isSold = false
                )
                
                Log.d(TAG, "DEBUG: Pushing sample #$index to Repository (Dual-Write)...")
                // Use the repository instead of the DAO to trigger the Firestore upload
                listingRepository.createListing(listingEntity)
            }

            Log.d(TAG, "DEBUG: Successfully triggered generation of $count sample listings.")
            
        } catch (e: Exception) {
            Log.e(TAG, "DEBUG: generateSampleListings: Failed", e)
        }
    }

    private suspend fun performRemoteSync() {
        try {
            Log.d(TAG, "DEBUG: Starting remote sync to fetch Cloud data...")
            listingRepository.syncFromRemote()
            Log.d(TAG, "DEBUG: Remote sync completed.")
        } catch (e: Exception) {
            Log.e(TAG, "DEBUG: performRemoteSync: Failed", e)
        }
    }
}
