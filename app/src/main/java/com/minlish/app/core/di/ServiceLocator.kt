package com.minlish.app.core.di

import android.content.Context
import com.minlish.app.core.database.BunnyDatabase
import com.minlish.app.data.repository.BunnyRepositoryImpl
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.CalculateSrsUseCase

object ServiceLocator {
    @Volatile
    private var database: BunnyDatabase? = null

    @Volatile
    private var repository: BunnyRepository? = null

    private val calculateSrsUseCase = CalculateSrsUseCase()

    private fun getDatabase(context: Context): BunnyDatabase {
        return database ?: synchronized(this) {
            val db = BunnyDatabase.getDatabase(context)
            database = db
            db
        }
    }

    fun getRepository(context: Context): BunnyRepository {
        return repository ?: synchronized(this) {
            val dao = getDatabase(context).bunnyDao()
            val repo = BunnyRepositoryImpl(dao, calculateSrsUseCase)
            repository = repo
            repo
        }
    }

    fun getCalculateSrsUseCase(): CalculateSrsUseCase {
        return calculateSrsUseCase
    }
}
