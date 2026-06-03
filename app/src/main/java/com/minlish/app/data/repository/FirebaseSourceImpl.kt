package com.minlish.app.data.repository

import com.minlish.app.data.source.remote.FirebaseDatabaseService
import com.minlish.app.data.source.remote.FirebaseSource
import com.minlish.app.domain.model.Vocabulary

class FirebaseSourceImpl(
    private val firebaseDatabaseService: FirebaseDatabaseService
) : FirebaseSource {
    override suspend fun getWordFromSystem(word: String, partOfSpeech: String): Vocabulary? {
        // Tìm từ vựng trong deck_vocabularies/deck_minlish_01 trước
        return firebaseDatabaseService.searchVocabularyByWord(word, partOfSpeech)
    }
}