package com.minlish.app.data.source.remote

data class FreeDictResponse(
    val word: String,
    val phonetics: List<PhoneticDto>,
    val meanings: List<MeaningDto>
)

data class PhoneticDto(val text: String?, val audio: String?)
data class MeaningDto(val partOfSpeech: String, val definitions: List<DefinitionDto>)
data class DefinitionDto(val definition: String, val example: String?)