package com.minlish.app.data.mapper

import com.minlish.app.data.source.remote.FreeDictResponse
import com.minlish.app.data.source.remote.MinhqndResponse
import com.minlish.app.domain.model.Word

fun mapToWordDomain(
    wordQuery: String,
    freeDictResult: FreeDictResponse?,
    minhqndResult: MinhqndResponse?
): Word {
    val primaryMeaning = freeDictResult?.meanings?.firstOrNull()
    val primaryDefinition = primaryMeaning?.definitions?.firstOrNull()

    // Lấy nghĩa tiếng Việt đầu tiên từ kết quả trả về của API minhqnd
    val vietnameseMeaningObj = minhqndResult?.results
        ?.flatMap { it.meanings ?: emptyList() }
        ?.firstOrNull { it.definitionLang == "vi" }
        
    val vietnameseMeaningText = vietnameseMeaningObj?.definition ?: ""

    return Word(
        id = "", // Sẽ được gen khi lưu
        deckId = "",
        word = freeDictResult?.word ?: wordQuery,
        phonetic = freeDictResult?.phonetics?.firstOrNull { !it.text.isNullOrEmpty() }?.text ?: "",
        partOfSpeech = primaryMeaning?.partOfSpeech ?: "",
        englishDefinition = primaryDefinition?.definition ?: "",
        vietnameseMeaning = vietnameseMeaningText,
        context = primaryDefinition?.example ?: vietnameseMeaningObj?.example ?: ""
    )
}
