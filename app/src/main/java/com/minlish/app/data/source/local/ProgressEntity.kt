package com.minlish.app.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deck_progress")
data class DeckProgressEntity(
    @PrimaryKey
    val id: String, // format: userId_deckId
    val userId: String,
    val deckId: String,
    val learnedCount: Int,
    val totalCount: Int,
    val learnedVocabularyIds: String = "", // Danh sách ID cách nhau bằng dấu phẩy
    val lastLearnedId: String? = null
)
