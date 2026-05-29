package com.minlish.app.core.di

import com.minlish.app.core.network.FirebaseFunctionsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    // Firebase Functions API
    @Provides
    @Singleton
    @Named("functions")
    fun provideFirebaseFunctionsApi(): FirebaseFunctionsApi {
        return Retrofit.Builder()
            .baseUrl(FirebaseFunctionsApi.PRODUCTION_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirebaseFunctionsApi::class.java)
    }
}
