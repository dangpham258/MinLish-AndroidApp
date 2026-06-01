package com.minlish.app.domain.model
import com.minlish.app.domain.model.enumration.LearningGoal

data class Deck(
    val id: String = "",
    val deckName: String = "",
    val description: String = "",
    val tags: List<LearningGoal> = emptyList(),
    val createId: String = "",
    val vocabularyIds: List<String> = emptyList(),
    @field:JvmField
    val isPublic: Boolean = false
)