package com.minlish.app.domain.model

data class Vocabulary(
    val id: String = "",
    val deckId: String = "",
    val word: String = "",
    val phonetic: String = "",
    val partOfSpeech: String = "",
    val soundUrl: String = "",
    val level: String? = null,
    val englishDefinition: String = "",
    val vietnameseMeaning: String = "",
    val context: String = "",
    val note: String = ""
) {
    val wordType: String get() {
        val lvl = level ?: ""
        return if (lvl.isNotBlank() && partOfSpeech.isNotBlank()) "$lvl - $partOfSpeech" else lvl + partOfSpeech
    }

    val pronunciation: String get() = phonetic
    val voiceUrl: String get() = soundUrl
    val meaning: String get() = vietnameseMeaning
    val descriptionEnglish: String get() = englishDefinition
    val example: List<String> get() = if (context.isBlank()) emptyList() else listOf(context)
}
