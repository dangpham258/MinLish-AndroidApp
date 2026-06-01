package com.minlish.app.data.mapper

import com.minlish.app.data.source.remote.FreeDictResponse
import com.minlish.app.data.source.remote.MinhqndResponse
import com.minlish.app.domain.model.Vocabulary

fun mapToVocabularyDomain(
    wordQuery: String,
    partOfSpeech: String,
    freeDictResult: FreeDictResponse?,
    minhqndResult: MinhqndResponse?
): Vocabulary? {
    // Tìm meaning theo partOfSpeech
    val targetMeaning = freeDictResult?.meanings?.firstOrNull { 
        it.partOfSpeech.equals(partOfSpeech, ignoreCase = true) 
    }
    
    // Nếu API không trả về loại từ này thì return null
    if (freeDictResult != null && targetMeaning == null) {
        return null
    }

    val primaryDefinition = targetMeaning?.definitions?.firstOrNull()

    // Lấy nghĩa tiếng Việt đầu tiên từ kết quả trả về của API minhqnd
    val vietnameseMeaningObj = minhqndResult?.results
        ?.flatMap { it.meanings ?: emptyList() }
        ?.firstOrNull { it.definitionLang == "vi" }
        
    val vietnameseMeaningText = vietnameseMeaningObj?.definition ?: ""
    
    val soundUrl = freeDictResult?.phonetics?.firstOrNull { !it.audio.isNullOrEmpty() }?.audio ?: ""
    val phoneticText = freeDictResult?.phonetics?.firstOrNull { !it.text.isNullOrEmpty() }?.text ?: ""

    return Vocabulary(
        id = "", // Sẽ được gen khi lưu
        deckId = "",
        word = freeDictResult?.word ?: wordQuery,
        phonetic = phoneticText,
        partOfSpeech = targetMeaning?.partOfSpeech ?: partOfSpeech,
        soundUrl = soundUrl,
        level = null, // Set null as required
        englishDefinition = primaryDefinition?.definition ?: "",
        vietnameseMeaning = vietnameseMeaningText,
        context = primaryDefinition?.example ?: vietnameseMeaningObj?.example ?: ""
    )
}
