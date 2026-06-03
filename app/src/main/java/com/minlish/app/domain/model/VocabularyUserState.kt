package com.minlish.app.domain.model


import com.minlish.app.domain.model.enumration.EaseFactor
import java.util.Date
import java.util.Calendar

data class UserVocabularyState(
    val id: String = "",
    val vocabId: String = "",
    val deckId: String = "",
    val interval: Double = 1.0,
    val repetition: Int = 0,
    val easeFactor: EaseFactor = EaseFactor.GOOD,
    val nextReview: Date = Date()
)