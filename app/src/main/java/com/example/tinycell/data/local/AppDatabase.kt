package com.example.tinycell.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tinycell.data.local.dao.*
import com.example.tinycell.data.local.entity.*

/**
 * TinyCell Room Database.
 * Updated to include the Review System.
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ListingEntity::class,
        FavouriteEntity::class,
        ChatMessageEntity::class,
        OfferEntity::class,
        ReviewEntity::class // Added ReviewEntity
    ],
    version = 5, // Incremented version for Review system
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun listingDao(): ListingDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun offerDao(): OfferDao
    abstract fun reviewDao(): ReviewDao // Added ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tinycell_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
