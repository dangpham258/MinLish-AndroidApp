package com.minlish.app.data.repository

import com.minlish.app.data.source.remote.FirebaseDatabaseService
import com.minlish.app.data.source.remote.SystemVocabularySource
import com.minlish.app.domain.model.Vocabulary

/**
 * Triển khai [SystemVocabularySource] sử dụng Firebase Realtime Database.
 * Tìm kiếm từ vựng trong kho dữ liệu hệ thống (deck_vocabularies/deck_minlish_01)
 * do admin tạo sẵn, phục vụ tính năng Auto-fill khi người dùng thêm từ mới.
 */
class SystemVocabularySourceImpl(
    private val firebaseDatabaseService: FirebaseDatabaseService
) : SystemVocabularySource {
    override suspend fun getWordFromSystem(word: String, partOfSpeech: String): Vocabulary? {
        return firebaseDatabaseService.searchVocabularyByWord(word, partOfSpeech)
    }
}
