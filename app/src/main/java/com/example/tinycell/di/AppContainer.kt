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

            val categories = listOf("General", "Electronics", "Fashion", "Home", "Toys", "Books")
            categories.forEach { catName ->
                lDao.insertCategory(CategoryEntity(id = catName, name = catName, icon = null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "seedDatabase: Failed", e)
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
