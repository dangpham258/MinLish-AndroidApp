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

    private fun observeDecks() {
        viewModelScope.launch {
            repository.getDecks().collect { deckList ->
                _decks.value = deckList
            }
        }
    }

    fun selectDeckById(deckId: String) {
        _isLoading.value = true
        _loadingProgress.value = 0.05f
        
        viewModelScope.launch {
            val progressJob = launch {
                for (i in 5..90) {
                    delay(12) 
                    _loadingProgress.value = i / 100f
                }
            }
            
            val isAllDue = deckId == "all_due"
            val deck = if (isAllDue) {
                Deck(
                    id = "all_due",
                    name = "Daily Review",
                    description = "Review vocabularies scheduled for today",
                    vocabularyIds = emptyList()
                )
            } else {
                repository.getDeckById(deckId)
            }
            
            if (deck != null) {
                _selectedDeck.value = deck
                
                learnedIds.clear()
                if (isAllDue) {
                    _learnedCount.value = 0
                } else {
                    // Khôi phục tiến độ cũ từ Local trước khi bắt đầu
                    val savedProgress = repository.getDeckProgressEntity(deckId)
                    if (savedProgress != null && savedProgress.learnedVocabularyIds.isNotBlank()) {
                        val ids = savedProgress.learnedVocabularyIds.split(",").toSet()
                        learnedIds.addAll(ids)
                        _learnedCount.value = learnedIds.size
                    } else {
                        _learnedCount.value = 0
                    }
                }

                _currentCardIndex.value = 0
                _currentSrsIndex.value = 0
                _isCardFlipped.value = false
                _isSrsCardFlipped.value = false
                _isCompleted.value = false
                
                val todayEndCal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }
                val todayEndMs = todayEndCal.timeInMillis

                val vocabFlow = if (isAllDue) {
                    repository.getVocabularyDueForReview(todayEndMs)
                } else {
                    repository.getVocabularyByDeck(deck.id, deck.vocabularyIds)
                }

                vocabFlow.collect { vocabList ->
                    progressJob.cancel()
                    val current = (_loadingProgress.value * 100).toInt()
                    for (i in current..100) {
                        delay(4)
                        _loadingProgress.value = i / 100f
                    }
                    
                    _vocabularies.value = vocabList
                    _flashcards.value = vocabList
                    _dueVocabularies.value = vocabList
                    
                    delay(250)
                    _isLoading.value = false
                }
            } else {
                progressJob.cancel()
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
        saveProgressLocally()
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
        if (_isCardFlipped.value) {
            val currentVocab = _flashcards.value.getOrNull(_currentCardIndex.value)
            currentVocab?.id?.let { markAsLearned(it) }
        }
    }

    fun toggleSrsCardFlip() {
        _isSrsCardFlipped.value = !_isSrsCardFlipped.value
        if (_isSrsCardFlipped.value) {
            val currentVocab = _dueVocabularies.value.getOrNull(_currentSrsIndex.value)
            currentVocab?.id?.let { markAsLearned(it) }
        }
    }

    fun flipSrsCard() {
        _isSrsCardFlipped.value = !_isSrsCardFlipped.value
        if (_isSrsCardFlipped.value) {
            val currentVocab = _dueVocabularies.value.getOrNull(_currentSrsIndex.value)
            currentVocab?.id?.let { markAsLearned(it) }
        }
    }

    fun markAsLearned(id: String) {
        viewModelScope.launch {
            if (learnedIds.add(id)) {
                _learnedCount.value = learnedIds.size
                saveProgressLocally()
                repository.notifyNewWordLearned(id)
            }
        }
    }

    private fun saveProgressLocally() {
        val deckId = _selectedDeck.value?.id ?: return
        if (deckId == "all_due") return
        val total = _vocabularies.value.size
        if (total == 0) return
        
        viewModelScope.launch {
            repository.saveProgress(deckId, learnedIds.size, total, learnedIds)
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
        _isCompleted.value = false
        _currentCardIndex.value = 0
        _currentSrsIndex.value = 0
    }

    fun getButtonIntervalEstimate(vocabId: String, factor: EaseFactor): String {
        return calculateSrsUseCase.estimateNextIntervalDays(
            UserVocabularyState(vocabId = vocabId),
            factor
        )
    }

    fun submitSrsGrade(vocabId: String, grade: EaseFactor) {
        viewModelScope.launch {
            val currentVocab = _dueVocabularies.value.getOrNull(_currentSrsIndex.value)
            val vocabDeckId = currentVocab?.deckId ?: _selectedDeck.value?.id ?: ""
            val currentState = repository.getUserVocabularyState(vocabId) ?: UserVocabularyState(
                vocabId = vocabId,
                deckId = vocabDeckId
            )
            val updatedState = calculateSrsUseCase(currentState, grade, System.currentTimeMillis())
            repository.saveUserVocabularyState(updatedState, grade)

            if (currentState.repetition == 0 && updatedState.repetition > 0) {
                markAsLearned(vocabId)
            }

            _isCardFlipped.value = false
            _isSrsCardFlipped.value = false
            nextSrsCard()
        }
    }

    fun onSrsButtonPressed(button: EaseFactor, currentVocab: Vocabulary) {
        submitSrsGrade(currentVocab.id, button)
    }
}
