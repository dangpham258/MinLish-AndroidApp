package com.minlish.app.core.di

import android.content.Context
import com.minlish.app.core.database.BunnyDatabase
import com.minlish.app.core.database.FirebaseDatabaseService
import com.minlish.app.data.repository.BunnyRepositoryImpl
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.AutoFillWordUseCase
import com.minlish.app.domain.usecase.CalculateSrsUseCase

object ServiceLocator {
    @Volatile
    private var repository: BunnyRepository? = null

    @Volatile
    private var firebaseService: FirebaseDatabaseService? = null

    private val calculateSrsUseCase = CalculateSrsUseCase()
    private val autoFillWordUseCase = AutoFillWordUseCase()

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

    fun getCalculateSrsUseCase(): CalculateSrsUseCase {
        return calculateSrsUseCase
    }

    fun getAutoFillWordUseCase(): AutoFillWordUseCase {
        return autoFillWordUseCase
    }
}
