package com.minlish.app.domain.model

import com.minlish.app.enumration.InitialLevel

data class Vocabulary(
    val id: String = "",
    val deckId: String = "",
    val word: String = "",
    val phonetic: String = "",
    val partOfSpeech: String = "",
    val soundUrl: String = "",
    val level: InitialLevel? = null,
    val englishDefinition: String = "",
    val vietnameseMeaning: String = "",
    val context: String = "" // Examples
)
