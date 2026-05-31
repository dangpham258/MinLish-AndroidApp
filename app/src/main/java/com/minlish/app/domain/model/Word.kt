package com.minlish.app.domain.model

data class Word(
    val id: String = "",
    val deckId: String = "",
    val word: String = "",
    val phonetic: String = "",
    val partOfSpeech: String = "",
    val englishDefinition: String = "",
    val vietnameseMeaning: String = "",
    val context: String = "" // Examples
)
