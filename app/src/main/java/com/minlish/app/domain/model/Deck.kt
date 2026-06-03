package com.minlish.app.domain.model

import com.minlish.app.domain.model.enumration.LearningGoal

data class Deck(
    val id: String = "",
    val deckName: String = "",
    val description: String = "",
    val tags: List<LearningGoal> = emptyList(),
    val createId: String? = "",
    @field:JvmField
    val isPublic: Boolean = false,
    val vocabularies: List<String> = emptyList()
)
