package com.minlish.app.data.source.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BunnyDao {
    // User
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdFlow(id: Int): Flow<UserEntity?>

    // Account
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAccountByUserId(userId: Int): AccountEntity?

    // User Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getUserProfile(userId: Int): Flow<UserProfileEntity?>

    // User Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSetting(setting: UserSettingEntity)

    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun getUserSetting(userId: Int): Flow<UserSettingEntity?>

    // Decks
    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Int): DeckEntity?

    @Query("SELECT * FROM decks WHERE id = :id")
    fun getDeckByIdFlow(id: Int): Flow<DeckEntity?>

    // Vocabulary
    @Query("""
        SELECT v.* FROM vocabularies v
        INNER JOIN deck_vocabularies dv ON v.id = dv.vocabularyId
        WHERE dv.deckId = :deckId
    """)
    fun getVocabularyByDeck(deckId: Int): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabularies WHERE id = :id")
    fun getVocabularyById(id: Int): Flow<VocabularyEntity?>

    @Query("SELECT * FROM vocabularies WHERE id = :id")
    suspend fun getVocabularyByIdDirect(id: Int): VocabularyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: VocabularyEntity): Long

    // DeckVocabulary (Many-to-Many)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeckVocabulary(join: DeckVocabularyEntity)

    // Spaced Repetition (User Vocabulary State)
    @Query("SELECT * FROM user_vocabulary_states WHERE vocabularyId = :vocabularyId AND userId = :userId")
    suspend fun getUserVocabularyState(vocabularyId: Int, userId: Int = 1): UserVocabularyStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserVocabularyState(state: UserVocabularyStateEntity)

    @Query("SELECT * FROM user_vocabulary_states WHERE userId = :userId")
    fun getAllUserVocabularyStates(userId: Int = 1): Flow<List<UserVocabularyStateEntity>>

    @Transaction
    @Query("""
        SELECT v.* FROM vocabularies v 
        INNER JOIN deck_vocabularies dv ON v.id = dv.vocabularyId
        INNER JOIN user_vocabulary_states s ON v.id = s.vocabularyId 
        WHERE s.nextReview <= :currentTime AND s.userId = :userId
        AND (:deckId IS NULL OR dv.deckId = :deckId)
    """)
    fun getDueVocabulary(currentTime: Long, userId: Int = 1, deckId: Int? = null): Flow<List<VocabularyEntity>>

    @Query("""
        SELECT v.* FROM vocabularies v
        INNER JOIN deck_vocabularies dv ON v.id = dv.vocabularyId
        WHERE v.id NOT IN (SELECT vocabularyId FROM user_vocabulary_states WHERE userId = :userId)
        AND (:deckId IS NULL OR dv.deckId = :deckId)
        LIMIT :limit
    """)
    fun getNewVocabulary(userId: Int = 1, deckId: Int? = null, limit: Int = 10): Flow<List<VocabularyEntity>>

    // Review History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewHistory(history: ReviewHistoryEntity)

    @Query("SELECT * FROM review_histories WHERE vocabularyId = :vocabularyId AND userId = :userId")
    fun getReviewHistoryForVocabulary(vocabularyId: Int, userId: Int = 1): Flow<List<ReviewHistoryEntity>>

    // Notifications
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: Int = 1): Flow<List<NotificationEntity>>

    // User Stats
    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    fun getUserStatsFlow(userId: Int = 1): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    suspend fun getUserStatsDirect(userId: Int = 1): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStatsEntity)
}
