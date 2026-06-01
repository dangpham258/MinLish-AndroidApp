package com.minlish.app.domain.auth.usecase

import com.minlish.app.domain.auth.model.User
import com.minlish.app.domain.auth.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        return authRepository.signUp(name, email, password)
    }
}
