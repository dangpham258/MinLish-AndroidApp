package com.minlish.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.minlish.app.data.source.local.*
import com.minlish.app.domain.model.*
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.CalculateSrsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class BunnyRepositoryImpl(
    private val bunnyDao: BunnyDao,
    private val calculateSrsUseCase: CalculateSrsUseCase
) : BunnyRepository {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Decks
    override fun getDecks(): Flow<List<Deck>> = bunnyDao.getAllDecks().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun insertDeck(deck: Deck): Long = bunnyDao.insertDeck(deck.toEntity())

    override suspend fun getDeckById(deckId: Int): Deck? {
        val deck = bunnyDao.getDeckById(deckId)?.toDomain() ?: return null
        
        // Count total words in this deck
        val allWords = bunnyDao.getVocabularyByDeck(deckId).first()
        val totalWords = allWords.size
        
        // Count learned words
        val learnedStates = bunnyDao.getAllUserVocabularyStates(1).first()
        val learnedWords = allWords.count { word -> 
            learnedStates.any { state -> state.vocabularyId == word.id }
        }
        
        // Get streak from UserStats
        val streak = bunnyDao.getUserStatsDirect(1)?.streakDays ?: 0
        
        return deck.copy(totalWords = totalWords, learned = learnedWords, streak = streak)
    }

    override fun getDeckByIdFlow(deckId: Int): Flow<Deck?> = 
        bunnyDao.getDeckByIdFlow(deckId).map { entity ->
            val deck = entity?.toDomain() ?: return@map null
            
            // Count total words in this deck
            val allWords = bunnyDao.getVocabularyByDeck(deckId).first()
            val totalWords = allWords.size
            
            // Count learned words
            val learnedStates = bunnyDao.getAllUserVocabularyStates(1).first()
            val learnedWords = allWords.count { word -> 
                learnedStates.any { state -> state.vocabularyId == word.id }
            }
            
            // Get streak from UserStats
            val streak = bunnyDao.getUserStatsDirect(1)?.streakDays ?: 0
            
            deck.copy(totalWords = totalWords, learned = learnedWords, streak = streak)
        }

    // Vocabulary
    override fun getVocabularyByDeck(deckId: Int): Flow<List<Vocabulary>> = 
        bunnyDao.getVocabularyByDeck(deckId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getVocabularyById(id: Int): Flow<Vocabulary?> = 
        bunnyDao.getVocabularyById(id).map { it?.toDomain() }

    override suspend fun insertVocabulary(vocabulary: Vocabulary): Long = 
        bunnyDao.insertVocabulary(vocabulary.toEntity())

    override suspend fun getVocabularyByIdDirect(id: Int): Vocabulary? = 
        bunnyDao.getVocabularyByIdDirect(id)?.toDomain()

    // SRS
    override fun getActiveUserVocabularyStates(): Flow<List<UserVocabularyState>> = 
        bunnyDao.getAllUserVocabularyStates(1).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getVocabularyDueForReview(currentTime: Long, deckId: Int?): Flow<List<Vocabulary>> = 
        bunnyDao.getDueVocabulary(currentTime, 1, deckId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getNewVocabularyForLearning(deckId: Int?, limit: Int): Flow<List<Vocabulary>> = 
        bunnyDao.getNewVocabulary(1, deckId, limit).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getUserVocabularyState(vocabularyId: Int): UserVocabularyState? = 
        bunnyDao.getUserVocabularyState(vocabularyId, 1)?.toDomain()

    override suspend fun saveUserVocabularyState(state: UserVocabularyState, previousEaseFactor: EaseFactor?) {
        // Local Save
        bunnyDao.insertOrUpdateUserVocabularyState(state.toEntity())
        bunnyDao.insertReviewHistory(
            ReviewHistoryEntity(
                userId = state.userId,
                vocabularyId = state.vocabularyId,
                learningTime = System.currentTimeMillis()
            )
        )

        // Update Stats (Learned Count)
        val userStats = bunnyDao.getUserStatsDirect(state.userId) ?: UserStatsEntity(
            userId = state.userId, 
            learnedWordsCount = 0, 
            streakDays = 0, 
            lastActiveTimestamp = 0L
        )
        
        var totalLearned = userStats.learnedWordsCount
        
        // If first time learning this word, increment learned count
        if (previousEaseFactor == null) {
            totalLearned += 1
        }

        // Update local stats
        bunnyDao.insertOrUpdateUserStats(userStats.copy(
            learnedWordsCount = totalLearned,
            lastActiveTimestamp = System.currentTimeMillis()
        ))
    }

    // User Profile / Settings
    override fun getUserName(): Flow<String> = bunnyDao.getUserStatsFlow(1).map { 
        bunnyDao.getUserById(1)?.name ?: "Bunny Learner"
    }
    
    override suspend fun updateUserName(name: String) {
        val user = bunnyDao.getUserById(1) ?: UserEntity(id = 1, name = name)
        bunnyDao.insertUser(user.copy(name = name))
    }

    override fun getLearningGoal(): Flow<LearningGoal> = bunnyDao.getUserProfile(1).map { 
        val goals = it?.learningGoalsJson?.split(",")?.filter { g -> g.isNotBlank() } ?: emptyList()
        if (goals.isNotEmpty()) LearningGoal.valueOf(goals.first()) else LearningGoal.IELTS
    }

    override suspend fun updateLearningGoal(goal: LearningGoal) {
        bunnyDao.insertUserProfile(UserProfileEntity(userId = 1, learningGoalsJson = goal.name, initialLevel = "B1"))
    }

    override fun getInitialLevel(): Flow<InitialLevel> = bunnyDao.getUserProfile(1).map { 
        InitialLevel.valueOf(it?.initialLevel ?: "B1") 
    }

    override suspend fun updateInitialLevel(level: InitialLevel) {
        bunnyDao.insertUserProfile(UserProfileEntity(userId = 1, learningGoalsJson = "IELTS", initialLevel = level.name))
    }

    override fun getUser(userId: Int): Flow<User?> = bunnyDao.getUserByIdFlow(userId).map { it?.toDomain() }

    // Stats
    override fun getLearnedWordsCount(): Flow<Int> = bunnyDao.getUserStatsFlow(1).map { it?.learnedWordsCount ?: 0 }
    override fun getStreakDaysCount(): Flow<Int> = bunnyDao.getUserStatsFlow(1).map { it?.streakDays ?: 0 }
    
    override suspend fun incrementStreak() {
        val currentStats = bunnyDao.getUserStatsDirect(1) ?: UserStatsEntity(
            userId = 1, 
            learnedWordsCount = 0, 
            streakDays = 0, 
            lastActiveTimestamp = 0L
        )
        
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = currentStats.lastActiveTimestamp
        val lastActiveDay = calendar.get(Calendar.DAY_OF_YEAR)
        val lastActiveYear = calendar.get(Calendar.YEAR)
        
        // Only increment if last active was yesterday or before
        if (todayYear > lastActiveYear || (todayYear == lastActiveYear && today > lastActiveDay)) {
            val newStreak = if (
                (todayYear == lastActiveYear && today == lastActiveDay + 1) ||
                (todayYear > lastActiveYear && today == 1 && lastActiveDay == calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
            ) {
                currentStats.streakDays + 1
            } else if (currentStats.lastActiveTimestamp == 0L) {
                1
            } else {
                1 // Reset if more than 1 day missed
            }
            
            bunnyDao.insertOrUpdateUserStats(currentStats.copy(
                streakDays = newStreak,
                lastActiveTimestamp = now
            ))
        }
    }

    // Review History
    override fun getReviewHistory(vocabularyId: Int): Flow<List<ReviewHistory>> = 
        bunnyDao.getReviewHistoryForVocabulary(vocabularyId, 1).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addReviewHistory(history: ReviewHistory) {
        bunnyDao.insertReviewHistory(history.toEntity())
    }

    // Notifications
    override fun getNotifications(): Flow<List<Notification>> = 
        bunnyDao.getNotificationsForUser(1).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addNotification(notification: Notification) {
        bunnyDao.insertNotification(notification.toEntity())
    }

    // Mock/Initial Data
    override suspend fun prepopulateInitialData() {
        if (bunnyDao.getDeckById(1) == null) {
            if (bunnyDao.getUserById(1) == null) {
                bunnyDao.insertUser(UserEntity(id = 1, name = "Bunny Learner"))
                bunnyDao.insertUserProfile(UserProfileEntity(userId = 1, learningGoalsJson = "IELTS", initialLevel = "B1"))
                bunnyDao.insertUserSetting(UserSettingEntity(userId = 1, dailyNewWordGoal = 10, dailyReviewGoal = 20))
                bunnyDao.insertOrUpdateUserStats(UserStatsEntity(userId = 1, learnedWordsCount = 0, streakDays = 0, lastActiveTimestamp = 0L))
            }
            
            val deckId = bunnyDao.insertDeck(DeckEntity(
                id = 1,
                deckName = "IELTS Academic Core",
                description = "Bộ từ vựng học thuật quan trọng.",
                tagsJson = "IELTS,COMMUNICATION",
                createId = 1,
                isPublic = true
            )).toInt()
            
            val testWords = listOf(
                VocabularyEntity(
                    word = "Abandon", pronunciation = "/əˈbæn.dən/", meaning = "Từ bỏ, bỏ rơi",
                    descriptionEnglish = "To leave a place, thing, or person, usually for ever.",
                    exampleJson = "The baby had been abandoned by its mother.|We had to abandon the car.",
                    synonymsJson = "Desert|Leave|Quit",
                    antonymsJson = "Stay|Keep|Support",
                    relatedWordsJson = "", note = "Thường dùng trong văn cảnh tiêu cực", wordType = "Verb"
                ),
                VocabularyEntity(
                    word = "Benevolent", pronunciation = "/bəˈnev.əl.ənt/", meaning = "Nhân từ, rộng lượng",
                    descriptionEnglish = "Kind and helpful.",
                    exampleJson = "He was a benevolent old man and wouldn't hurt a fly.|A benevolent organization.",
                    synonymsJson = "Kind|Generous|Compassionate",
                    antonymsJson = "Cruel|Mean|Selfish",
                    relatedWordsJson = "", note = "Academic word", wordType = "Adjective"
                ),
                VocabularyEntity(
                    word = "Coherent", pronunciation = "/koʊˈhɪr.ənt/", meaning = "Mạch lạc, chặt chẽ",
                    descriptionEnglish = "Logical and consistent.",
                    exampleJson = "He proposed a coherent plan for reform.|She failed to give a coherent account of the events.",
                    synonymsJson = "Logical|Consistent|Rational",
                    antonymsJson = "Incoherent|Confused|Irrational",
                    relatedWordsJson = "", note = "Writing Task 2", wordType = "Adjective"
                ),
                VocabularyEntity(
                    word = "Deteriorate", pronunciation = "/dɪˈtɪr.i.ə.reɪt/", meaning = "Sụp đổ, tệ đi",
                    descriptionEnglish = "To become worse.",
                    exampleJson = "The weather conditions deteriorated rapidly.|His health has deteriorated in recent months.",
                    synonymsJson = "Worsen|Decline|Degenerate",
                    antonymsJson = "Improve|Better|Enhance",
                    relatedWordsJson = "", note = "Common in Listening", wordType = "Verb"
                ),
                VocabularyEntity(
                    word = "Eloquent", pronunciation = "/ˈel.ə.kwənt/", meaning = "Hùng hồn, có tài hùng biện",
                    descriptionEnglish = "Fluent or persuasive in speaking or writing.",
                    exampleJson = "An eloquent speech against the war.|She made an eloquent appeal for help.",
                    synonymsJson = "Articulate|Fluent|Persuasive",
                    antonymsJson = "Inarticulate|Hesitant|Mute",
                    relatedWordsJson = "", note = "Speaking high score", wordType = "Adjective"
                ),
                VocabularyEntity(
                    word = "Frugal", pronunciation = "/ˈfruː.ɡəl/", meaning = "Tiết kiệm, căn cơ",
                    descriptionEnglish = "Simple and plain and costing little of money.",
                    exampleJson = "A frugal lifestyle.|He has always been frugal with his money.",
                    synonymsJson = "Thrifty|Economical|Sparing",
                    antonymsJson = "Extravagant|Wasteful|Lavish",
                    relatedWordsJson = "", note = "Topic: Money", wordType = "Adjective"
                ),
                VocabularyEntity(
                    word = "Gullible", pronunciation = "/ˈɡʌl.ə.bəl/", meaning = "Nhẹ dạ, cả tin",
                    descriptionEnglish = "Easily deceived or tricked.",
                    exampleJson = "There are any number of silver-tongued crooks who prey on gullible people.",
                    synonymsJson = "Naïve|Innocent|Trusting",
                    antonymsJson = "Skeptical|Cynical|Suspicious",
                    relatedWordsJson = "", note = "Personality trait", wordType = "Adjective"
                ),
                VocabularyEntity(
                    word = "Hypothesis", pronunciation = "/haɪˈpɒθ.ə.sɪs/", meaning = "Giả thuyết",
                    descriptionEnglish = "A proposed explanation made on the basis of limited evidence.",
                    exampleJson = "Several hypotheses for global warming have been suggested.",
                    synonymsJson = "Theory|Thesis|Proposition",
                    antonymsJson = "Fact|Reality|Certainty",
                    relatedWordsJson = "", note = "Academic context", wordType = "Noun"
                ),
                VocabularyEntity(
                    word = "Inevitable", pronunciation = "/ɪˈnev.ɪ.tə.bəl/", meaning = "Không thể tránh khỏi",
                    descriptionEnglish = "Certain to happen; unavoidable.",
                    exampleJson = "The accident was the inevitable consequence of carelessness.",
                    synonymsJson = "Unavoidable|Certain|Inescapable",
                    antonymsJson = "Avoidable|Uncertain|Doubtful",
                    relatedWordsJson = "", note = "Useful for conclusions", wordType = "Adjective"
                ),
                VocabularyEntity(
                    word = "Jeopardy", pronunciation = "/ˈdʒep.ə.di/", meaning = "Sự nguy hiểm",
                    descriptionEnglish = "Danger of loss, harm, or failure.",
                    exampleJson = "The lives of thousands of birds are in jeopardy as a result of the oil spill.",
                    synonymsJson = "Danger|Peril|Risk",
                    antonymsJson = "Safety|Security|Protection",
                    relatedWordsJson = "", note = "Phrase: in jeopardy", wordType = "Noun"
                )
            )

            testWords.forEach { word ->
                val vId = bunnyDao.insertVocabulary(word).toInt()
                bunnyDao.insertDeckVocabulary(DeckVocabularyEntity(deckId, vId))
            }
        }
    }

    // Mappers
    private fun DeckEntity.toDomain() = Deck(
        id = id,
        deckName = deckName,
        description = description,
        tags = tagsJson.split(",").filter { it.isNotBlank() }.map { LearningGoal.valueOf(it) },
        createId = createId,
        isPublic = isPublic
    )

    private fun Deck.toEntity() = DeckEntity(
        id = id,
        deckName = deckName,
        description = description,
        tagsJson = tags.joinToString(",") { it.name },
        createId = createId,
        isPublic = isPublic
    )

    private fun VocabularyEntity.toDomain() = Vocabulary(
        id = id,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        descriptionEnglish = descriptionEnglish,
        example = if (exampleJson.isBlank()) emptyList() else exampleJson.split("|"),
        synonyms = if (synonymsJson.isBlank()) emptyList() else synonymsJson.split("|"),
        antonyms = if (antonymsJson.isBlank()) emptyList() else antonymsJson.split("|"),
        relatedWords = emptyList(),
        note = note,
        imageUrl = imageUrl,
        wordType = wordType
    )

    private fun Vocabulary.toEntity() = VocabularyEntity(
        id = id,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        descriptionEnglish = descriptionEnglish,
        exampleJson = example.joinToString("|"),
        synonymsJson = synonyms.joinToString("|"),
        antonymsJson = antonyms.joinToString("|"),
        relatedWordsJson = "",
        note = note,
        imageUrl = imageUrl,
        wordType = wordType
    )

    private fun UserEntity.toDomain() = User(id = id, name = name, totalWords = totalWords, rememberedWords = rememberedWords)

    private fun UserVocabularyStateEntity.toDomain() = UserVocabularyState(
        userId = userId,
        vocabularyId = vocabularyId,
        interval = interval,
        repetition = repetition,
        easeFactor = EaseFactor.valueOf(easeFactor),
        nextReview = Date(nextReview)
    )

    private fun UserVocabularyState.toEntity() = UserVocabularyStateEntity(
        userId = userId,
        vocabularyId = vocabularyId,
        interval = interval,
        repetition = repetition,
        easeFactor = easeFactor.name,
        nextReview = nextReview.time
    )

    private fun ReviewHistoryEntity.toDomain() = ReviewHistory(
        id = id,
        userId = userId,
        vocabularyId = vocabularyId,
        learningTime = Date(learningTime)
    )

    private fun ReviewHistory.toEntity() = ReviewHistoryEntity(
        id = id,
        userId = userId,
        vocabularyId = vocabularyId,
        learningTime = learningTime.time
    )

    private fun NotificationEntity.toDomain() = Notification(
        id = id,
        userId = userId,
        title = title,
        content = content,
        timestamp = Date(timestamp),
        isRead = isRead
    )

    private fun Notification.toEntity() = NotificationEntity(
        id = id,
        userId = userId,
        title = title,
        content = content,
        timestamp = timestamp.time,
        isRead = isRead
    )
}
