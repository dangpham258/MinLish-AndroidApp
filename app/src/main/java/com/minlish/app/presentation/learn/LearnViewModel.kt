package com.minlish.app.presentation.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minlish.app.core.di.ServiceLocator
import com.minlish.app.domain.model.*
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.CalculateSrsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

open class LearnViewModel(
    private val repository: BunnyRepository,
    private val calculateSrsUseCase: CalculateSrsUseCase
) : ViewModel() {

    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    open val decks: StateFlow<List<Deck>> = _decks.asStateFlow()

    private val _selectedDeck = MutableStateFlow<Deck?>(null)
    open val selectedDeck: StateFlow<Deck?> = _selectedDeck.asStateFlow()

    private val _vocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    open val vocabularies: StateFlow<List<Vocabulary>> = _vocabularies.asStateFlow()

    // Flashcard Learning state
    private val _flashcards = MutableStateFlow<List<Vocabulary>>(emptyList())
    open val flashcards: StateFlow<List<Vocabulary>> = _flashcards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    open val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    open val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    // SRS Learning card states (Due review words Filtered)
    private val _dueVocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    open val dueVocabularies: StateFlow<List<Vocabulary>> = _dueVocabularies.asStateFlow()

    private val _currentSrsIndex = MutableStateFlow(0)
    open val currentSrsIndex: StateFlow<Int> = _currentSrsIndex.asStateFlow()

    private val _isSrsCardFlipped = MutableStateFlow(false)
    open val isSrsCardFlipped: StateFlow<Boolean> = _isSrsCardFlipped.asStateFlow()

    // User progress stats
    private val _learnedCount = MutableStateFlow(0)
    open val learnedCount: StateFlow<Int> = _learnedCount.asStateFlow()

    private val _maxReachedIndex = MutableStateFlow(-1)
    private val _maxReachedSrsIndex = MutableStateFlow(-1)

    private val _isCompleted = MutableStateFlow(false)
    open val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    fun resetProgress() {
        _currentCardIndex.value = 0
        _currentSrsIndex.value = 0
        _learnedCount.value = 0
        _maxReachedIndex.value = -1
        _maxReachedSrsIndex.value = -1
        _isCompleted.value = false
        _isCardFlipped.value = false
        _isSrsCardFlipped.value = false
    }

    fun restartLearning() {
        _currentCardIndex.value = 0
        _currentSrsIndex.value = 0
        _isCompleted.value = false
        _isCardFlipped.value = false
        _isSrsCardFlipped.value = false
    }

    fun markAsLearned(vocabularyId: Int) {
        viewModelScope.launch {
            val currentState = repository.getUserVocabularyState(vocabularyId)
            if (currentState == null) {
                // Initial state when just viewed
                val initialState = UserVocabularyState(
                    userId = 1,
                    vocabularyId = vocabularyId,
                    interval = 0.0,
                    repetition = 0,
                    easeFactor = EaseFactor.Again,
                    nextReview = Date()
                )
                repository.saveUserVocabularyState(initialState, null)
            }
        }
    }

    private val _streakDays = MutableStateFlow(0)
    open val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateInitialData()
            loadDecksAndSelectDefault()
            observeStats()
        }
    }

    private fun loadDecksAndSelectDefault() {
        viewModelScope.launch {
            repository.getDecks().collect { deckList ->
                _decks.value = deckList
                if (deckList.isNotEmpty() && _selectedDeck.value == null) {
                    selectDeck(deckList.first())
                }
            }
        }
    }

    fun selectDeck(deck: Deck) {
        _selectedDeck.value = deck
        viewModelScope.launch {
            repository.incrementStreak()
            repository.getVocabularyByDeck(deck.id).collect { list ->
                _vocabularies.value = list
                _flashcards.value = list.shuffled()
                _currentCardIndex.value = 0
                _isCardFlipped.value = false
            }
        }
        observeDueVocabulary()
    }

    private fun observeStats() {
        viewModelScope.launch {
            repository.getLearnedWordsCount().collect { count ->
                _learnedCount.value = count
            }
        }
        viewModelScope.launch {
            repository.getStreakDaysCount().collect { count ->
                _streakDays.value = count
            }
        }
    }

    private fun observeDueVocabulary() {
        viewModelScope.launch {
            val deckId = _selectedDeck.value?.id
            combine(
                repository.getVocabularyDueForReview(System.currentTimeMillis(), deckId),
                repository.getNewVocabularyForLearning(deckId, 10)
            ) { due, new ->
                due + new
            }.collect { combinedList ->
                _dueVocabularies.value = combinedList
                if (_currentSrsIndex.value >= combinedList.size) {
                    _currentSrsIndex.value = 0
                }
            }
        }
    }

    open fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    open fun nextFlashcard() {
        if (_flashcards.value.isNotEmpty()) {
            if (_currentCardIndex.value > _maxReachedIndex.value) {
                _maxReachedIndex.value = _currentCardIndex.value
                _learnedCount.value = _maxReachedIndex.value + 1
            }
            
            if (_currentCardIndex.value == _flashcards.value.size - 1) {
                _isCompleted.value = true
            } else {
                _currentCardIndex.value = _currentCardIndex.value + 1
                _isCardFlipped.value = false
            }
        }
    }

    open fun previousFlashcard() {
        if (_flashcards.value.isNotEmpty()) {
            val nextIndex = if (_currentCardIndex.value - 1 < 0) {
                0 // Don't loop back in learn mode
            } else {
                _currentCardIndex.value - 1
            }
            _currentCardIndex.value = nextIndex
            _isCardFlipped.value = false
        }
    }

    open fun nextSrsCard() {
        if (_dueVocabularies.value.isNotEmpty()) {
            if (_currentSrsIndex.value > _maxReachedSrsIndex.value) {
                _maxReachedSrsIndex.value = _currentSrsIndex.value
                _learnedCount.value = _maxReachedSrsIndex.value + 1
            }

            if (_currentSrsIndex.value == _dueVocabularies.value.size - 1) {
                _isCompleted.value = true
            } else {
                _currentSrsIndex.value = _currentSrsIndex.value + 1
                _isSrsCardFlipped.value = false
            }
        }
    }

    open fun previousSrsCard() {
        if (_dueVocabularies.value.isNotEmpty()) {
            val nextIndex = if (_currentSrsIndex.value - 1 < 0) {
                0 // Don't loop back
            } else {
                _currentSrsIndex.value - 1
            }
            _currentSrsIndex.value = nextIndex
            _isSrsCardFlipped.value = false
        }
    }

    open fun toggleSrsCardFlip() {
        _isSrsCardFlipped.value = !_isSrsCardFlipped.value
    }

    open suspend fun getButtonIntervalEstimate(vocabularyId: Int, button: EaseFactor): String {
        val existingState = repository.getUserVocabularyState(vocabularyId) ?: UserVocabularyState(
            userId = 1,
            vocabularyId = vocabularyId,
            interval = 1.0,
            repetition = 1,
            easeFactor = EaseFactor.Good, // Đổi từ 2.5 sang Enum
            nextReview = Date() // Đổi sang Date
        )
        return calculateSrsUseCase.estimateNextIntervalDays(existingState, button)
    }

    open fun submitSrsGrade(vocabularyId: Int, button: EaseFactor) {
        viewModelScope.launch {
            val existingState = repository.getUserVocabularyState(vocabularyId)
            val previousEaseFactor = existingState?.easeFactor

            val stateToProcess = existingState ?: UserVocabularyState(
                userId = 1,
                vocabularyId = vocabularyId,
                interval = 0.0, // Start with 0 if new
                repetition = 0, // Start with 0 if new
                easeFactor = EaseFactor.Again,
                nextReview = Date()
            )

            val updatedState = calculateSrsUseCase(stateToProcess, button, System.currentTimeMillis())
            repository.saveUserVocabularyState(updatedState, previousEaseFactor)

            if (button != EaseFactor.Again) {
                repository.incrementStreak()
            }

            _isCardFlipped.value = false
            _isSrsCardFlipped.value = false
            
            if (_dueVocabularies.value.isNotEmpty() && _dueVocabularies.value.size > _currentSrsIndex.value) {
                _currentSrsIndex.value = (_currentSrsIndex.value + 1) % _dueVocabularies.value.size
            }
            if (_flashcards.value.isNotEmpty() && _flashcards.value.size > _currentCardIndex.value) {
                _currentCardIndex.value = (_currentCardIndex.value + 1) % _flashcards.value.size
            }
            
            observeDueVocabulary()
        }
    }
}

class LearnViewModelFactory(
    private val repository: BunnyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearnViewModel::class.java)) {
            return LearnViewModel(repository, ServiceLocator.getCalculateSrsUseCase()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
