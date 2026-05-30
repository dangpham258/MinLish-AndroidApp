package com.minlish.app.domain.repository

import com.minlish.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface BunnyRepository {
    // Decks
    fun getDecks(): Flow<List<Deck>>
    suspend fun insertDeck(deck: Deck): Long
    suspend fun getDeckById(deckId: Int): Deck?
    fun getDeckByIdFlow(deckId: Int): Flow<Deck?>

    // Vocabulary
    fun getVocabularyByDeck(deckId: Int): Flow<List<Vocabulary>>
    fun getVocabularyById(id: Int): Flow<Vocabulary?>
    suspend fun insertVocabulary(vocabulary: Vocabulary): Long
    suspend fun getVocabularyByIdDirect(id: Int): Vocabulary?

    // Spaced Repetition States (SRS)
    fun getActiveUserVocabularyStates(): Flow<List<UserVocabularyState>>
    fun getVocabularyDueForReview(currentTime: Long, deckId: Int? = null): Flow<List<Vocabulary>>
    fun getNewVocabularyForLearning(deckId: Int? = null, limit: Int = 10): Flow<List<Vocabulary>>
    suspend fun getUserVocabularyState(vocabularyId: Int): UserVocabularyState?
    suspend fun saveUserVocabularyState(state: UserVocabularyState, previousEaseFactor: EaseFactor?)

    // User Profile / Settings
    fun getUserName(): Flow<String>
    suspend fun updateUserName(name: String)
    fun getLearningGoal(): Flow<LearningGoal>
    suspend fun updateLearningGoal(goal: LearningGoal)
    fun getInitialLevel(): Flow<InitialLevel>
    suspend fun updateInitialLevel(level: InitialLevel)
    fun getUser(userId: Int): Flow<User?>

    // Stats
    fun getLearnedWordsCount(): Flow<Int>
    fun getStreakDaysCount(): Flow<Int>
    suspend fun incrementStreak()

    // Review History
    fun getReviewHistory(vocabularyId: Int): Flow<List<ReviewHistory>>
    suspend fun addReviewHistory(history: ReviewHistory)

    // Notifications
    fun getNotifications(): Flow<List<Notification>>
    suspend fun addNotification(notification: Notification)

    // Database Initialization
    suspend fun prepopulateInitialData()
}
