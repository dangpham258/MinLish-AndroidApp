package com.minlish.app.core.di

import android.content.Context
import com.minlish.app.core.database.BunnyDatabase
import com.minlish.app.data.source.remote.FirebaseDatabaseService
import com.minlish.app.data.repository.BunnyRepositoryImpl
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.repository.WordRepository
import com.minlish.app.domain.usecase.AutoFillWordUseCase
import com.minlish.app.domain.usecase.CalculateSrsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {



    @Provides
    @Singleton
    fun provideBunnyDatabase(
        @ApplicationContext context: Context
    ): BunnyDatabase {
        return BunnyDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideBunnyRepository(
        firebaseDatabaseService: FirebaseDatabaseService,
        bunnyDatabase: BunnyDatabase
    ): BunnyRepository {
        return BunnyRepositoryImpl(firebaseDatabaseService, bunnyDatabase)
    }

    @Provides
    @Singleton
    fun provideCalculateSrsUseCase(): CalculateSrsUseCase {
        return CalculateSrsUseCase()
    }

    @Provides
    @Singleton
    fun provideAutoFillWordUseCase(
        wordRepository: WordRepository
    ): AutoFillWordUseCase {
        return AutoFillWordUseCase(wordRepository)
    }
}
