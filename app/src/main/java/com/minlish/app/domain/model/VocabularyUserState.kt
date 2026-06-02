package com.minlish.app.domain.model

import java.util.Date

data class UserVocabularyState(
    val vocabId: String = "",
    val deckId: String = "",
    val interval: Double = 1.0,
    val repetition: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReview: Date = Date()
)
