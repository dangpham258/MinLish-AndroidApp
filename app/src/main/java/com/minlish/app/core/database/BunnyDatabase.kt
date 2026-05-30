package com.minlish.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.minlish.app.data.source.local.*

@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        UserProfileEntity::class,
        UserSettingEntity::class,
        DeckEntity::class,
        VocabularyEntity::class,
        DeckVocabularyEntity::class,
        UserVocabularyStateEntity::class,
        ReviewHistoryEntity::class,
        NotificationEntity::class,
        UserStatsEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class BunnyDatabase : RoomDatabase() {
    abstract fun bunnyDao(): BunnyDao

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
