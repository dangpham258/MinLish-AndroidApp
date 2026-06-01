package com.minlish.app.presentation.deck

import androidx.lifecycle.ViewModel
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.Vocabulary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.minlish.app.domain.usecase.AutoFillWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: Integrate with real use cases and repositories for Firebase & API
@HiltViewModel
class DeckViewModel @Inject constructor(
    private val autoFillWordUseCase: AutoFillWordUseCase
) : ViewModel() {

    private val _decks = MutableStateFlow<List<Deck>>(
        listOf(
            Deck(id = "1", name = "IELTS Speaking Part 1", tags = listOf("IELTS", "Speaking"), totalWords = 50),
            Deck(id = "2", name = "Business English", tags = listOf("Business"), totalWords = 120),
            Deck(id = "3", name = "Daily Conversation", tags = listOf("Daily"), totalWords = 30)
        )
    )
    val decks: StateFlow<List<Deck>> = _decks.asStateFlow()

    private val _currentDeck = MutableStateFlow<Deck?>(null)
    val currentDeck: StateFlow<Deck?> = _currentDeck.asStateFlow()

    private val _words = MutableStateFlow<List<Vocabulary>>(emptyList())
    val words: StateFlow<List<Vocabulary>> = _words.asStateFlow()

    // Thêm state để quản lý việc Auto-fill
    private val _isSearchingAPI = MutableStateFlow(false)
    val isSearchingAPI: StateFlow<Boolean> = _isSearchingAPI.asStateFlow()

    // Inject UseCase vào ViewModel (thông qua Hilt/Dagger)
    fun searchWordToAutoFill(query: String, partOfSpeech: String, onResult: (Vocabulary?) -> Unit) {
        viewModelScope.launch {
            _isSearchingAPI.value = true
            // Gọi UseCase
            val wordData = autoFillWordUseCase(query, partOfSpeech)
            _isSearchingAPI.value = false

            onResult(wordData)
        }
    }

    fun searchDecks(query: String) {
        // Mock search
    }

    fun loadDeckDetails(deckId: String) {
        // Mock load
        _currentDeck.value = _decks.value.find { it.id == deckId }
        _words.value = listOf(
            Vocabulary("w1", deckId, "Abandon", "/əˈbændən/", "Verb", "", null, "To leave completely and finally", "Từ bỏ", "He abandoned his car in the snow."),
            Vocabulary("w2", deckId, "Ability", "/əˈbɪlɪti/", "Noun", "", null, "Capacity or power to do something", "Khả năng", "She has the ability to learn quickly.")
        )
    }

    fun createDeck(name: String, description: String, tags: List<String>) {
        // Mock create
    }

    fun deleteDeck(deckId: String) {
        // Mock delete
    }

    fun addWord(word: Vocabulary) {
        // Mock add word
    }

    fun updateWord(word: Vocabulary) {
        // Mock update word
    }

    fun deleteWord(wordId: String) {
        // Mock delete word
    }
}
