package com.minlish.app.data.source.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minlish.app.domain.model.Account
import com.minlish.app.domain.model.User
import com.minlish.app.domain.model.UserProfile
import com.minlish.app.domain.model.UserSetting
import com.minlish.app.domain.model.enumration.InitialLevel
import com.minlish.app.domain.model.enumration.LearningGoal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.util.Date
import java.util.UUID
import java.util.Calendar
import com.minlish.app.domain.model.enumration.EaseFactor
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.model.UserVocabularyState
import com.minlish.app.domain.model.ReviewHistory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDatabaseService @Inject constructor() {

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    suspend fun createUserIfNotExists(
        uid: String,
        name: String,
        email: String
    ) {
        val userRef = usersRef.child(uid)
        val snapshot = userRef.get().await()
        
        if (!snapshot.exists()) {
            // Tao moi - set tat ca cac field
            val userData = mapOf(
                "id" to uid,
                "name" to name,
                "account" to mapOf(
                    "email" to email,
                    "password" to ""
                ),
                "userProfile" to mapOf(
                    "tags" to emptyList<String>(),
                    "initialLevel" to InitialLevel.B1.value,
                    "emailNotification" to false,
                    "dailyReminder" to false,
                    "spacedRepetition" to false,
                    "avatarIndex" to 0,
                    "wordsLearned" to 0,
                    "streak" to 0
                ),
                "userSetting" to mapOf(
                    "dailyNewWordGoal" to 0,
                    "dailyReviewGoal" to 0
                )
            )
            userRef.setValue(userData).await()
        } else {
            // User da ton tai - migrate len schema moi
            migrateUserToNewSchema(userRef, snapshot, email)
        }
    }

    private suspend fun migrateUserToNewSchema(userRef: com.google.firebase.database.DatabaseReference, snapshot: DataSnapshot, email: String) {
        val updates = mutableMapOf<String, Any?>()
        
        // Lay gia tri hien tai
        val currentName = snapshot.child("name").getValue(String::class.java) ?: ""
        val currentEmail = snapshot.child("account/email").getValue(String::class.java) 
            ?: snapshot.child("email").getValue(String::class.java) 
            ?: email
        val currentAvatarIndex = snapshot.child("avatarIndex").getValue(Int::class.java) ?: 0
        val currentWordsLearned = snapshot.child("wordsLearned").getValue(Int::class.java) ?: 0
        val currentStreak = snapshot.child("streak").getValue(Int::class.java) ?: 0
        
        // UserProfile
        val currentLearningGoal = snapshot.child("userProfile/learningGoal").children.mapNotNull {
            it.getValue(String::class.java)
        }
        val currentInitialLevel = snapshot.child("userProfile/initialLevel").getValue(String::class.java) ?: InitialLevel.B1.value
        val currentEmailNotification = snapshot.child("userProfile/emailNotification").getValue(Boolean::class.java) ?: false
        val currentDailyReminder = snapshot.child("userProfile/dailyReminder").getValue(Boolean::class.java) ?: false
        val currentSpacedRepetition = snapshot.child("userProfile/spacedRepetition").getValue(Boolean::class.java) ?: false
        
        // UserSetting
        val currentDailyNewWordGoal = snapshot.child("userSetting/dailyNewWordGoal").getValue(Int::class.java) ?: 0
        val currentDailyReviewGoal = snapshot.child("userSetting/dailyReviewGoal").getValue(Int::class.java) ?: 0
        
        // Cap nhat account
        updates["account"] = mapOf(
            "email" to currentEmail,
            "password" to ""
        )
        
        // Cap nhat userProfile - dong bo voi schema moi
        updates["userProfile"] = mapOf(
            "tags" to currentLearningGoal,
            "initialLevel" to currentInitialLevel,
            "emailNotification" to currentEmailNotification,
            "dailyReminder" to currentDailyReminder,
            "spacedRepetition" to currentSpacedRepetition,
            "avatarIndex" to currentAvatarIndex,
            "wordsLearned" to currentWordsLearned,
            "streak" to currentStreak
        )
        
        // Cap nhat userSetting - dong bo voi schema moi
        updates["userSetting"] = mapOf(
            "dailyNewWordGoal" to currentDailyNewWordGoal,
            "dailyReviewGoal" to currentDailyReviewGoal
        )
        
        // Xoa cac field cu nam ngoai userProfile
        updates["avatarIndex"] = null
        updates["wordsLearned"] = null
        updates["streak"] = null
        updates["name"] = currentName
        
        // Xoa cac field cu (nam o top-level)
        updates["email"] = null
        updates["createdAt"] = null
        
        // Update
        userRef.updateChildren(updates).await()
    }

    suspend fun getUser(uid: String): DataSnapshot? {
        return usersRef.child(uid).get().await()
    }

    suspend fun getUserFromSnapshot(uid: String): User? {
        val snapshot = usersRef.child(uid).get().await()
        val user = snapshot.toUser()
        
        // Auto-migrate: neu tags rong nhung co learningGoal, tu dong sync sang tags
        if (user != null && user.userProfile.tags.isEmpty()) {
            val learningGoalSnapshot = snapshot.child("userProfile/learningGoal")
            if (learningGoalSnapshot.hasChildren()) {
                val learningGoals = learningGoalSnapshot.children.mapNotNull { 
                    it.getValue(String::class.java) 
                }
                if (learningGoals.isNotEmpty()) {
                    usersRef.child(uid).child("userProfile").child("tags").setValue(learningGoals).await()
                }
            }
        }
        
        return user
    }

    fun observeUser(uid: String): Flow<DataSnapshot?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        usersRef.child(uid).addValueEventListener(listener)
        awaitClose { usersRef.child(uid).removeEventListener(listener) }
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>) {
        usersRef.child(uid).updateChildren(updates).await()
    }

    suspend fun updateUserField(uid: String, path: String, value: Any) {
        usersRef.child(uid).child(path).setValue(value).await()
    }

    suspend fun findUserIdByEmail(email: String): String? {
        val snapshot = usersRef
            .orderByChild("account/email")
            .equalTo(email)
            .get()
            .await()

        return snapshot.children.firstOrNull()?.key
    }

    suspend fun createNotification(userId: String, notification: com.minlish.app.domain.model.Notification) {
        val notificationRef = database.getReference("notifications").child(userId).push()
        val notificationId = notification.id.ifBlank { notificationRef.key ?: UUID.randomUUID().toString() }
        val notificationData = mapOf(
            "id" to notificationId,
            "title" to notification.title,
            "content" to notification.content,
            "isRead" to notification.isRead,
            "createdAt" to com.google.firebase.database.ServerValue.TIMESTAMP
        )

        notificationRef.setValue(notificationData).await()
    }

    suspend fun deleteUser(uid: String) {
        usersRef.child(uid).removeValue().await()
    }

    private fun DataSnapshot.toUser(): User? {
        return try {
            // Doc tu field "tags" thay vi "learningGoal"
            val tagStrings = child("userProfile/tags").children.mapNotNull {
                it.getValue(String::class.java)
            }
            val tags = tagStrings.mapNotNull { value ->
                LearningGoal.entries.find { it.value == value }
            }
            
            // Hoac doc tu field cu "learningGoal" de ho tro migration
            val learningGoalStrings = child("userProfile/learningGoal").children.mapNotNull {
                it.getValue(String::class.java)
            }
            val learningGoals = if (tags.isEmpty() && learningGoalStrings.isNotEmpty()) {
                learningGoalStrings.mapNotNull { value ->
                    LearningGoal.entries.find { it.value == value }
                }
            } else {
                tags
            }

            val initialLevelValue = child("userProfile/initialLevel").getValue(String::class.java) ?: "B1"
            val initialLevel = InitialLevel.entries.find { it.value == initialLevelValue } ?: InitialLevel.B1

            // avatarIndex, wordsLearned, streak đã chuyển vào UserProfile trong model mới
            // Hỗ trợ cả 2 vị trí (migration): nếu có trong userProfile dùng trước, fallback sang top-level
            val avatarIndex = child("userProfile/avatarIndex").getValue(Int::class.java)
                ?: child("avatarIndex").getValue(Int::class.java) ?: 0
            val wordsLearned = child("userProfile/wordsLearned").getValue(Int::class.java)
                ?: child("wordsLearned").getValue(Int::class.java) ?: 0
            val streak = child("userProfile/streak").getValue(Int::class.java)
                ?: child("streak").getValue(Int::class.java) ?: 0

            User(
                id = key ?: "",
                name = child("name").getValue(String::class.java) ?: "",
                account = Account(
                    email = child("account/email").getValue(String::class.java) ?: "",
                    password = child("account/password").getValue(String::class.java) ?: ""
                ),
                userProfile = UserProfile(
                    initialLevel = initialLevel,
                    emailNotification = child("userProfile/emailNotification").getValue(Boolean::class.java) ?: false,
                    dailyReminder = child("userProfile/dailyReminder").getValue(Boolean::class.java) ?: false,
                    spacedRepetition = child("userProfile/spacedRepetition").getValue(Boolean::class.java) ?: false,
                    avatarIndex = child("userProfile/avatarIndex").getValue(Int::class.java) ?: 0,
                    wordsLearned = child("userProfile/wordsLearned").getValue(Int::class.java) ?: 0,
                    streak = child("userProfile/streak").getValue(Int::class.java) ?: 0,
                    tags = learningGoals
                ),
                userSetting = UserSetting(
                    dailyNewWordGoal = child("userSetting/dailyNewWordGoal").getValue(Int::class.java) ?: 0,
                    dailyReviewGoal = child("userSetting/dailyReviewGoal").getValue(Int::class.java) ?: 0
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    // Lấy danh sách Deck theo userId: deck của user lên đầu, tiếp theo là public decks
    suspend fun getDecks(userId: String): List<Deck> {
        return try {
            val snapshot = database.getReference("decks").get().await()
            if (!snapshot.exists()) {
                Log.d("FirebaseDB", "No decks found in database")
                return emptyList()
            }

            Log.d("FirebaseDB", "Found ${snapshot.childrenCount} decks, filtering for userId=$userId")

            val allDecks = snapshot.children.mapNotNull { child ->
                try {
                    val nodeKey = child.key ?: ""
                    val id = child.child("id").getValue(String::class.java) ?: nodeKey
                    val deckName = child.child("deckName").getValue(String::class.java) ?: ""
                    val description = child.child("description").getValue(String::class.java) ?: ""
                    val createId = child.child("createId").getValue(String::class.java) ?: ""
                    val isPublic = child.child("isPublic").getValue(Boolean::class.java) ?: false

                    // Chỉ lấy deck của user hiện tại HOẶC deck public
                    if (createId != userId && !isPublic) {
                        return@mapNotNull null
                    }

                    Log.d("FirebaseDB", "Mapping deck: $id (Key: $nodeKey, createId: $createId, isPublic: $isPublic)")

                    // Xử lý danh sách Tags Enum an toàn
                    val tagsList = mutableListOf<LearningGoal>()
                    child.child("tags").children.forEach { tagSnapshot ->
                        tagSnapshot.getValue(String::class.java)?.let { tagStr ->
                            try {
                                tagsList.add(LearningGoal.valueOf(tagStr.uppercase()))
                            } catch (_: Exception) {}
                        }
                    }

                    // Parse vocabularyIds from "vocabularies" field in the deck
                    val vocabularyIds = mutableListOf<String>()
                    child.child("vocabularies").children.forEach { vIdSnapshot ->
                        try {
                            val idVal = vIdSnapshot.getValue(String::class.java)
                            if (idVal != null) {
                                vocabularyIds.add(idVal)
                            } else {
                                vIdSnapshot.key?.let { vocabularyIds.add(it) }
                            }
                        } catch (e: Exception) {
                            vIdSnapshot.key?.let { vocabularyIds.add(it) }
                        }
                    }

                    Deck(
                        id = id,
                        name = deckName,
                        description = description,
                        tags = tagsList,
                        createId = createId,
                        vocabularyIds = vocabularyIds,
                        isPublic = isPublic,
                        totalWords = child.child("totalWords").getValue(Int::class.java) ?: vocabularyIds.size
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseDB", "Error mapping deck child: ${e.message}")
                    null
                }
            }

            // Sắp xếp: deck của user hiện tại lên đầu, sau đó là public decks
            val userDecks = allDecks.filter { it.createId == userId }
            val publicDecks = allDecks.filter { it.createId != userId }
            userDecks + publicDecks
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error fetching decks: ${e.message}")
            emptyList()
        }
    }

    // Tìm từ vựng theo tên từ và loại từ trong toàn bộ deck_vocabularies/deck_minlish_01
    suspend fun searchVocabularyByWord(word: String, partOfSpeech: String): Vocabulary? {
        return try {
            val snapshot = database.getReference("deck_vocabularies/deck_minlish_01").get().await()
            if (!snapshot.exists()) return null

            val wordLower = word.trim().lowercase()
            val posLower = partOfSpeech.trim().lowercase()
            snapshot.children.firstOrNull { child ->
                val matchWord = child.child("word").getValue(String::class.java)?.lowercase() == wordLower
                val childPos = child.child("pos").getValue(String::class.java)?.lowercase() ?: ""
                val matchPos = childPos == posLower || (posLower == "verb" && childPos == "v") || (posLower == "noun" && childPos == "n") || (posLower == "adjective" && childPos == "adj") || (posLower == "adverb" && childPos == "adv")
                matchWord && matchPos
            }?.let { child ->
                val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                val examples = mutableListOf<String>()
                val exampleNode = child.child("example")
                if (exampleNode.hasChildren()) {
                    exampleNode.children.forEach { ex -> ex.getValue(String::class.java)?.let { examples.add(it) } }
                } else {
                    exampleNode.getValue(String::class.java)?.let { examples.add(it) }
                }
                Vocabulary(
                    id = id,
                    deckId = "",  // từ vựng hệ thống, không gắn với deck cụ thể
                    word = child.child("word").getValue(String::class.java) ?: "",
                    phonetic = child.child("pronunciation").getValue(String::class.java) ?: "",
                    partOfSpeech = child.child("pos").getValue(String::class.java) ?: "",
                    soundUrl = child.child("voiceUrl").getValue(String::class.java) ?: "",
                    level = child.child("level").getValue(String::class.java),
                    englishDefinition = child.child("descriptionEnglish").getValue(String::class.java) ?: "",
                    vietnameseMeaning = child.child("meaning").getValue(String::class.java) ?: "",
                    context = examples.joinToString("\n"),
                    note = child.child("note").getValue(String::class.java) ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error searching vocabulary by word '$word': ${e.message}")
            null
        }
    }

    // Lưu deck do người dùng tạo lên Firebase
    suspend fun saveDeck(deck: Deck) {
        try {
            val deckData = mapOf(
                "id" to deck.id,
                "deckName" to deck.name,
                "description" to deck.description,
                "isPublic" to deck.isPublic,
                "createId" to deck.createId,
                "tags" to deck.tags.map { it.name }
            )
            database.getReference("decks").child(deck.id).setValue(deckData).await()
            Log.d("FirebaseDB", "Saved deck: ${deck.id}")
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error saving deck: ${e.message}")
        }
    }

    // Thêm từ vựng vào deck do người dùng tạo trên Firebase
    suspend fun saveVocabularyToDeck(deckId: String, vocabulary: Vocabulary) {
        try {
            val vocabData = mapOf(
                "id" to vocabulary.id,
                "word" to vocabulary.word,
                "meaning" to vocabulary.vietnameseMeaning,
                "descriptionEnglish" to vocabulary.englishDefinition,
                "pronunciation" to vocabulary.phonetic,
                "pos" to vocabulary.partOfSpeech,
                "voiceUrl" to vocabulary.soundUrl,
                "note" to vocabulary.note,
                "example" to vocabulary.example,
                "deckId" to vocabulary.deckId,
                "level" to (vocabulary.level ?: "")
            )
            database.getReference("decks").child(deckId)
                .child("vocabularies").child(vocabulary.id)
                .setValue(vocabData).await()
            Log.d("FirebaseDB", "Saved vocabulary '${vocabulary.word}' to deck $deckId")
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error saving vocabulary to deck: ${e.message}")
        }
    }

    suspend fun getVocabularyByDeck(deckId: String, vocabularyIds: List<String>): List<Vocabulary> {
        // Check if there are inline vocabularies under decks/$deckId/vocabularies first
        val inlineSnapshot = try {
            database.getReference("decks").child(deckId).child("vocabularies").get().await()
        } catch (e: Exception) {
            null
        }

        val useInline = inlineSnapshot != null && inlineSnapshot.exists() && inlineSnapshot.children.any { 
            it.hasChildren()
        }

        return try {
            val snapshot = if (useInline) {
                inlineSnapshot!!
            } else {
                // Fallback to deck_vocabularies/$deckId, then deck_vocabularies/deck_minlish_01
                val path1 = "deck_vocabularies/$deckId"
                val s1 = try { database.getReference(path1).get().await() } catch (e: Exception) { null }
                if (s1 != null && s1.exists()) {
                    s1
                } else {
                    val path2 = "deck_vocabularies/deck_minlish_01"
                    database.getReference(path2).get().await()
                }
            }

            if (!snapshot.exists()) {
                Log.d("FirebaseDB", "No vocab found for deck: $deckId")
                return emptyList()
            }

            Log.d("FirebaseDB", "Total vocab in snapshot: ${snapshot.childrenCount}")

            snapshot.children.mapNotNull { child ->
                val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                
                // Chỉ lấy những từ có trong danh sách vocabularyIds của Deck
                if (vocabularyIds.isNotEmpty() && !vocabularyIds.contains(id)) {
                    return@mapNotNull null
                }

                try {
                    val word = child.child("word").getValue(String::class.java) ?: ""
                    val pronunciation = child.child("pronunciation").getValue(String::class.java) ?: ""
                    val meaning = child.child("meaning").getValue(String::class.java) ?: ""
                    val descriptionEnglish = child.child("descriptionEnglish").getValue(String::class.java) ?: ""
                    val note = child.child("note").getValue(String::class.java) ?: ""
                    val pos = child.child("pos").getValue(String::class.java) ?: ""
                    val level = child.child("level").getValue(String::class.java) ?: ""
                    val voiceUrl = child.child("voiceUrl").getValue(String::class.java) ?: ""

                    val examples = mutableListOf<String>()
                    val exampleNode = child.child("example")
                    if (exampleNode.hasChildren()) {
                        exampleNode.children.forEach { exSnapshot ->
                            exSnapshot.getValue(String::class.java)?.let { examples.add(it) }
                        }
                    } else {
                        exampleNode.getValue(String::class.java)?.let { examples.add(it) }
                    }

                    Vocabulary(
                        id = id,
                        deckId = deckId,
                        word = word,
                        phonetic = pronunciation,
                        vietnameseMeaning = meaning,
                        englishDefinition = descriptionEnglish,
                        context = examples.joinToString(separator = "\n"),
                        note = note,
                        partOfSpeech = pos,
                        level = level,
                        soundUrl = voiceUrl
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseDB", "Error parsing vocab $id: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error fetching vocab for deck $deckId: ${e.message}")
            emptyList()
        }
    }

    // Xóa deck do người dùng tạo
    suspend fun deleteDeck(deckId: String) {
        try {
            database.getReference("decks").child(deckId).removeValue().await()
            Log.d("FirebaseDB", "Deleted deck: $deckId")
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error deleting deck: ${e.message}")
        }
    }

    // Xóa từ vựng khỏi deck do người dùng tạo
    suspend fun deleteVocabularyFromDeck(deckId: String, vocabId: String) {
        try {
            database.getReference("decks").child(deckId)
                .child("vocabularies").child(vocabId).removeValue().await()
            Log.d("FirebaseDB", "Deleted vocab '$vocabId' from deck $deckId")
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error deleting vocabulary from deck: ${e.message}")
        }
    }

    suspend fun syncUserVocabularyState(userId: String, state: UserVocabularyState) {
        val srsData = mapOf(
            "vocabId" to state.vocabId,
            "deckId" to state.deckId,
            "interval" to state.interval,
            "repetition" to state.repetition,
            "easeFactor" to state.easeFactor,
            "nextReview" to mapOf(
                "time" to state.nextReview.time,
                "date" to state.nextReview.date,
                "day" to state.nextReview.day,
                "hours" to state.nextReview.hours,
                "minutes" to state.nextReview.minutes,
                "month" to state.nextReview.month,
                "seconds" to state.nextReview.seconds,
                "year" to state.nextReview.year,
                "timezoneOffset" to state.nextReview.timezoneOffset
            )
        )
        try {
            database.getReference("vocabularyStates").child(userId).child(state.vocabId).setValue(srsData).await()
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error syncing vocabulary state: ${e.message}")
        }
    }

    suspend fun getUserVocabularyState(userId: String, vocabId: String): UserVocabularyState? {
        return try {
            val snapshot = database.getReference("vocabularyStates").child(userId).child(vocabId).get().await()
            if (snapshot.exists()) {
                val nextReviewSnapshot = snapshot.child("nextReview")
                val nextReviewTime = if (nextReviewSnapshot.hasChild("time")) {
                    nextReviewSnapshot.child("time").getValue(Long::class.java) ?: System.currentTimeMillis()
                } else {
                    nextReviewSnapshot.getValue(Long::class.java) ?: System.currentTimeMillis()
                }

                UserVocabularyState(
                    vocabId = snapshot.child("vocabId").getValue(String::class.java) ?: vocabId,
                    deckId = snapshot.child("deckId").getValue(String::class.java) ?: "",
                    interval = snapshot.child("interval").getValue(Double::class.java) ?: 1.0,
                    repetition = snapshot.child("repetition").getValue(Int::class.java) ?: 0,
                    easeFactor = snapshot.child("easeFactor").getValue(Double::class.java) ?: 2.5,
                    nextReview = Date(nextReviewTime)
                )
            } else null
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error getting vocabulary state: ${e.message}")
            null
        }
    }

    suspend fun syncReviewHistory(userId: String, history: ReviewHistory) {
        val reviewId = history.id.ifBlank { database.getReference("reviewHistories").child(userId).push().key ?: UUID.randomUUID().toString() }
        
        val historyData = mapOf(
            "id" to reviewId,
            "vocabId" to history.vocabId,
            "rating" to history.rating.name,
            "learningTime" to mapOf(
                "time" to history.learningTime.time,
                // Firebase normally serializes Date as long, but if we want to match user's screenshot exactly:
                "date" to history.learningTime.date,
                "day" to history.learningTime.day,
                "hours" to history.learningTime.hours,
                "minutes" to history.learningTime.minutes,
                "month" to history.learningTime.month,
                "seconds" to history.learningTime.seconds,
                "year" to history.learningTime.year,
                "timezoneOffset" to history.learningTime.timezoneOffset
            )
        )
        
        try {
            database.getReference("reviewHistories").child(userId).child(reviewId).setValue(historyData).await()
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error syncing review history: ${e.message}")
        }
    }

    suspend fun getUserProgress(userId: String): com.minlish.app.presentation.dashboard.model.UserProgress? {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            if (snapshot.exists()) {
                val wordsLearned = snapshot.child("wordsLearned").getValue(Int::class.java) 
                    ?: snapshot.child("userProfile/wordsLearned").getValue(Int::class.java) 
                    ?: 0
                val streak = snapshot.child("streak").getValue(Int::class.java)
                    ?: snapshot.child("userProfile/streak").getValue(Int::class.java) 
                    ?: 0
                
                val currentLevel = calculateLevel(wordsLearned)
                val progressPercent = ((wordsLearned % 1000) / 10)
                
                val statesSnapshot = database.getReference("vocabularyStates").child(userId).get().await()
                var totalStatesCount = 0
                var retainedStatesCount = 0
                if (statesSnapshot.exists()) {
                    for (stateChild in statesSnapshot.children) {
                        totalStatesCount++
                        val ease = stateChild.child("easeFactor").getValue(Double::class.java) ?: 2.5
                        if (ease >= 2.0) {
                            retainedStatesCount++
                        }
                    }
                }
                val retentionRate = if (totalStatesCount > 0) {
                    ((retainedStatesCount.toFloat() / totalStatesCount) * 100).toInt().coerceAtMost(100)
                } else {
                    85
                }

                com.minlish.app.presentation.dashboard.model.UserProgress(
                    currentLevelName = mapLevelToName(currentLevel),
                    levelProgress = progressPercent,
                    nextLevelName = getNextLevelName(currentLevel),
                    wordsCount = wordsLearned,
                    streak = streak,
                    retentionRate = retentionRate
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error getting user progress: ${e.message}")
            null
        }
    }

    suspend fun saveUserProgress(userId: String, progress: com.minlish.app.presentation.dashboard.model.UserProgress) {
        try {
            val updates = mapOf(
                "wordsLearned" to progress.wordsCount,
                "streak" to progress.streak,
                "userProfile/wordsLearned" to progress.wordsCount,
                "userProfile/streak" to progress.streak
            )
            usersRef.child(userId).updateChildren(updates).await()
            Log.d("FirebaseDB", "Saved user progress successfully")
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error saving user progress: ${e.message}")
        }
    }

    suspend fun getReviewHistories(userId: String): List<ReviewHistory>? {
        return try {
            val snapshot = database.getReference("reviewHistories").child(userId).get().await()
            if (snapshot.exists()) {
                snapshot.children.mapNotNull { child ->
                    try {
                        val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                        val vocabId = child.child("vocabId").getValue(String::class.java) ?: ""
                        val ratingStr = child.child("rating").getValue(String::class.java) ?: "GOOD"
                        val rating = try { EaseFactor.valueOf(ratingStr) } catch (e: Exception) { EaseFactor.GOOD }
                        
                        val timeSnapshot = child.child("learningTime")
                        val timeLong = if (timeSnapshot.hasChild("time")) {
                            timeSnapshot.child("time").getValue(Long::class.java) ?: System.currentTimeMillis()
                        } else {
                            timeSnapshot.getValue(Long::class.java) ?: System.currentTimeMillis()
                        }
                        
                        ReviewHistory(
                            id = id,
                            vocabId = vocabId,
                            learningTime = Date(timeLong),
                            rating = rating
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error getting review histories: ${e.message}")
            null
        }
    }

    suspend fun saveReviewHistory(userId: String, history: ReviewHistory) {
        try {
            val key = history.id.ifBlank { database.getReference("reviewHistories").child(userId).push().key ?: java.util.UUID.randomUUID().toString() }
            val historyData = mapOf(
                "id" to key,
                "vocabId" to history.vocabId,
                "rating" to history.rating.name,
                "learningTime" to mapOf(
                    "time" to history.learningTime.time,
                    "date" to history.learningTime.date,
                    "day" to history.learningTime.day,
                    "hours" to history.learningTime.hours,
                    "minutes" to history.learningTime.minutes,
                    "month" to history.learningTime.month,
                    "seconds" to history.learningTime.seconds,
                    "year" to history.learningTime.year,
                    "timezoneOffset" to history.learningTime.timezoneOffset
                )
            )
            database.getReference("reviewHistories").child(userId).child(key).setValue(historyData).await()
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error saving review history: ${e.message}")
        }
    }

    suspend fun initializeEverythingWithFullData(userId: String) {
        try {
            val userSnapshot = usersRef.child(userId).get().await()
            if (!userSnapshot.exists()) {
                val userData = mapOf(
                    "id" to userId,
                    "name" to "Bunny Learner",
                    "userProfile" to mapOf(
                        "initialLevel" to "B1",
                        "wordsLearned" to 0,
                        "streak" to 0,
                        "avatarIndex" to 1
                    ),
                    "userSetting" to mapOf(
                        "dailyNewWordGoal" to 10,
                        "dailyReviewGoal" to 30
                    ),
                    "wordsLearned" to 0,
                    "streak" to 0
                )
                usersRef.child(userId).setValue(userData).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error initializing database: ${e.message}")
        }
    }

    suspend fun getDailyPlanTelemetry(userId: String): com.minlish.app.presentation.dashboard.model.DailyPlanTelemetry {
        return try {
            val statesSnapshot = database.getReference("vocabularyStates").child(userId).get().await()
            val learnedVocabIds = mutableSetOf<String>()
            var reviewsCount = 0

            val nowMs = System.currentTimeMillis()
            val todayEndCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val todayEndMs = todayEndCal.timeInMillis

            if (statesSnapshot.exists()) {
                for (stateChild in statesSnapshot.children) {
                    val vocabId = stateChild.child("vocabId").getValue(String::class.java) ?: stateChild.key ?: ""
                    if (vocabId.isNotEmpty()) {
                        learnedVocabIds.add(vocabId)
                        
                        val nextReviewSnapshot = stateChild.child("nextReview")
                        val nextReviewTime = if (nextReviewSnapshot.hasChild("time")) {
                            nextReviewSnapshot.child("time").getValue(Long::class.java) ?: nowMs
                        } else {
                            nextReviewSnapshot.getValue(Long::class.java) ?: nowMs
                        }

                        if (nextReviewTime <= todayEndMs) {
                            reviewsCount++
                        }
                    }
                }
            }

            val decksSnapshot = database.getReference("decks").get().await()
            var newWordsCount = 0

            if (decksSnapshot.exists()) {
                for (deckChild in decksSnapshot.children) {
                    val createId = deckChild.child("createId").getValue(String::class.java) ?: ""
                    val isPublic = deckChild.child("isPublic").getValue(Boolean::class.java) ?: false
                    
                    if (createId == userId || isPublic) {
                        val deckVocabIds = mutableSetOf<String>()
                        deckChild.child("vocabularies").children.forEach { vChild ->
                            val vId = vChild.child("id").getValue(String::class.java) ?: vChild.key ?: ""
                            if (vId.isNotEmpty()) {
                                deckVocabIds.add(vId)
                            }
                        }

                        val learnedInDeck = deckVocabIds.filter { it in learnedVocabIds }.size

                        if (learnedInDeck > 0 && learnedInDeck < deckVocabIds.size) {
                            val remainingInDeck = deckVocabIds.size - learnedInDeck
                            newWordsCount += remainingInDeck
                        }
                    }
                }
            }

            com.minlish.app.presentation.dashboard.model.DailyPlanTelemetry(
                newWordsCount = newWordsCount,
                reviewWordsCount = reviewsCount
            )
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error getting daily plan telemetry: ${e.message}")
            com.minlish.app.presentation.dashboard.model.DailyPlanTelemetry(0, 0)
        }
    }

    private fun calculateLevel(wordsLearned: Int): String {
        return when {
            wordsLearned < 1000 -> "A1"
            wordsLearned < 2000 -> "A2"
            wordsLearned < 3000 -> "B1"
            wordsLearned < 4000 -> "B2"
            wordsLearned < 5000 -> "C1"
            else -> "C2"
        }
    }

    private fun mapLevelToName(level: String): String {
        return when(level.uppercase()) {
            "A1" -> "Người Mới Bắt Đầu"
            "A2" -> "Sơ Cấp"
            "B1" -> "Trung Cấp"
            "B2" -> "Trung Cao Cấp"
            "C1" -> "Cao Cấp"
            "C2" -> "Thành Thạo"
            else -> "Người Mới Bắt Đầu"
        }
    }

    private fun getNextLevelName(level: String): String {
        return when(level.uppercase()) {
            "A1" -> "Sơ Cấp (A2)"
            "A2" -> "Trung Cấp (B1)"
            "B1" -> "Trung Cao Cấp (B2)"
            "B2" -> "Cao Cấp (C1)"
            "C1" -> "Thành Thạo (C2)"
            "C2" -> "Hoàn thành"
            else -> "Sơ Cấp (A2)"
        }
    }
}
