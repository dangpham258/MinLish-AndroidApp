package com.minlish.app.core.database

import android.content.Context
import android.util.Log
import java.util.UUID
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.model.UserVocabularyState
import com.minlish.app.domain.model.ReviewHistory
import com.minlish.app.domain.model.enumration.LearningGoal
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseService(context: Context) {

    private val database: FirebaseDatabase? by lazy {
        try {
            val apps = FirebaseApp.getApps(context)
            val app = if (apps.isEmpty()) null else FirebaseApp.getInstance()

            if (app != null) {
                FirebaseDatabase.getInstance(app, "https://minlish-1e2ec-default-rtdb.asia-southeast1.firebasedatabase.app/").apply {
                    setPersistenceEnabled(false)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Failed to initialize FirebaseDatabase: ${e.message}")
            null
        }
    }

    // Lấy danh sách Deck trực tiếp từ nút "decks"
    suspend fun getDecks(): List<Deck> {
        val db = database ?: return emptyList()
        return try {
            val snapshot = db.getReference("decks").get().await()
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
                        vIdSnapshot.getValue(String::class.java)?.let { vocabularyIds.add(it) }
                    }

                    Deck(
                        id = id,
                        name = deckName,
                        description = description,
                        tags = tagsList,
                        createId = createId,
                        vocabularyIds = vocabularyIds,
                        isPublic = isPublic
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

    // Truy xuất chuẩn từ nút "deck_vocabularies/deck_minlish_01" và lọc theo danh sách ID
    suspend fun getVocabularyByDeck(deckId: String, vocabularyIds: List<String>): List<Vocabulary> {
        val db = database ?: return emptyList()
        
        // Theo yêu cầu của user, dữ liệu nằm trong deck_vocabularies/deck_minlish_01
        val path = "deck_vocabularies/deck_minlish_01"
        Log.d("FirebaseDB", "Fetching vocab from path: $path for ${vocabularyIds.size} IDs")
        
        return try {
            val snapshot = db.getReference(path).get().await()
            if (!snapshot.exists()) {
                Log.d("FirebaseDB", "No vocab found at $path")
                return emptyList()
            }

            Log.d("FirebaseDB", "Total vocab in $path: ${snapshot.childrenCount}")

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
            Log.e("FirebaseDB", "Error fetching vocab from $path: ${e.message}")
            emptyList()
        }
    }

    suspend fun syncUserVocabularyState(userId: String, state: UserVocabularyState) {
        val db = database ?: return
        val srsData = mapOf(
            "vocabId" to state.vocabId,
            "deckId" to state.deckId,
            "interval" to state.interval,
            "repetition" to state.repetition,
            "easeFactor" to state.easeFactor.name,
            "nextReview" to state.nextReview.time
        )
        try {
            db.getReference("vocabularyStates").child(userId).child(state.vocabId).setValue(srsData).await()
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error syncing vocabulary state: ${e.message}")
        }
    }

    suspend fun syncReviewHistory(userId: String, history: ReviewHistory) {
        val db = database ?: return
        val reviewId = history.id.ifBlank { db.getReference("reviewHistories").child(userId).push().key ?: UUID.randomUUID().toString() }
        
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
            db.getReference("reviewHistories").child(userId).child(reviewId).setValue(historyData).await()
        } catch (e: Exception) {
            Log.e("FirebaseDB", "Error syncing review history: ${e.message}")
        }
    }
}