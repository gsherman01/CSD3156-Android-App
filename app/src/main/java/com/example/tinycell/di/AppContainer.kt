package com.example.tinycell.di

import android.content.Context
import androidx.room.Room
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.data.repository.CameraRepository

import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.UserEntity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * [TODO_DI_INTEGRATION]
 * Container for dependency injection.
 * This class provides single instances of the Database and Repositories.
 */
class AppContainer(private val context: Context) {

    // Lazy initialization of the database
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "tinycell_db"
        ).fallbackToDestructiveMigration() // [TODO_DB_MIGRATION]: Implement proper migrations later
            .build()
    }

    // Lazy initialization of the repository
    val listingRepository: ListingRepository by lazy {
        ListingRepository(database.listingDao())
    }


    /**
     * [TODO_HARDWARE_INTEGRATION]:
     * - ACTION: Hardware lead to ensure the Context passed here is the ApplicationContext
     *   to prevent memory leaks when the camera is initialized.
     */
    val cameraRepository: CameraRepository by lazy {
        CameraRepository(context.applicationContext)
    }


    /**
     * [TODO_DATABASE_INTEGRATION]:
     * Seed essential data to prevent Foreign Key violations (Error 787).
     * This ensures "user_1" and the categories exist before a listing is created.
     */
    fun seedDatabase() {
        MainScope().launch {
            val dao = database.listingDao()

            // Seed Default User
            dao.insertUser(
                UserEntity(
                    id = "user_1",
                    name = "Default User",
                    email = "user1@example.com",
                    profilePicUrl = null,
                    createdAt = System.currentTimeMillis() //  this looks problematic
                )
            )

            // Seed Categories matching CreateListingViewModel choices
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
        }
    }//end of seedDatabase function
}//end of class