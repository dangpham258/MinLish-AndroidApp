package com.minlish.app.domain.repository

import com.minlish.app.domain.model.Vocabulary

interface WordRepository {
    suspend fun fetchWordData(wordQuery: String, partOfSpeech: String): Vocabulary?
}