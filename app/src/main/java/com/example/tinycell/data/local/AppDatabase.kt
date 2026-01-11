package com.example.tinycell.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
//import androidx.room.RoomDatabaseimport com.example.tinycell.data.local.dao.AppDao
import com.example.tinycell.data.local.dao.AppDao
import com.example.tinycell.data.local.entity.AppEntity

@Database(entities = [AppEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
