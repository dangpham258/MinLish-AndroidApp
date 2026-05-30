package com.minlish.app.data.source.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalWords: Int = 0,
    val rememberedWords: Int = 0
)

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AccountEntity(
    @PrimaryKey val userId: Int,
    val email: String,
    val password: String
)

@Entity(
    tableName = "user_profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserProfileEntity(
    @PrimaryKey val userId: Int,
    val learningGoalsJson: String,
    val initialLevel: String
)

@Entity(
    tableName = "user_settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserSettingEntity(
    @PrimaryKey val userId: Int,
    val dailyNewWordGoal: Int,
    val dailyReviewGoal: Int
)

@Entity(
    tableName = "decks",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckName: String,
    val description: String,
    val tagsJson: String,
    val createId: Int?,
    val isPublic: Boolean
)

@Entity(tableName = "vocabularies")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val pronunciation: String,
    val meaning: String,
    val descriptionEnglish: String, // String according to latest prompt
    val exampleJson: String, // List<String> according to latest prompt
    val synonymsJson: String = "[]",
    val antonymsJson: String = "[]",
    val relatedWordsJson: String,
    val note: String,
    val imageUrl: String = "",
    val wordType: String = ""
)

@Entity(
    tableName = "deck_vocabularies",
    primaryKeys = ["deckId", "vocabularyId"],
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocabularyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DeckVocabularyEntity(
    val deckId: Int,
    val vocabularyId: Int
)

@Entity(
    tableName = "user_vocabulary_states",
    primaryKeys = ["userId", "vocabularyId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocabularyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserVocabularyStateEntity(
    val userId: Int,
    val vocabularyId: Int,
    val interval: Double,
    val repetition: Int,
    val easeFactor: String,
    val nextReview: Long
)

@Entity(
    tableName = "review_histories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocabularyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReviewHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val vocabularyId: Int,
    val learningTime: Long
)

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String = "",
    val content: String,
    val timestamp: Long,
    val isRead: Boolean
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val userId: Int,
    val learnedWordsCount: Int,
    val streakDays: Int,
    val lastActiveTimestamp: Long
)
