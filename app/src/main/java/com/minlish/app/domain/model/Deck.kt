package com.minlish.app.domain.model

import com.minlish.app.domain.model.enumration.LearningGoal

data class Deck(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val tags: List<LearningGoal> = emptyList(),
    val createId: String = "",
    val vocabularyIds: List<String> = emptyList(),
    @field:JvmField
    val isPublic: Boolean = false,
    val learned: Int = 0,
    val streak: Int = 0,
    val totalWords: Int = 0
) {
    // Alias for deckName if used in some places
    val deckName: String get() = name
}
