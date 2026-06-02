package com.minlish.app.domain.usecase

import com.minlish.app.domain.model.enumration.EaseFactor
import com.minlish.app.domain.model.UserVocabularyState
import java.util.Date
import kotlin.math.roundToInt

class CalculateSrsUseCase {

    operator fun invoke(
        currentState: UserVocabularyState,
        buttonPressed: EaseFactor,
        currentTime: Long = System.currentTimeMillis()
    ): UserVocabularyState {
        val newRepetition: Int
        val newIntervalDays: Double
        
        val multiplier = when (buttonPressed) {
            EaseFactor.AGAIN -> 0.0
            EaseFactor.HARD -> 1.2
            EaseFactor.GOOD -> 1.5
            EaseFactor.EASY -> 2.0
        }

        if (buttonPressed == EaseFactor.AGAIN) {
            newRepetition = 0
            newIntervalDays = 1.0
        } else {
            newRepetition = currentState.repetition + 1
            newIntervalDays = when (newRepetition) {
                1 -> 1.0
                2 -> 6.0
                else -> (currentState.interval * multiplier).roundToInt().toDouble().coerceAtLeast(currentState.interval + 1)
            }
        }

        val millisecondInterval = (newIntervalDays * 24 * 60 * 60 * 1000L).toLong()
        val nextReviewTimestamp = currentTime + millisecondInterval

        return currentState.copy(
            repetition = newRepetition,
            interval = newIntervalDays,
            easeFactor = buttonPressed,
            nextReview = Date(nextReviewTimestamp)
        )
    }

    fun estimateNextIntervalDays(
        currentState: UserVocabularyState,
        button: EaseFactor
    ): String {
        val nextState = invoke(currentState, button)
        val days = nextState.interval.roundToInt()
        return if (button == EaseFactor.AGAIN) "1 ngày" else "$days ngày"
    }
}
