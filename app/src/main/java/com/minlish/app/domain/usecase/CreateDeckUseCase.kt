package com.minlish.app.domain.usecase

import com.minlish.app.data.source.remote.FirebaseAuthApi
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.enumration.LearningGoal
import com.minlish.app.domain.repository.BunnyRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case chịu trách nhiệm toàn bộ business logic tạo Deck mới:
 * - Lấy ID người dùng hiện tại từ FirebaseAuthApi.
 * - Convert danh sách tag string sang List<LearningGoal>.
 * - Tạo UUID cho Deck.
 * - Gọi repository để lưu lên Firebase.
 *
 * Tuân thủ Clean Architecture: ViewModel không còn chứa logic này.
 */
class CreateDeckUseCase @Inject constructor(
    private val repository: BunnyRepository,
    private val authApi: FirebaseAuthApi
) {
    /**
     * @param name Tên bộ từ vựng (bắt buộc, không được rỗng).
     * @param description Mô tả bộ từ vựng.
     * @param tags Danh sách tên enum [LearningGoal] dưới dạng String (ví dụ: "IELTS", "TOEIC").
     * @return [Deck] đã được tạo, dùng để Optimistic Update ngay trên UI.
     */
    suspend operator fun invoke(
        name: String,
        description: String,
        tags: List<String>
    ): Deck {
        val currentUserId = authApi.getCurrentUser()?.uid ?: ""
        val goalTags = tags.mapNotNull { tag ->
            try {
                LearningGoal.valueOf(tag.uppercase())
            } catch (e: Exception) {
                null
            }
        }
        val newDeck = Deck(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            tags = goalTags,
            createId = currentUserId,
            isPublic = false
        )
        repository.insertDeck(newDeck)
        return newDeck
    }
}
