package com.minlish.app.data.auth.repository

import com.minlish.app.data.auth.datasource.FirebaseAuthApi
import com.minlish.app.data.auth.datasource.FirebaseDatabaseService
import com.minlish.app.domain.auth.model.User
import com.minlish.app.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthApi: FirebaseAuthApi,
    private val firebaseDatabaseService: FirebaseDatabaseService
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    override suspend fun login(email: String, password: String): Result<User> {
        val result = firebaseAuthApi.login(email, password)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            // Dam bao user co day du cac field trong DB
            firebaseDatabaseService.createUserIfNotExists(
                uid = user.id,
                name = user.name,
                email = email
            )
            _currentUser.value = user
        }

        return result
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> {
        val result = firebaseAuthApi.signUp(name, email, password)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            firebaseDatabaseService.createUserIfNotExists(
                uid = user.id,
                name = name,
                email = email
            )
            _currentUser.value = user
        }

        return result
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        val result = firebaseAuthApi.signInWithGoogle(idToken)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            firebaseDatabaseService.createUserIfNotExists(
                uid = user.id,
                name = user.name,
                email = user.account.email
            )
            _currentUser.value = user
        }

        return result
    }

    override suspend fun logout() {
        firebaseAuthApi.signOut()
        _currentUser.value = null
    }

    override fun getAuthenticatedUser(): Flow<User?> {
        return _currentUser.asStateFlow()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return firebaseAuthApi.sendPasswordResetEmail(email)
    }

    override suspend fun verifyPasswordResetCode(oobCode: String): Result<String> {
        return firebaseAuthApi.verifyPasswordResetCode(oobCode)
    }

    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> {
        return firebaseAuthApi.confirmPasswordReset(oobCode, newPassword)
    }
}
