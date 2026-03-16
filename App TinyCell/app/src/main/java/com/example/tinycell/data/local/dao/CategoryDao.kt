package com.example.tinycell.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tinycell.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Category table.
 *
 * Provides operations for managing marketplace categories.
 * Categories help users browse and filter listings.
 */
@Dao
interface CategoryDao {

    /**
     * Insert a new category. Replaces if category with same ID exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    /**
     * Insert multiple categories at once (for initial setup).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    /**
     * Get all categories as an observable Flow, ordered alphabetically.
     */
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * Get a specific category by ID.
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    /**
     * Delete all categories (useful for testing/reset).
     */
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
