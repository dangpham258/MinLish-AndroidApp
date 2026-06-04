package com.minlish.app.data.source.remote

import com.minlish.app.domain.model.Vocabulary

/**
 * Nguồn dữ liệu từ vựng hệ thống (system vocabulary).
 * Chịu trách nhiệm truy vấn các từ vựng do admin tạo sẵn trên Firebase,
 * phân biệt với từ vựng cá nhân do người dùng tạo.
 */
interface SystemVocabularySource {
    suspend fun getWordFromSystem(word: String, partOfSpeech: String): Vocabulary?
}
