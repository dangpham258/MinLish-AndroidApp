package com.minlish.app.data.repository

import com.minlish.app.core.database.BunnyDatabase
import com.minlish.app.data.source.remote.FirebaseDatabaseService
import com.minlish.app.data.source.remote.FirebaseAuthApi
import com.minlish.app.data.source.local.DeckProgressEntity
import com.minlish.app.domain.model.*
import com.minlish.app.domain.model.enumration.*
import com.minlish.app.domain.repository.BunnyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.*

class BunnyRepositoryImpl(
    private val firebaseService: FirebaseDatabaseService,
    private val database: BunnyDatabase,
    private val authApi: FirebaseAuthApi
) : BunnyRepository {

    private fun getCurrentUserId(): String {
        return authApi.getCurrentUser()?.uid ?: "guest_user"
    }

    override suspend fun saveProgress(deckId: String, learnedCount: Int, totalCount: Int, learnedIds: Set<String>) {
        val userId = getCurrentUserId()
        database.progressDao().saveProgress(
            DeckProgressEntity(
                id = "${userId}_$deckId",
                userId = userId,
                deckId = deckId,
                learnedCount = learnedCount,
                totalCount = totalCount,
                learnedVocabularyIds = learnedIds.joinToString(",")
            )
        )
    }

    private suspend fun updateUserWordsLearned() {
        val userId = getCurrentUserId()
        val user = firebaseService.getUserFromSnapshot(userId) ?: return
        val currentWords = user.wordsLearned
        firebaseService.updateUser(userId, mapOf("wordsLearned" to currentWords + 1))
    }

    override suspend fun notifyNewWordLearned(vocabularyId: String) {
        val userId = getCurrentUserId()
        val currentState = firebaseService.getUserVocabularyState(userId, vocabularyId)
        
        if (currentState == null || currentState.repetition == 0) {
            updateUserWordsLearned()
            
            if (currentState == null) {
                val newState = UserVocabularyState(
                    vocabId = vocabularyId,
                    repetition = 1,
                    nextReview = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
                )
                firebaseService.syncUserVocabularyState(userId, newState)
            }
        }
    }

    override fun getProgress(deckId: String): Flow<Int> {
        return database.progressDao().getProgressByDeck(getCurrentUserId(), deckId)
            .map { it?.learnedCount ?: 0 }
    }

    override suspend fun getDeckProgressEntity(deckId: String): DeckProgressEntity? {
        return database.progressDao().getProgressByDeckDirect(getCurrentUserId(), deckId)
    }

    override fun getDecks(): Flow<List<Deck>> = flow {
        emit(firebaseService.getDecks())
    }

    override fun getVocabularyByDeck(deckId: String, vocabularyIds: List<String>): Flow<List<Vocabulary>> = flow {
        emit(firebaseService.getVocabularyByDeck(deckId, vocabularyIds))
    }

    override suspend fun insertDeck(deck: Deck): Long {
        return 0
    }

    override suspend fun getDeckById(deckId: String): Deck? {
        val decks = firebaseService.getDecks()
        return decks.find { it.id == deckId } ?: decks.find { it.deckName.contains(deckId, ignoreCase = true) }
    }

    override fun getDeckByIdFlow(deckId: String): Flow<Deck?> = flow {
        emit(getDeckById(deckId))
    }

    override suspend fun saveUserVocabularyState(state: UserVocabularyState, rating: EaseFactor?) {
        val userId = getCurrentUserId()
        val oldState = firebaseService.getUserVocabularyState(userId, state.vocabId)
        val isFirstTimeLearned = (oldState == null || oldState.repetition == 0) && state.repetition > 0
        
        firebaseService.syncUserVocabularyState(userId, state)
        
        if (isFirstTimeLearned) {
            updateUserWordsLearned()
        }
        
        if (rating != null) {
            val history = ReviewHistory(
                id = UUID.randomUUID().toString(),
                vocabId = state.vocabId,
                rating = rating,
                learningTime = Date()
            )
            firebaseService.syncReviewHistory(userId, history)
        }
    }

    override fun getVocabularyById(id: String): Flow<Vocabulary?> = flow { emit(null) }
    override suspend fun insertVocabulary(vocabulary: Vocabulary): Long = 0
    override suspend fun getVocabularyByIdDirect(id: String): Vocabulary? = null
    override fun getActiveUserVocabularyStates(): Flow<List<UserVocabularyState>> = flow { emit(emptyList()) }
    override fun getVocabularyDueForReview(currentTime: Long, deckId: String?): Flow<List<Vocabulary>> = flow { emit(emptyList()) }
    override fun getNewVocabularyForLearning(deckId: String?, limit: Int): Flow<List<Vocabulary>> = flow { emit(emptyList()) }
    
    override suspend fun getUserVocabularyState(vocabularyId: String): UserVocabularyState? {
        return firebaseService.getUserVocabularyState(getCurrentUserId(), vocabularyId)
    }

    override fun getUserName(): Flow<String> = flow { emit(authApi.getCurrentUser()?.displayName ?: "Người dùng") }
    override suspend fun updateUserName(name: String) {}
    override fun getLearningGoal(): Flow<LearningGoal> = flow { emit(LearningGoal.TOEIC) }
    override suspend fun updateLearningGoal(goal: LearningGoal) {}
    override fun getInitialLevel(): Flow<InitialLevel> = flow { emit(InitialLevel.B1) }
    override suspend fun updateInitialLevel(level: InitialLevel) {}
    override fun getUser(userId: Int): Flow<User?> = flow { emit(null) }
    override fun getLearnedWordsCount(): Flow<Int> = flow { emit(0) }
    override fun getStreakDaysCount(): Flow<Int> = flow { emit(0) }
    override suspend fun incrementStreak() {}
    override fun getReviewHistory(vocabularyId: String): Flow<List<ReviewHistory>> = flow { emit(emptyList()) }
    override suspend fun addReviewHistory(history: ReviewHistory) {
        firebaseService.syncReviewHistory(getCurrentUserId(), history)
    }

    override fun getNotifications(): Flow<List<Notification>> = flow { emit(emptyList()) }
    override suspend fun addNotification(notification: Notification) {}
    override suspend fun prepopulateInitialData() {}
}
