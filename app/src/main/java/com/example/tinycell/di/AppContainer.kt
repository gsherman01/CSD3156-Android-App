package com.example.tinycell.di

import android.content.Context
import androidx.room.Room
import com.example.tinycell.data.local.AppDatabase
import com.example.tinycell.data.repository.ListingRepository
import com.example.tinycell.data.repository.CameraRepository

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

}