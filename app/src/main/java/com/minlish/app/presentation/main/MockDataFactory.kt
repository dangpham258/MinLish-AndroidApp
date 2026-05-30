package com.minlish.app.presentation.main

import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.LearningGoal

object MockDataFactory {
    fun createIeltsSpeakingDeckMock(): Deck {
        return Deck(
            id = 1,
            deckName = "IELTS Speaking Vocabulary",
            description = "Essential vocabulary and phrases for IELTS Speaking Success.",
            tags = listOf(LearningGoal.IELTS, LearningGoal.COMMUNICATION),
            createId = 1,
            isPublic = true,
        )
    }
}
