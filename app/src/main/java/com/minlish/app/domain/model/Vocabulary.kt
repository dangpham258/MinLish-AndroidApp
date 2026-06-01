package com.minlish.app.domain.model

data class Vocabulary(
    val id: String = "",
    val word: String = "",
    val pronunciation: String = "",
    val meaning: String = "",
    val descriptionEnglish: String = "",
    val example: List<String> = emptyList(),
    val note: String = "",
    val pos: String = "",
    val level: String = "",
    val voiceUrl: String = ""
) {
    // Computed properties for UI compatibility
    val wordType: String get() = if (level.isNotBlank() && pos.isNotBlank()) "$level - $pos" else level + pos
}
