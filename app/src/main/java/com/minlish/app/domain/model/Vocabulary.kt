package com.minlish.app.domain.model

data class Vocabulary(
    // V1 fields (Must be first for positional arguments compatibility)
    val id: String = "",
    val deckId: String = "",
    val word: String = "",
    val phonetic: String = "",
    val partOfSpeech: String = "",
    val soundUrl: String = "",
    val level: String? = null,
    val englishDefinition: String = "",
    val vietnameseMeaning: String = "",
    val context: String = "", // Used as example/context
    
    // Remaining unique V2 fields
    val note: String = ""
) {
    // Computed properties for UI compatibility
    val wordType: String get() {
        val lvl = level ?: ""
        return if (lvl.isNotBlank() && partOfSpeech.isNotBlank()) "$lvl - $partOfSpeech" else lvl + partOfSpeech
    }

    // Aliases for backward compatibility with older UI code
    val pronunciation: String get() = phonetic
    val voiceUrl: String get() = soundUrl
    val meaning: String get() = vietnameseMeaning
    val descriptionEnglish: String get() = englishDefinition
    val example: List<String> get() = if (context.isBlank()) emptyList() else listOf(context)
}
