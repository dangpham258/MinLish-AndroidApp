package com.minlish.app.presentation.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minlish.app.core.di.ServiceLocator
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.model.enumration.LearningGoal
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.AutoFillWordUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class DeckViewModel(
    private val repository: BunnyRepository,
    private val autoFillWordUseCase: AutoFillWordUseCase
) : ViewModel() {

    val decks: StateFlow<List<Deck>> = repository.getDecks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentDeck = MutableStateFlow<Deck?>(null)
    val currentDeck: StateFlow<Deck?> = _currentDeck.asStateFlow()

    private val _words = MutableStateFlow<List<Vocabulary>>(emptyList())
    val words: StateFlow<List<Vocabulary>> = _words.asStateFlow()

    private val _isSearchingAPI = MutableStateFlow(false)
    val isSearchingAPI: StateFlow<Boolean> = _isSearchingAPI.asStateFlow()

    private val _loadingProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val loadingProgress: StateFlow<Map<String, Int>> = _loadingProgress.asStateFlow()

    init {
        syncDataIfNeeded()
    }

    private fun syncDataIfNeeded() {
        viewModelScope.launch {
            try {
                repository.prepopulateInitialData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchWordToAutoFill(query: String, partOfSpeech: String, onResult: (Vocabulary?) -> Unit) {
        viewModelScope.launch {
            _isSearchingAPI.value = true
            val wordData = autoFillWordUseCase(query, partOfSpeech)
            _isSearchingAPI.value = false
            onResult(wordData)
        }
    }

    fun searchDecks(query: String) {
        // Có thể filter Local state ở ListOfDeckScreen, không cần xử lý logic phức tạp ở đây
    }

    fun loadDeckDetails(deckId: String) {
        viewModelScope.launch {
            val deck = repository.getDeckById(deckId)
            _currentDeck.value = deck

            if (deck != null) {
                repository.getVocabularyByDeck(deckId, deck.vocabularyIds).collectLatest { vocabList ->
                    _words.value = vocabList
                }
            }
        }
    }

    fun getDeckProgress(deckId: String): Flow<Int> {
        return repository.getProgress(deckId)
    }

    fun addWord(vocabulary: Vocabulary) {
        viewModelScope.launch {
            repository.insertVocabulary(vocabulary)
        }
    }

    fun updateWord(vocabulary: Vocabulary) {
        viewModelScope.launch {
            repository.insertVocabulary(vocabulary)
        }
    }

    fun createDeck(name: String, description: String, tags: List<String>) {
        viewModelScope.launch {
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
                tags = goalTags
            )
            repository.insertDeck(newDeck)
        }
    }
}

class DeckViewModelFactory(
    private val repository: BunnyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckViewModel::class.java)) {
            return DeckViewModel(
                repository,
                ServiceLocator.getAutoFillWordUseCase()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
