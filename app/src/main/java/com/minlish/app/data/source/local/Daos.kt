package com.minlish.app.data.source.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/*
@Dao
interface BunnyDao {
    // User
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    // Account
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAccountByUserId(userId: String): AccountEntity?

    // User Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    // User Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSetting(setting: UserSettingEntity)

    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun getUserSetting(userId: String): Flow<UserSettingEntity?>

    // Decks
    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity)

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: String): DeckEntity?

    @Query("SELECT * FROM decks WHERE id = :id")
    fun getDeckByIdFlow(id: String): Flow<DeckEntity?>

    // Vocabulary
    @Query("""
        SELECT v.* FROM vocabularies v
        INNER JOIN deck_vocabularies dv ON v.id = dv.vocabularyId
        WHERE dv.deckId = :deckId
    """)
    fun getVocabularyByDeck(deckId: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabularies WHERE id = :id")
    fun getVocabularyById(id: String): Flow<VocabularyEntity?>

    @Query("SELECT * FROM vocabularies WHERE id = :id")
    suspend fun getVocabularyByIdDirect(id: String): VocabularyEntity?

    @Query("SELECT * FROM vocabularies WHERE word = :word LIMIT 1")
    suspend fun getVocabularyByWord(word: String): VocabularyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: VocabularyEntity)

    // DeckVocabulary (Many-to-Many)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeckVocabulary(join: DeckVocabularyEntity)

    // Spaced Repetition (User Vocabulary State)
    @Query("SELECT * FROM user_vocabulary_states WHERE vocabularyId = :vocabularyId AND userId = :userId")
    suspend fun getUserVocabularyState(vocabularyId: String, userId: String = "1"): UserVocabularyStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserVocabularyState(state: UserVocabularyStateEntity)

    @Query("SELECT * FROM user_vocabulary_states WHERE userId = :userId")
    fun getAllUserVocabularyStates(userId: String = "1"): Flow<List<UserVocabularyStateEntity>>

    @Transaction
    @Query("""
        SELECT v.* FROM vocabularies v 
        INNER JOIN deck_vocabularies dv ON v.id = dv.vocabularyId
        INNER JOIN user_vocabulary_states s ON v.id = s.vocabularyId 
        WHERE s.nextReview <= :currentTime AND s.userId = :userId
        AND (:deckId IS NULL OR dv.deckId = :deckId)
    """)
    fun getDueVocabulary(currentTime: Long, userId: String = "1", deckId: String? = null): Flow<List<VocabularyEntity>>

    @Query("""
        SELECT v.* FROM vocabularies v
        INNER JOIN deck_vocabularies dv ON v.id = dv.vocabularyId
        WHERE v.id NOT IN (SELECT vocabularyId FROM user_vocabulary_states WHERE userId = :userId)
        AND (:deckId IS NULL OR dv.deckId = :deckId)
        LIMIT :limit
    """)
    fun getNewVocabulary(userId: String = "1", deckId: String? = null, limit: Int = 10): Flow<List<VocabularyEntity>>

    // Review History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewHistory(history: ReviewHistoryEntity)

    @Query("SELECT * FROM review_histories WHERE vocabularyId = :vocabularyId AND userId = :userId")
    fun getReviewHistoryForVocabulary(vocabularyId: String, userId: String = "1"): Flow<List<ReviewHistoryEntity>>

    // Notifications
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String = "1"): Flow<List<NotificationEntity>>

    // User Stats
    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    fun getUserStatsFlow(userId: String = "1"): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    suspend fun getUserStatsDirect(userId: String = "1"): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStatsEntity)
}
*/

@Dao
interface ProgressDao {
    @Query("SELECT * FROM deck_progress WHERE deckId = :deckId")
    fun getProgressByDeck(deckId: String): Flow<DeckProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: DeckProgressEntity)
}