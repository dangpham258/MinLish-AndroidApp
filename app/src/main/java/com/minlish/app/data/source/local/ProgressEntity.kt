package com.minlish.app.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deck_progress")
data class DeckProgressEntity(
    @PrimaryKey
    val deckId: String,
    val learnedCount: Int,
    val totalCount: Int,
    val lastLearnedId: String? = null
)
