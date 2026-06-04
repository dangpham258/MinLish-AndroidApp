package com.minlish.app.presentation.deck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.AutoFillWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn hình [com.minlish.app.presentation.deck.ui.AddUpdateWordScreen].
 * Chịu trách nhiệm:
 * - Tải từ vựng hiện có khi ở chế độ Update.
 * - Tự động điền thông tin từ vựng từ Firebase hoặc API ngoài (Auto-fill).
 * - Lưu từ vựng mới hoặc cập nhật từ vựng hiện có vào Deck.
 */
@HiltViewModel
class WordEditorViewModel @Inject constructor(
    private val repository: BunnyRepository,
    private val autoFillWordUseCase: AutoFillWordUseCase
) : ViewModel() {

    private val _words = MutableStateFlow<List<Vocabulary>>(emptyList())
    val words: StateFlow<List<Vocabulary>> = _words.asStateFlow()

    private val _isSearchingAPI = MutableStateFlow(false)
    val isSearchingAPI: StateFlow<Boolean> = _isSearchingAPI.asStateFlow()

    /**
     * Tải danh sách từ vựng của Deck để màn hình Update có thể tìm từ cần sửa.
     * Chỉ gọi khi đang ở chế độ Update và danh sách chưa được load.
     */
    fun loadWordsForDeck(deckId: String) {
        viewModelScope.launch {
            val deck = repository.getDeckById(deckId) ?: return@launch
            repository.getVocabularyByDeck(deckId, deck.vocabularyIds).collectLatest { list ->
                _words.value = list
            }
        }
    }

    /**
     * Tìm kiếm và tự động điền thông tin từ vựng.
     * Thứ tự ưu tiên: Firebase hệ thống → FreeDictionary API + Minhqnd API (song song).
     *
     * @param query Từ cần tìm kiếm.
     * @param partOfSpeech Loại từ (noun, verb, adjective...).
     * @param onResult Callback trả về [Vocabulary] nếu tìm thấy, null nếu không có kết quả.
     */
    fun searchWordToAutoFill(query: String, partOfSpeech: String, onResult: (Vocabulary?) -> Unit) {
        viewModelScope.launch {
            _isSearchingAPI.value = true
            val wordData = autoFillWordUseCase(query, partOfSpeech)
            _isSearchingAPI.value = false
            onResult(wordData)
        }
    }

    /** Thêm từ vựng mới vào Deck. */
    fun addWord(vocabulary: Vocabulary) {
        viewModelScope.launch {
            repository.insertVocabulary(vocabulary)
        }
    }

    /** Cập nhật thông tin từ vựng đã có trong Deck. */
    fun updateWord(vocabulary: Vocabulary) {
        viewModelScope.launch {
            repository.insertVocabulary(vocabulary)
        }
    }
}
