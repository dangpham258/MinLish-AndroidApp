package com.minlish.app.data.source.remote

import com.google.gson.annotations.SerializedName

data class MinhqndResponse(
    val exists: Boolean,
    val word: String?,
    val results: List<MinhqndResult>?
)

data class MinhqndResult(
    @SerializedName("lang_code") val langCode: String,
    val meanings: List<MinhqndMeaning>?
)

data class MinhqndMeaning(
    val definition: String,
    @SerializedName("definition_lang") val definitionLang: String,
    val example: String?,
    val pos: String?
)
