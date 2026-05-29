package com.minlish.app.features.auth.domain.usecase

import com.minlish.app.features.auth.data.source.AuthLocalDataSource
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor(
    private val localDataSource: AuthLocalDataSource
) {
    operator fun invoke(): Boolean {
        return !localDataSource.getAccessToken().isNullOrBlank()
    }
}
