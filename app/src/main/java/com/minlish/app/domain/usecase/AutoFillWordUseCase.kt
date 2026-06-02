package com.minlish.app.domain.usecase

import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.repository.WordRepository

import javax.inject.Inject

class AutoFillWordUseCase @Inject constructor(private val repository: WordRepository) {
    suspend operator fun invoke(wordQuery: String, partOfSpeech: String): Vocabulary? {
        if (wordQuery.isBlank()) return null
        return repository.fetchWordData(wordQuery.trim().lowercase(), partOfSpeech)
    }
}
