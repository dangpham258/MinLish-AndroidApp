package com.minlish.app.core.di

import android.content.Context
import com.minlish.app.core.database.BunnyDatabase
import com.minlish.app.core.database.FirebaseDatabaseService
import com.minlish.app.data.repository.BunnyRepositoryImpl
import com.minlish.app.data.repository.FirebaseSourceImpl
import com.minlish.app.data.repository.WordRepositoryImpl
import com.minlish.app.data.source.remote.FreeDictionaryApi
import com.minlish.app.data.source.remote.MinhqndApi
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.repository.WordRepository
import com.minlish.app.domain.usecase.AutoFillWordUseCase
import com.minlish.app.domain.usecase.CalculateSrsUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {
    @Volatile
    private var repository: BunnyRepository? = null

    @Volatile
    private var firebaseService: FirebaseDatabaseService? = null

    @Volatile
    private var wordRepository: WordRepository? = null

    private val calculateSrsUseCase = CalculateSrsUseCase()

    private fun getFirebaseService(context: Context): FirebaseDatabaseService {
        return firebaseService ?: synchronized(this) {
            val service = FirebaseDatabaseService(context.applicationContext)
            firebaseService = service
            service
        }
    }

    fun getRepository(context: Context): BunnyRepository {
        return repository ?: synchronized(this) {
            val existing = repository
            if (existing != null) return@synchronized existing

            val service = getFirebaseService(context)
            val db = BunnyDatabase.getDatabase(context)
            val repo = BunnyRepositoryImpl(service, db)
            repository = repo
            repo
        }
    }

    private fun getWordRepository(): WordRepository {
        return wordRepository ?: synchronized(this) {
            val freeDictApi = Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FreeDictionaryApi::class.java)
            
            val minhqndApi = Retrofit.Builder()
                .baseUrl("https://dict.minhqnd.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MinhqndApi::class.java)

            val repo = WordRepositoryImpl(FirebaseSourceImpl(), freeDictApi, minhqndApi)
            wordRepository = repo
            repo
        }
    }

    fun getCalculateSrsUseCase(): CalculateSrsUseCase {
        return calculateSrsUseCase
    }

    fun getAutoFillWordUseCase(): AutoFillWordUseCase {
        return AutoFillWordUseCase(getWordRepository())
    }
}
