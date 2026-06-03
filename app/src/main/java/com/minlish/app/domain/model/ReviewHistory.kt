package com.minlish.app.domain.model


import com.minlish.app.domain.model.enumration.EaseFactor
import java.util.Date

data class ReviewHistory(
    val id: String = "",
    val vocabId: String = "",
    val learningTime: Date = Date(),
    val rating: EaseFactor = EaseFactor.GOOD
)