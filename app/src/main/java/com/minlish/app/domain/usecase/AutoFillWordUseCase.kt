package com.minlish.app.domain.usecase

import com.minlish.app.domain.model.Vocabulary

class AutoFillWordUseCase {
    suspend operator fun invoke(query: String, partOfSpeech: String): Vocabulary? {
        // This is a stub implementation. 
        // In a real app, this would call a remote API (like Free Dictionary API or OpenAI) 
        // to get word details.
        return null
    }
}
