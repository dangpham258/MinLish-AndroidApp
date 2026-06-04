package com.minlish.app.presentation.deck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.repository.BunnyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn hình [com.minlish.app.presentation.deck.ui.DeckDetailScreen].
 * Chịu trách nhiệm:
 * - Tải thông tin chi tiết của một Deck theo [deckId].
 * - Quan sát danh sách từ vựng trong Deck.
 * - Xóa từ vựng khỏi Deck cá nhân (Optimistic Update).
 */
@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val repository: BunnyRepository
) : ViewModel() {

    private val _currentDeck = MutableStateFlow<Deck?>(null)
    val currentDeck: StateFlow<Deck?> = _currentDeck.asStateFlow()

    private val _words = MutableStateFlow<List<Vocabulary>>(emptyList())
    val words: StateFlow<List<Vocabulary>> = _words.asStateFlow()

    /**
     * Tải chi tiết Deck và danh sách từ vựng.
     * Reset state về null/rỗng trước khi tải để tránh hiển thị dữ liệu cũ
     * của Deck trước đó trong navigation stack.
     */
    fun loadDeckDetails(deckId: String) {
        _currentDeck.value = null
        _words.value = emptyList()

        viewModelScope.launch {
            val deck = repository.getDeckById(deckId)
            _currentDeck.value = deck

            if (deck != null) {
                // Deck cá nhân chưa có từ nào → trả về danh sách rỗng ngay, không fallback
                if (!deck.isPublic && deck.vocabularyIds.isEmpty()) {
                    _words.value = emptyList()
                    return@launch
                }
                repository.getVocabularyByDeck(deckId, deck.vocabularyIds).collectLatest { vocabList ->
                    _words.value = vocabList
                }
            }
        }
    }

    fun getDeckProgress(deckId: String): Flow<Int> = repository.getProgress(deckId)

    /**
     * Xóa từ vựng khỏi Deck cá nhân.
     * Từ vựng chỉ bị xóa khỏi tham chiếu trong Deck; dữ liệu gốc trên Firebase vẫn được bảo toàn.
     * Dùng Optimistic Update để UI phản hồi tức thì.
     */
    fun deleteVocabulary(deckId: String, vocabId: String) {
        _words.value = _words.value.filter { it.id != vocabId }
        viewModelScope.launch {
            repository.deleteVocabulary(deckId, vocabId)
        }
    }
}
