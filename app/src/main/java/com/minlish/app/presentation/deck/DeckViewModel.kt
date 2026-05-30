package com.minlish.app.presentation.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.repository.BunnyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeckViewModel(private val repository: BunnyRepository) : ViewModel() {

    private val _currentDeck = MutableStateFlow<Deck?>(null)
    val currentDeck: StateFlow<Deck?> = _currentDeck.asStateFlow()

    private val _words = MutableStateFlow<List<Vocabulary>>(emptyList())
    val words: StateFlow<List<Vocabulary>> = _words.asStateFlow()

    fun loadDeckDetails(deckId: String) {
        val id = deckId.toIntOrNull() ?: return
        viewModelScope.launch {
            repository.getDeckByIdFlow(id).collect {
                _currentDeck.value = it
            }
        }
        viewModelScope.launch {
            repository.getVocabularyByDeck(id).collect {
                _words.value = it
            }
        }
    }
}

class DeckViewModelFactory(private val repository: BunnyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeckViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
