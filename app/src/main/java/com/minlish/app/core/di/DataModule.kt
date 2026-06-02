package com.minlish.app.core.di

import com.minlish.app.data.repository.FirebaseSourceImpl
import com.minlish.app.data.repository.WordRepositoryImpl
import com.minlish.app.data.source.remote.FirebaseSource
import com.minlish.app.data.source.remote.FreeDictionaryApi
import com.minlish.app.data.source.remote.MinhqndApi
import com.minlish.app.domain.repository.WordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFirebaseSource(): FirebaseSource = FirebaseSourceImpl()

    @Provides
    @Singleton
    fun provideFreeDictApi(): FreeDictionaryApi {
        return Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FreeDictionaryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMinhqndApi(): MinhqndApi {
        return Retrofit.Builder()
            .baseUrl("https://dict.minhqnd.com/") // Thay bằng base URL thật của API này
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MinhqndApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWordRepository(
        firebaseSource: FirebaseSource,
        freeDictApi: FreeDictionaryApi,
        minhqndApi: MinhqndApi // Giả định bạn đã tạo Retrofit cho Minhqnd
    ): WordRepository {
        return WordRepositoryImpl(firebaseSource, freeDictApi, minhqndApi)
    }
}