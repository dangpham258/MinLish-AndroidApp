package com.minlish.app.features.auth.domain.usecase

import com.minlish.app.features.auth.domain.model.User
import com.minlish.app.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("All fields are required"))
        }
        return repository.signUp(name, email, password)
    }
}
