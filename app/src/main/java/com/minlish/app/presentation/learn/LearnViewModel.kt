package com.minlish.app.presentation.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minlish.app.domain.model.*
import com.minlish.app.domain.model.enumration.*
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.CalculateSrsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class LearnViewModel @Inject constructor(
    private val repository: BunnyRepository,
    private val calculateSrsUseCase: CalculateSrsUseCase
) : ViewModel() {

    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    open val decks: StateFlow<List<Deck>> = _decks.asStateFlow()

    private val _selectedDeck = MutableStateFlow<Deck?>(null)
    open val selectedDeck: StateFlow<Deck?> = _selectedDeck.asStateFlow()

    private val _vocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    open val vocabularies: StateFlow<List<Vocabulary>> = _vocabularies.asStateFlow()

    // Trạng thái Flashcard & Học tập
    private val _flashcards = MutableStateFlow<List<Vocabulary>>(emptyList())
    val flashcards: StateFlow<List<Vocabulary>> = _flashcards.asStateFlow()

    private val _dueVocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    val dueVocabularies: StateFlow<List<Vocabulary>> = _dueVocabularies.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _currentSrsIndex = MutableStateFlow(0)
    val currentSrsIndex: StateFlow<Int> = _currentSrsIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    private val _isSrsCardFlipped = MutableStateFlow(false)
    val isSrsCardFlipped: StateFlow<Boolean> = _isSrsCardFlipped.asStateFlow()

    // Missing properties for UI compatibility
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _learnedCount = MutableStateFlow(0)
    val learnedCount: StateFlow<Int> = _learnedCount.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    private val learnedIds = mutableSetOf<String>()

    init {
        observeDecks()
    }

    // THAY ĐỔI QUAN TRỌNG: Lắng nghe danh sách Deck thực tế từ Firebase truyền tải sang UI
    private fun observeDecks() {
        viewModelScope.launch {
            repository.getDecks().collect { deckList ->
                _decks.value = deckList
            }
        }
    }

    fun selectDeckById(deckId: String) {
        _isLoading.value = true
        _loadingProgress.value = 0.05f // Bắt đầu từ 5% để người dùng thấy có chạy ngay
        
        viewModelScope.launch {
            // Chạy tiến độ giả lập mượt mà trong background
            val progressJob = launch {
                for (i in 5..90) {
                    delay(12) 
                    _loadingProgress.value = i / 100f
                }
            }
            
            val deck = repository.getDeckById(deckId)
            if (deck != null) {
                _selectedDeck.value = deck
                resetProgress()
                
                repository.getVocabularyByDeck(deck.id, deck.vocabularyIds).collect { vocabList ->
                    progressJob.cancel()
                    
                    // Chạy nhanh nốt phần còn lại lên 100%
                    val current = (_loadingProgress.value * 100).toInt()
                    for (i in current..100) {
                        delay(4)
                        _loadingProgress.value = i / 100f
                    }
                    
                    _vocabularies.value = vocabList
                    _flashcards.value = vocabList
                    _dueVocabularies.value = vocabList
                    _learnedCount.value = 0
                    learnedIds.clear()
                    
                    delay(250) // Giữ 100% một chút cho cảm giác hoàn thành
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun selectDeck(deck: Deck) {
        _selectedDeck.value = deck
        selectDeckById(deck.id)
    }

    fun resetProgress() {
        _currentCardIndex.value = 0
        _currentSrsIndex.value = 0
        _isCardFlipped.value = false
        _isSrsCardFlipped.value = false
        _isCompleted.value = false
        _learnedCount.value = 0
        learnedIds.clear()
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun toggleSrsCardFlip() {
        _isSrsCardFlipped.value = !_isSrsCardFlipped.value
    }

    fun flipSrsCard() {
        _isSrsCardFlipped.value = !_isSrsCardFlipped.value
    }

    fun markAsLearned(id: String) {
        if (learnedIds.add(id)) {
            _learnedCount.value = learnedIds.size
            saveProgressLocally()
        }
    }

    private fun saveProgressLocally() {
        val deckId = _selectedDeck.value?.id ?: return
        val total = _vocabularies.value.size
        if (total == 0) return
        
        viewModelScope.launch {
            repository.saveProgress(deckId, learnedIds.size, total)
        }
    }

    fun nextFlashcard() {
        val nextIndex = _currentCardIndex.value + 1
        if (nextIndex < _flashcards.value.size) {
            _currentCardIndex.value = nextIndex
            _isCardFlipped.value = false
        } else {
            _isCompleted.value = true
        }
    }

    fun previousFlashcard() {
        if (_currentCardIndex.value > 0) {
            _currentCardIndex.value -= 1
            _isCardFlipped.value = false
        }
    }

    fun nextSrsCard() {
        val nextIndex = _currentSrsIndex.value + 1
        if (nextIndex < _dueVocabularies.value.size) {
            _currentSrsIndex.value = nextIndex
            _isSrsCardFlipped.value = false
        } else {
            _isCompleted.value = true
        }
    }

    fun previousSrsCard() {
        if (_currentSrsIndex.value > 0) {
            _currentSrsIndex.value -= 1
            _isSrsCardFlipped.value = false
        }
    }

    fun restartLearning() {
        resetProgress()
        _isCompleted.value = false
    }

    fun getButtonIntervalEstimate(vocabId: String, factor: EaseFactor): String {
        // Có thể bổ sung logic tính toán dự kiến interval tại đây
        return when (factor) {
            EaseFactor.AGAIN -> "1 ngày"
            EaseFactor.HARD -> "2 ngày"
            EaseFactor.GOOD -> "4 ngày"
            EaseFactor.EASY -> "7 ngày"
        }
    }

    fun submitSrsGrade(vocabId: String, grade: EaseFactor) {
        viewModelScope.launch {
            val currentVocab = _dueVocabularies.value.find { it.id == vocabId } ?: return@launch
            
            val stateToProcess = UserVocabularyState(
                vocabId = vocabId,
                deckId = _selectedDeck.value?.id ?: "",
                interval = 1.0,
                repetition = 0,
                easeFactor = EaseFactor.GOOD,
                nextReview = Date()
            )

            val updatedState = calculateSrsUseCase(stateToProcess, grade, System.currentTimeMillis())
            repository.saveUserVocabularyState(updatedState, null)

            _isCardFlipped.value = false
            _isSrsCardFlipped.value = false

            nextSrsCard()
        }
    }

    fun onSrsButtonPressed(button: EaseFactor, currentVocab: Vocabulary) {
        submitSrsGrade(currentVocab.id, button)
    }
}
