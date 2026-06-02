package com.minlish.app.data.repository

import com.minlish.app.core.database.BunnyDatabase
import com.minlish.app.data.source.remote.FirebaseDatabaseService
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
    private val database: BunnyDatabase
) : BunnyRepository {

    override suspend fun saveProgress(deckId: String, learnedCount: Int, totalCount: Int) {
        database.progressDao().saveProgress(
            DeckProgressEntity(deckId, learnedCount, totalCount)
        )
    }

    override fun getProgress(deckId: String): Flow<Int> {
        return database.progressDao().getProgressByDeck(deckId).map { it?.learnedCount ?: 0 }
    }

    // Phát luồng danh sách Deck trực tiếp từ nguồn Firebase
    override fun getDecks(): Flow<List<Deck>> = flow {
        emit(firebaseService.getDecks())
    }

    // Phát luồng danh sách từ vựng theo cấu trúc deck_vocabularies mới
    override fun getVocabularyByDeck(deckId: String, vocabularyIds: List<String>): Flow<List<Vocabulary>> = flow {
        emit(firebaseService.getVocabularyByDeck(deckId, vocabularyIds))
    }

    override suspend fun insertDeck(deck: Deck): Long {
        // Thực hiện tương tự lệnh setValue() lên Firebase nếu cần tính năng tạo bộ thẻ mới
        return 0
    }

    override suspend fun getDeckById(deckId: String): Deck? {
        // Tối ưu: Nếu đã có cache decks từ FirebaseService thì tìm nhanh, 
        // nếu không thì mới fetch. Tránh việc đứng ở 0% quá lâu.
        val decks = firebaseService.getDecks()
        return decks.find { it.id == deckId } ?: decks.find { it.deckName.contains(deckId, ignoreCase = true) }
    }

    override fun getDeckByIdFlow(deckId: String): Flow<Deck?> = flow {
        emit(getDeckById(deckId))
    }

    override suspend fun saveUserVocabularyState(state: UserVocabularyState, previousEaseFactor: EaseFactor?) {
        firebaseService.syncUserVocabularyState("test_user_001", state)
        
        // Tự động tạo một bản ghi lịch sử khi lưu trạng thái mới
        val history = ReviewHistory(
            vocabId = state.vocabId,
            rating = state.easeFactor,
            learningTime = Date()
        )
        firebaseService.syncReviewHistory("test_user_001", history)
    }

    // --- Các hàm Stub trống tạm thời không ảnh hưởng tiến độ hiển thị Deck ---
    override fun getVocabularyById(id: String): Flow<Vocabulary?> = flow { emit(null) }
    override suspend fun insertVocabulary(vocabulary: Vocabulary): Long = 0
    override suspend fun getVocabularyByIdDirect(id: String): Vocabulary? = null
    override fun getActiveUserVocabularyStates(): Flow<List<UserVocabularyState>> = flow { emit(emptyList()) }
    override fun getVocabularyDueForReview(currentTime: Long, deckId: String?): Flow<List<Vocabulary>> = flow { emit(emptyList()) }
    override fun getNewVocabularyForLearning(deckId: String?, limit: Int): Flow<List<Vocabulary>> = flow { emit(emptyList()) }
    override suspend fun getUserVocabularyState(vocabularyId: String): UserVocabularyState? = null
    override fun getUserName(): Flow<String> = flow { emit("Hoài Phương") }
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
        firebaseService.syncReviewHistory("test_user_001", history)
    }

    override fun getNotifications(): Flow<List<Notification>> = flow { emit(emptyList()) }
    override suspend fun addNotification(notification: Notification) {}
    override suspend fun prepopulateInitialData() {}
}
