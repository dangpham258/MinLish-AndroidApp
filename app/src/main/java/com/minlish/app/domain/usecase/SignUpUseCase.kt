package com.minlish.app.domain.usecase

import com.minlish.app.domain.model.User
import com.minlish.app.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        return authRepository.signUp(name, email, password)
    }
}
