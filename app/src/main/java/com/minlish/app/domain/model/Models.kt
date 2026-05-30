package com.minlish.app.domain.model

import java.util.Date

// --- ENUMERATIONS ---

enum class InitialLevel {
    A1, A2, B1, B2, C1, C2
}

enum class LearningGoal {
    IELTS, TOEIC, VSTEP, TRAVEL, BUSINESS, COMMUNICATION
}

enum class EaseFactor {
    Easy, Good, Hard, Again
}

// --- DATA CLASSES ---

data class User(
    val id: Int,
    val name: String,
    val totalWords: Int = 0,
    val rememberedWords: Int = 0
)

data class Account(
    val userId: Int,
    val email: String,
    val password: String
)

data class UserProfile(
    val userId: Int,
    val learningGoals: List<LearningGoal>,
    val initialLevel: InitialLevel
)

data class UserSetting(
    val userId: Int,
    val dailyNewWordGoal: Int,
    val dailyReviewGoal: Int
)

data class Deck(
    val id: Int = 0,
    val deckName: String,
    val description: String,
    val tags: List<LearningGoal>,
    val createId: Int?,
    val isPublic: Boolean,
    val totalWords: Int = 0,
    val learned: Int = 0,
    val streak: Int = 0
)

data class Vocabulary(
    val id: Int = 0,
    val word: String,
    val pronunciation: String,
    val meaning: String,
    val descriptionEnglish: String, // Latest prompt: String
    val example: List<String>, // Latest prompt: List<String>
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val relatedWords: List<Vocabulary> = emptyList(),
    val note: String,
    // Optional UI fields
    val imageUrl: String = "",
    val wordType: String = ""
)

data class UserVocabularyState(
    val userId: Int = 1,
    val vocabularyId: Int,
    val interval: Double,
    val repetition: Int,
    val easeFactor: EaseFactor,
    val nextReview: Date
)

data class ReviewHistory(
    val id: Int = 0,
    val userId: Int = 1,
    val vocabularyId: Int,
    val learningTime: Date
)

data class Notification(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String,
    val timestamp: Date = Date(),
    val isRead: Boolean
)
