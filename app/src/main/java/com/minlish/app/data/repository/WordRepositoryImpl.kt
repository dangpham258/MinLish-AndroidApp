package com.minlish.app.data.repository

import com.minlish.app.data.source.remote.FirebaseSource
import com.minlish.app.data.source.remote.FreeDictionaryApi
import com.minlish.app.data.source.remote.MinhqndApi
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import com.minlish.app.data.mapper.mapToVocabularyDomain

class WordRepositoryImpl(
    private val firebaseSource: FirebaseSource, // Giả định bạn đã có class xử lý Firebase
    private val freeDictApi: FreeDictionaryApi,
    private val minhqndApi: MinhqndApi
) : WordRepository {

    override suspend fun fetchWordData(wordQuery: String, partOfSpeech: String): Vocabulary? = withContext(Dispatchers.IO) {
        // 1. Tìm trên Firebase trước
        val firebaseWord = firebaseSource.getWordFromSystem(wordQuery)
        if (firebaseWord != null) {
            return@withContext firebaseWord
        }

        // 2. Nếu không có, gọi 2 API song song
        val freeDictDeferred = async {
            runCatching { 
                val response = freeDictApi.getWordInfo(wordQuery)
                // Lấy TOÀN BỘ danh sách entries, không chỉ entry đầu tiên
                // VD: "main" trả về 4 entries, noun nằm ở entry thứ 2/3
                if (response.isSuccessful) response.body() else null
            }.getOrNull()
        }
        val minhqndDeferred = async {
            runCatching { 
                val response = minhqndApi.getVietnameseMeaning(wordQuery)
                if (response.isSuccessful) response.body() else null
            }.getOrNull()
        }

        val freeDictResult = freeDictDeferred.await()
        val minhqndResult = minhqndDeferred.await()

        // Nếu cả 2 đều lỗi/không có dữ liệu thì trả về null
        if (freeDictResult.isNullOrEmpty() && minhqndResult == null) return@withContext null

        // 3. Mapping dữ liệu API sang Domain Model
        mapToVocabularyDomain(wordQuery, partOfSpeech, freeDictResult, minhqndResult)
    }
}