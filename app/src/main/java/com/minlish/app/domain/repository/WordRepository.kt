package com.minlish.app.domain.repository

import com.minlish.app.domain.model.Word

interface WordRepository {
    suspend fun fetchWordData(wordQuery: String): Word?
}