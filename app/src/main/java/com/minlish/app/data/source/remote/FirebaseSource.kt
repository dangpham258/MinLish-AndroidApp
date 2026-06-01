package com.minlish.app.data.source.remote

import com.minlish.app.domain.model.Vocabulary

interface FirebaseSource {
    suspend fun getWordFromSystem(word: String): Vocabulary?
}