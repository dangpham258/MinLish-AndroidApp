package com.minlish.app.data.source.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MinhqndApi {
    @GET("api/v1/lookup")
    suspend fun getVietnameseMeaning(@Query("word") word: String): Response<MinhqndResponse>
}