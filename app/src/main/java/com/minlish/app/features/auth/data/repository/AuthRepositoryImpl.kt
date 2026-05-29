package com.minlish.app.features.auth.data.repository

import com.minlish.app.core.database.FirebaseDatabaseService
import com.minlish.app.core.network.FirebaseAuthApi
import com.minlish.app.features.auth.data.source.AuthLocalDataSource
import com.minlish.app.features.auth.domain.model.User
import com.minlish.app.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authLocalDataSource: AuthLocalDataSource,
    private val firebaseAuthApi: FirebaseAuthApi,
    private val firebaseDatabaseService: FirebaseDatabaseService
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    override suspend fun login(email: String, password: String): Result<User> {
        val result = firebaseAuthApi.login(email, password)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            authLocalDataSource.saveTokens(user.accessToken ?: "", user.refreshToken ?: "")
            _currentUser.value = user
        }

        return result
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> {
        val result = firebaseAuthApi.signUp(name, email, password)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            // Tao user trong Realtime Database
            firebaseDatabaseService.createUserIfNotExists(
                uid = user.uid,
                name = name,
                email = email
            )
            authLocalDataSource.saveTokens(user.accessToken ?: "", user.refreshToken ?: "")
            _currentUser.value = user
        }

        return result
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        val result = firebaseAuthApi.signInWithGoogle(idToken)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            // Tao user trong Realtime Database
            firebaseDatabaseService.createUserIfNotExists(
                uid = user.uid,
                name = user.name,
                email = user.email
            )
            authLocalDataSource.saveTokens(user.accessToken ?: "", user.refreshToken ?: "")
            _currentUser.value = user
        }

        return result
    }

    override suspend fun logout() {
        authLocalDataSource.clear()
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
