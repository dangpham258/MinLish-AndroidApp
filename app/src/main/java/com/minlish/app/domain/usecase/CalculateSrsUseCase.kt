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
            // Trả lời đúng (HARD, GOOD, EASY)
            if (currentState.repetition == 0) {
                // Lần đầu học thành công: Phân loại khoảng cách dựa trên nút bấm
                newInterval = when (buttonPressed) {
                    EaseFactor.HARD -> 1.0
                    EaseFactor.GOOD -> 2.0
                    EaseFactor.EASY -> 4.0
                    else -> 1.0
                }
                newRepetition = 1
            } else if (currentState.repetition == 1) {
                // Lần thứ hai học thành công
                newInterval = when (buttonPressed) {
                    EaseFactor.EASY -> 6.0
                    else -> 4.0
                }
                newRepetition = 2
            } else {
                // Từ lần thứ 3 trở đi: Tính theo công suất Ease Factor hiện tại
                newInterval = (currentState.interval * currentState.easeFactor).roundToInt().toDouble()
                    .coerceAtLeast(currentState.interval + 1)
                newRepetition = currentState.repetition + 1
            }

            // Cập nhật Ease Factor (SM-2 Formula) - tăng hoặc giảm nhẹ dựa trên nút bấm
            newEaseFactor = currentState.easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        } else {
            // Trả lời sai (AGAIN - quality < 3)
            // Khi trả lời sai, chúng ta reset repetition về 0 để người dùng học lại từ đầu chu kỳ.
            // Tuy nhiên, chúng ta GIẢM Ease Factor (thường là 0.2) để thuật toán "biết" đây là từ khó
            // và sẽ rút ngắn khoảng cách ôn tập ở các lần học tiếp theo (kể cả khi đã reset).
            newRepetition = 0
            newInterval = 1.0
            newEaseFactor = currentState.easeFactor - 0.2
        }

        // Đảm bảo Ease Factor không thấp hơn 1.3 (giới hạn của SM-2)
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
