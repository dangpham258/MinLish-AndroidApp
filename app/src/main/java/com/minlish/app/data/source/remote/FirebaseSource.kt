package com.minlish.app.data.source.remote

import com.minlish.app.domain.model.Word

interface FirebaseSource {
    suspend fun getWordFromSystem(word: String): Word?
}