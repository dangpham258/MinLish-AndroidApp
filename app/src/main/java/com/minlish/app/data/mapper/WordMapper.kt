package com.minlish.app.data.mapper

import com.minlish.app.data.source.remote.FreeDictResponse
import com.minlish.app.data.source.remote.MinhqndResponse
import com.minlish.app.domain.model.Vocabulary

/**
 * Map dữ liệu từ FreeDictionaryAPI (danh sách nhiều entries) và MinhQND API sang Vocabulary.
 *
 * Free Dictionary API trả về một mảng các entry, mỗi entry có thể chứa loại từ khác nhau.
 * Ví dụ: từ "main" trả về 4 entries, noun nằm ở entry thứ 2 và 3, không phải entry đầu tiên.
 * => Cần tìm kiếm xuyên suốt tất cả entries.
 *
 * dict.minhqnd.com không phân biệt loại từ, nên luôn trả về nghĩa tiếng Việt chung
 * bất kể partOfSpeech được chọn là gì. Đây là giới hạn của API đó.
 */
fun mapToVocabularyDomain(
    wordQuery: String,
    partOfSpeech: String,
    freeDictEntries: List<FreeDictResponse>?,
    minhqndResult: MinhqndResponse?
): Vocabulary? {
    // Tìm meaning theo partOfSpeech xuyên suốt TẤT CẢ các entries
    // VD: "main" có noun ở entry thứ 2/3, không phải entry đầu tiên
    val targetMeaning = freeDictEntries
        ?.flatMap { entry -> entry.meanings }
        ?.firstOrNull { meaning ->
            meaning.partOfSpeech.equals(partOfSpeech, ignoreCase = true)
        }

    // Nếu Free Dict API có data nhưng không tìm thấy loại từ này → trả về null để báo lỗi
    if (freeDictEntries != null && targetMeaning == null) {
        return null
    }

    val primaryDefinition = targetMeaning?.definitions?.firstOrNull()

    // Lấy phonetics từ entry đầu tiên có audio/text (phonetics giống nhau ở tất cả entries)
    val firstEntryWithPhonetics = freeDictEntries?.firstOrNull { it.phonetics.isNotEmpty() }
    val soundUrl = firstEntryWithPhonetics?.phonetics
        ?.firstOrNull { !it.audio.isNullOrEmpty() }?.audio ?: ""
    val phoneticText = firstEntryWithPhonetics?.phonetics
        ?.firstOrNull { !it.text.isNullOrEmpty() }?.text ?: ""

    // dict.minhqnd.com: chỉ lấy kết quả tiếng Anh (lang_code="en"), bỏ qua các ngôn ngữ khác
    // rồi lọc các nghĩa tiếng Việt (definition_lang="vi") theo đúng loại từ được chọn.
    val vietnameseMeanings = minhqndResult?.results
        ?.filter { it.langCode == "en" }
        ?.flatMap { it.meanings ?: emptyList() }
        ?.filter { it.definitionLang == "vi" } ?: emptyList()

    // Tìm nghĩa khớp đúng với loại từ (partOfSpeech) được chọn.
    // KHÔNG fallback về firstOrNull() vô điều kiện vì sẽ luôn trả về loại từ đầu tiên
    // (thường là Danh từ) dù người dùng chọn Động từ hay loại từ khác.
    val matchedByPos = vietnameseMeanings
        .filter { mapViPosToEnglish(it.pos).equals(partOfSpeech, ignoreCase = true) }

    val vietnameseMeaningObj = matchedByPos.firstOrNull()
        ?: vietnameseMeanings.firstOrNull { it.pos != null } // fallback: lấy nghĩa có pos đầu tiên
    val vietnameseMeaningText = vietnameseMeaningObj?.definition ?: ""

    return Vocabulary(
        id = "", // Sẽ được gen khi lưu
        deckId = "",
        word = freeDictEntries?.firstOrNull()?.word ?: wordQuery,
        phonetic = phoneticText,
        partOfSpeech = targetMeaning?.partOfSpeech ?: partOfSpeech,
        soundUrl = soundUrl,
        level = null,
        englishDefinition = primaryDefinition?.definition ?: "",
        vietnameseMeaning = vietnameseMeaningText,
        context = primaryDefinition?.example ?: vietnameseMeaningObj?.example ?: ""
    )
}

private fun mapViPosToEnglish(viPos: String?): String {
    return when (viPos?.trim()?.lowercase()) {
        "danh từ" -> "noun"
        "động từ" -> "verb"
        "tính từ" -> "adjective"
        "trạng từ", "phó từ" -> "adverb"
        "đại từ" -> "pronoun"
        "giới từ" -> "preposition"
        "liên từ" -> "conjunction"
        "thán từ" -> "interjection"
        else -> ""
    }
}
