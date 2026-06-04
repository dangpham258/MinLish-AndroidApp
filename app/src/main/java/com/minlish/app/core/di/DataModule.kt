package com.minlish.app.core.di

import android.content.Context
import com.minlish.app.data.FileStorageHelperImpl
import com.minlish.app.data.repository.SystemVocabularySourceImpl
import com.minlish.app.data.repository.WordRepositoryImpl
import com.minlish.app.data.source.remote.FirebaseAuthApi
import com.minlish.app.data.source.remote.FirebaseDatabaseService
import com.minlish.app.data.source.remote.FreeDictionaryApi
import com.minlish.app.data.source.remote.MinhqndApi
import com.minlish.app.data.source.remote.SystemVocabularySource
import com.minlish.app.core.util.FileStorageHelper
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.repository.WordRepository
import com.minlish.app.domain.usecase.CreateDeckUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSystemVocabularySource(
        firebaseDatabaseService: FirebaseDatabaseService
    ): SystemVocabularySource = SystemVocabularySourceImpl(firebaseDatabaseService)

    @Provides
    @Singleton
    fun provideFileStorageHelper(
        @ApplicationContext context: Context
    ): FileStorageHelper = FileStorageHelperImpl(context)

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
            .baseUrl("https://dict.minhqnd.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MinhqndApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWordRepository(
        systemVocabularySource: SystemVocabularySource,
        freeDictApi: FreeDictionaryApi,
        minhqndApi: MinhqndApi
    ): WordRepository {
        return WordRepositoryImpl(systemVocabularySource, freeDictApi, minhqndApi)
    }

    @Provides
    @Singleton
    fun provideCreateDeckUseCase(
        repository: BunnyRepository,
        authApi: FirebaseAuthApi
    ): CreateDeckUseCase {
        return CreateDeckUseCase(repository, authApi)
    }
}