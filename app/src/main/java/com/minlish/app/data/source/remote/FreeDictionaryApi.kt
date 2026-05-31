package com.minlish.app.data.source.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FreeDictionaryApi {
    @GET("api/v2/entries/en/{word}")
    suspend fun getWordInfo(@Path("word") word: String): Response<List<FreeDictResponse>>
}