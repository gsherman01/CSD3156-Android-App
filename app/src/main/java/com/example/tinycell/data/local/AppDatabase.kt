package com.example.tinycell.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tinycell.data.local.dao.CategoryDao
import com.example.tinycell.data.local.dao.ChatMessageDao
import com.example.tinycell.data.local.dao.FavouriteDao
import com.example.tinycell.data.local.dao.ListingDao
import com.example.tinycell.data.local.dao.UserDao
import com.example.tinycell.data.local.entity.CategoryEntity
import com.example.tinycell.data.local.entity.ChatMessageEntity
import com.example.tinycell.data.local.entity.FavouriteEntity
import com.example.tinycell.data.local.entity.ListingEntity
import com.example.tinycell.data.local.entity.UserEntity

/**
 * TinyCell Room Database.
 *
 * Contains all entities and provides DAO access for the marketplace app.
 * Uses singleton pattern to ensure only one database instance exists.
 *
 * Database schema includes:
 * - users: User accounts
 * - categories: Listing categories
 * - listings: Marketplace items for sale
 * - favourites: User's saved listings (many-to-many join table)
 * - chat_messages: Messages between users about listings
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ListingEntity::class,
        FavouriteEntity::class,
        ChatMessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO access methods
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun listingDao(): ListingDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        // Volatile ensures changes to INSTANCE are immediately visible to all threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get database instance (singleton pattern).
         *
         * Creates database if it doesn't exist, otherwise returns existing instance.
         * Thread-safe using synchronized block.
         *
         * @param context Application context
         * @return Database instance
         */
        fun getDatabase(context: Context): AppDatabase {
            // Return existing instance if available
            return INSTANCE ?: synchronized(this) {
                // Double-check inside synchronized block
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tinycell_database"
                )
                    // Uncomment for development to allow main thread queries (NOT recommended for production)
                    // .allowMainThreadQueries()

                    // Destroy and rebuild database on version conflicts (useful during development)
                    .fallbackToDestructiveMigration()

                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Clear database instance (useful for testing).
         * Call this before creating a new database in tests.
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
