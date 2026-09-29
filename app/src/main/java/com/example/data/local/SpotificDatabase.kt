package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteTrackEntity::class, DownloadedTrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SpotificDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadedDao(): DownloadedDao

    companion object {
        @Volatile
        private var INSTANCE: SpotificDatabase? = null

        fun getDatabase(context: Context): SpotificDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpotificDatabase::class.java,
                    "spotific_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
