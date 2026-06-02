package com.minlish.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.minlish.app.data.source.local.DeckProgressEntity
import com.minlish.app.data.source.local.ProgressDao

@Database(
    entities = [
        DeckProgressEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class BunnyDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: BunnyDatabase? = null

        fun getDatabase(context: Context): BunnyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BunnyDatabase::class.java,
                    "bunny_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
