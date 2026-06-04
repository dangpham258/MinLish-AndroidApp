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
        val quality = when (buttonPressed) {
            EaseFactor.AGAIN -> 0
            EaseFactor.HARD -> 3
            EaseFactor.GOOD -> 4
            EaseFactor.EASY -> 5
        }

        val newRepetition: Int
        var newInterval: Double
        var newEaseFactor: Double

        if (quality >= 3) {
            // Trả lời đúng
            if (currentState.repetition == 0) {
                // Lần đầu học: Phân loại khoảng cách dựa trên nút bấm
                newInterval = when (buttonPressed) {
                    EaseFactor.HARD -> 1.0
                    EaseFactor.GOOD -> 2.0
                    EaseFactor.EASY -> 4.0
                    else -> 1.0
                }
                newRepetition = 1
            } else if (currentState.repetition == 1) {
                // Lần thứ hai học
                newInterval = when (buttonPressed) {
                    EaseFactor.EASY -> 6.0
                    else -> 4.0
                }
                newRepetition = 2
            } else {
                // Từ lần thứ 3 trở đi: Tính theo công suất Ease Factor
                newInterval = (currentState.interval * currentState.easeFactor).roundToInt().toDouble()
                    .coerceAtLeast(currentState.interval + 1)
                newRepetition = currentState.repetition + 1
            }

            // Cập nhật Ease Factor (SM-2 Formula)
            newEaseFactor = currentState.easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        } else {
            // Trả lời sai (Again)
            newRepetition = 0
            newInterval = 1.0
            newEaseFactor = currentState.easeFactor
        }

        if (newEaseFactor < 1.3) newEaseFactor = 1.3

        val millisecondInterval = (newInterval * 24 * 60 * 60 * 1000L).toLong()
        val nextReviewTimestamp = currentTime + millisecondInterval

        return currentState.copy(
            repetition = newRepetition,
            interval = newInterval,
            easeFactor = newEaseFactor,
            nextReview = Date(nextReviewTimestamp)
        )
    }

    fun estimateNextIntervalDays(
        currentState: UserVocabularyState,
        button: EaseFactor
    ): String {
        val nextState = invoke(currentState, button)
        val days = nextState.interval.roundToInt()
        return if (days <= 1) "1 day" else "$days days"
    }
}
