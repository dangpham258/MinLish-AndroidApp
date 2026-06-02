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
import java.util.UUID
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
                    "learningGoal" to emptyList<String>(),
                    "initialLevel" to InitialLevel.B1.value,
                    "emailNotification" to false,
                    "dailyReminder" to true,
                    "spacedRepetition" to true
                ),
                "userSetting" to mapOf(
                    "dailyNewWordGoal" to 10,
                    "dailyReviewGoal" to 50
                ),
                "avatarIndex" to 0,
                "wordsLearned" to 0,
                "streak" to 0
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
        val currentDailyReminder = snapshot.child("userProfile/dailyReminder").getValue(Boolean::class.java) ?: true
        val currentSpacedRepetition = snapshot.child("userProfile/spacedRepetition").getValue(Boolean::class.java) ?: true
        
        // UserSetting
        val currentDailyNewWordGoal = snapshot.child("userSetting/dailyNewWordGoal").getValue(Int::class.java) ?: 10
        val currentDailyReviewGoal = snapshot.child("userSetting/dailyReviewGoal").getValue(Int::class.java) ?: 50
        
        // Cap nhat account
        updates["account"] = mapOf(
            "email" to currentEmail,
            "password" to ""
        )
        
        // Cap nhat userProfile
        updates["userProfile"] = mapOf(
            "learningGoal" to currentLearningGoal,
            "initialLevel" to currentInitialLevel,
            "emailNotification" to currentEmailNotification,
            "dailyReminder" to currentDailyReminder,
            "spacedRepetition" to currentSpacedRepetition
        )
        
        // Cap nhat userSetting
        updates["userSetting"] = mapOf(
            "dailyNewWordGoal" to currentDailyNewWordGoal,
            "dailyReviewGoal" to currentDailyReviewGoal
        )
        
        // Cap nhat cac field khac
        updates["name"] = currentName
        updates["avatarIndex"] = currentAvatarIndex
        updates["wordsLearned"] = currentWordsLearned
        updates["streak"] = currentStreak
        
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
        return snapshot.toUser()
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

    suspend fun deleteUser(uid: String) {
        usersRef.child(uid).removeValue().await()
    }

    private fun DataSnapshot.toUser(): User? {
        return try {
            val learningGoalStrings = child("userProfile/learningGoal").children.mapNotNull {
                it.getValue(String::class.java)
            }
            val learningGoals = learningGoalStrings.mapNotNull { value ->
                LearningGoal.entries.find { it.value == value }
            }
            
            val initialLevelValue = child("userProfile/initialLevel").getValue(String::class.java) ?: "B1"
            val initialLevel = InitialLevel.entries.find { it.value == initialLevelValue } ?: InitialLevel.B1
            
            User(
                id = key ?: "",
                name = child("name").getValue(String::class.java) ?: "",
                account = Account(
                    email = child("account/email").getValue(String::class.java) ?: "",
                    password = child("account/password").getValue(String::class.java) ?: ""
                ),
                userProfile = UserProfile(
                    learningGoal = learningGoals,
                    initialLevel = initialLevel,
                    emailNotification = child("userProfile/emailNotification").getValue(Boolean::class.java) ?: false,
                    dailyReminder = child("userProfile/dailyReminder").getValue(Boolean::class.java) ?: true,
                    spacedRepetition = child("userProfile/spacedRepetition").getValue(Boolean::class.java) ?: true
                ),
                userSetting = UserSetting(
                    dailyNewWordGoal = child("userSetting/dailyNewWordGoal").getValue(Int::class.java) ?: 10,
                    dailyReviewGoal = child("userSetting/dailyReviewGoal").getValue(Int::class.java) ?: 50
                ),
                avatarIndex = child("avatarIndex").getValue(Int::class.java) ?: 0,
                wordsLearned = child("wordsLearned").getValue(Int::class.java) ?: 0,
                streak = child("streak").getValue(Int::class.java) ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }

    // Lấy danh sách Deck trực tiếp từ nút "decks"
    suspend fun getDecks(): List<Deck> {
        return try {
            val snapshot = database.getReference("decks").get().await()
            if (!snapshot.exists()) {
                Log.d("FirebaseDB", "No decks found in database")
                return emptyList()
            }

            Log.d("FirebaseDB", "Found ${snapshot.childrenCount} decks")

            snapshot.children.mapNotNull { child ->
                try {
                    val nodeKey = child.key ?: ""
                    // Ưu tiên lấy field "id", nếu không có thì lấy key của node (ví dụ: deck_level_A1)
                    val id = child.child("id").getValue(String::class.java) ?: nodeKey
                    Log.d("FirebaseDB", "Mapping deck: $id (Key: $nodeKey)")
                    val deckName = child.child("deckName").getValue(String::class.java) ?: ""
                    val description = child.child("description").getValue(String::class.java) ?: ""
                    val createId = child.child("createId").getValue(String::class.java) ?: ""
                    val isPublic = child.child("isPublic").getValue(Boolean::class.java) ?: false

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
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error fetching decks: ${e.message}")
            emptyList()
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

    suspend fun syncUserVocabularyState(userId: String, state: UserVocabularyState) {
        val srsData = mapOf(
            "vocabId" to state.vocabId,
            "deckId" to state.deckId,
            "interval" to state.interval,
            "repetition" to state.repetition,
            "easeFactor" to state.easeFactor.name,
            "nextReview" to state.nextReview.time
        )
        try {
            database.getReference("vocabularyStates").child(userId).child(state.vocabId).setValue(srsData).await()
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error syncing vocabulary state: ${e.message}")
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
}
