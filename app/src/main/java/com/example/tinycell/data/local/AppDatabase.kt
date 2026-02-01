package com.example.tinycell.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tinycell.data.local.dao.*
import com.example.tinycell.data.local.entity.*

/**
 * TinyCell Room Database.
 * Updated to include the Formal Offer System.
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ListingEntity::class,
        FavouriteEntity::class,
        ChatMessageEntity::class,
        OfferEntity::class
    ],
    version = 4, // Incremented version for new OfferEntity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun listingDao(): ListingDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun offerDao(): OfferDao

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
