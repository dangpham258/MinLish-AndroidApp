package com.minlish.app.presentation.deck

import androidx.lifecycle.ViewModel
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO: Integrate with real use cases and repositories for Firebase & API
class DeckViewModel : ViewModel() {

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

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    fun searchDecks(query: String) {
        // Mock search
    }

    fun loadDeckDetails(deckId: String) {
        // Mock load
        _currentDeck.value = _decks.value.find { it.id == deckId }
        _words.value = listOf(
            Word("w1", deckId, "Abandon", "/əˈbændən/", "Verb", "To leave completely and finally", "Từ bỏ", "He abandoned his car in the snow."),
            Word("w2", deckId, "Ability", "/əˈbɪlɪti/", "Noun", "Capacity or power to do something", "Khả năng", "She has the ability to learn quickly.")
        )
    }

    fun createDeck(name: String, description: String, tags: List<String>) {
        // Mock create
    }

    fun deleteDeck(deckId: String) {
        // Mock delete
    }

    fun addWord(word: Word) {
        // Mock add word
    }

    fun updateWord(word: Word) {
        // Mock update word
    }

    fun deleteWord(wordId: String) {
        // Mock delete word
    }
}
