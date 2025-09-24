package com.example.beautyparlor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.beautyparlor.dao.ServiceDao
import com.example.beautyparlor.entities.Service
import com.example.beautyparlor.entities.ServiceSubItem

@Database(entities = [Service::class, ServiceSubItem::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serviceDao(): ServiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beauty_parlor_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabasePrepopulateCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}