package com.minlish.app.domain.repository

import com.minlish.app.domain.model.*
import com.minlish.app.domain.model.enumration.*
import kotlinx.coroutines.flow.Flow

interface BunnyRepository {
    // Decks
    fun getDecks(): Flow<List<Deck>>
    suspend fun insertDeck(deck: Deck): Long
    suspend fun deleteDeck(deckId: String)
    suspend fun getDeckById(deckId: String): Deck?
    fun getDeckByIdFlow(deckId: String): Flow<Deck?>

    // Vocabulary
    fun getVocabularyByDeck(deckId: String, vocabularyIds: List<String>): Flow<List<Vocabulary>>
    fun getVocabularyById(id: String): Flow<Vocabulary?>
    suspend fun insertVocabulary(vocabulary: Vocabulary): Long
    suspend fun deleteVocabulary(deckId: String, vocabId: String)
    suspend fun getVocabularyByIdDirect(id: String): Vocabulary?

    // Spaced Repetition States (SRS)
    fun getActiveUserVocabularyStates(): Flow<List<UserVocabularyState>>
    fun getVocabularyDueForReview(currentTime: Long, deckId: String? = null): Flow<List<Vocabulary>>
    fun getNewVocabularyForLearning(deckId: String? = null, limit: Int = 10): Flow<List<Vocabulary>>
    suspend fun getUserVocabularyState(vocabularyId: String): UserVocabularyState?
    suspend fun saveUserVocabularyState(state: UserVocabularyState, rating: EaseFactor?)

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
    suspend fun notifyNewWordLearned(vocabularyId: String)

    // Review History
    fun getReviewHistory(vocabularyId: String): Flow<List<ReviewHistory>>
    suspend fun addReviewHistory(history: ReviewHistory)

    // Notifications
    fun getNotifications(): Flow<List<Notification>>
    suspend fun addNotification(notification: Notification)

    // Database Initialization
    suspend fun prepopulateInitialData()
    suspend fun saveProgress(deckId: String, learnedCount: Int, totalCount: Int, learnedIds: Set<String>)
    fun getProgress(deckId: String): Flow<Int>
    suspend fun getDeckProgressEntity(deckId: String): com.minlish.app.data.source.local.DeckProgressEntity?
}
