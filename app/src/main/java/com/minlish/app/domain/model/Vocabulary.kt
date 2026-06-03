package com.minlish.app.domain.model

data class Vocabulary(
    val id: String = "",
    val word: String = "",
    val level: String = "",
    val pos: String = "",
    val pronunciation: String = "",
    val voiceUrl: String = "",
    val meaning: String = "",
    val descriptionEnglish: String = "",
    val example: List<String> = emptyList(),
    val note: String = ""
)
