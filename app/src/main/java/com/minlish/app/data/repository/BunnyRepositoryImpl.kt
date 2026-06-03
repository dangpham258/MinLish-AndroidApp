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
import kotlinx.coroutines.tasks.await
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
        val currentWords = user.userProfile.wordsLearned
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
        emit(firebaseService.getDecks(getCurrentUserId()))
    }

    override fun getVocabularyByDeck(deckId: String, vocabularyIds: List<String>): Flow<List<Vocabulary>> = flow {
        emit(firebaseService.getVocabularyByDeck(deckId, vocabularyIds))
    }

    override suspend fun insertDeck(deck: Deck): Long {
        firebaseService.saveDeck(deck)
        return 0
    }

    override suspend fun deleteDeck(deckId: String) {
        firebaseService.deleteDeck(deckId)
    }

    override suspend fun getDeckById(deckId: String): Deck? {
        val decks = firebaseService.getDecks(getCurrentUserId())
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
    override suspend fun insertVocabulary(vocabulary: Vocabulary): Long {
        // Chỉ lưu lên Firebase nếu có deckId (deck do user tạo)
        if (vocabulary.deckId.isNotBlank()) {
            firebaseService.saveVocabularyToDeck(vocabulary.deckId, vocabulary)
        }
        return 0
    }

    override suspend fun deleteVocabulary(deckId: String, vocabId: String) {
        firebaseService.deleteVocabularyFromDeck(deckId, vocabId)
    }
    override suspend fun getVocabularyByIdDirect(id: String): Vocabulary? = null
    override fun getActiveUserVocabularyStates(): Flow<List<UserVocabularyState>> = flow { emit(emptyList()) }
    override fun getVocabularyDueForReview(currentTime: Long, deckId: String?): Flow<List<Vocabulary>> = flow {
        val userId = getCurrentUserId()
        try {
            val snapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("vocabularyStates").child(userId).get().await()
            if (!snapshot.exists()) {
                emit(emptyList())
                return@flow
            }
            
            val dueStates = snapshot.children.mapNotNull { child ->
                try {
                    val vocabId = child.child("vocabId").getValue(String::class.java) ?: child.key ?: ""
                    if (vocabId.isEmpty()) return@mapNotNull null
                    
                    val nextReviewSnapshot = child.child("nextReview")
                    val nextReviewTime = if (nextReviewSnapshot.hasChild("time")) {
                        nextReviewSnapshot.child("time").getValue(Long::class.java) ?: System.currentTimeMillis()
                    } else {
                        nextReviewSnapshot.getValue(Long::class.java) ?: System.currentTimeMillis()
                    }
                    
                    val stateDeckId = child.child("deckId").getValue(String::class.java) ?: ""
                    
                    if (deckId != null && deckId.isNotBlank() && stateDeckId != deckId) {
                        return@mapNotNull null
                    }
                    
                    if (nextReviewTime > currentTime) {
                        return@mapNotNull null
                    }
                    
                    UserVocabularyState(
                        vocabId = vocabId,
                        deckId = stateDeckId,
                        interval = child.child("interval").getValue(Double::class.java) ?: 1.0,
                        repetition = child.child("repetition").getValue(Int::class.java) ?: 0,
                        easeFactor = child.child("easeFactor").getValue(Double::class.java) ?: 2.5,
                        nextReview = Date(nextReviewTime)
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            if (dueStates.isEmpty()) {
                emit(emptyList())
                return@flow
            }
            
            val grouped = dueStates.groupBy { it.deckId.ifBlank { "deck_minlish_01" } }
            val allVocabularies = mutableListOf<Vocabulary>()
            for ((dId, states) in grouped) {
                val vocabIds = states.map { it.vocabId }
                val vocabs = firebaseService.getVocabularyByDeck(dId, vocabIds)
                allVocabularies.addAll(vocabs)
            }
            emit(allVocabularies)
        } catch (e: Exception) {
            android.util.Log.e("BunnyRepositoryImpl", "Error getting due vocabularies: ${e.message}")
            emit(emptyList())
        }
    }
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

    override suspend fun getUserProgress(userId: String): com.minlish.app.presentation.dashboard.model.UserProgress? {
        return firebaseService.getUserProgress(userId)
    }

    override suspend fun saveUserProgress(userId: String, progress: com.minlish.app.presentation.dashboard.model.UserProgress) {
        firebaseService.saveUserProgress(userId, progress)
    }

    override suspend fun getReviewHistories(userId: String): List<ReviewHistory>? {
        return firebaseService.getReviewHistories(userId)
    }

    override suspend fun saveReviewHistory(userId: String, history: ReviewHistory) {
        firebaseService.saveReviewHistory(userId, history)
    }

    override suspend fun initializeEverythingWithFullData(userId: String) {
        firebaseService.initializeEverythingWithFullData(userId)
    }

    override suspend fun getDailyPlanTelemetry(userId: String): com.minlish.app.presentation.dashboard.model.DailyPlanTelemetry {
        return firebaseService.getDailyPlanTelemetry(userId)
    }
}
