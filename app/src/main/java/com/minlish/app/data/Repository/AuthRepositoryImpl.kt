package com.minlish.app.data.repository

import com.minlish.app.data.UserSession
import com.minlish.app.data.source.remote.FirebaseAuthApi
import com.minlish.app.data.source.remote.FirebaseDatabaseService
import com.minlish.app.domain.model.User
import com.minlish.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthApi: FirebaseAuthApi,
    private val firebaseDatabaseService: FirebaseDatabaseService,
    private val userSession: UserSession
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    override suspend fun login(email: String, password: String): Result<User> {
        val result = firebaseAuthApi.login(email, password)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            userSession.refreshSession()
            firebaseDatabaseService.createUserIfNotExists(
                uid = user.id,
                name = user.name,
                email = email
            )
            // Doc lai user tu Database de co day du thong tin (tags, userProfile, userSetting)
            val fullUser = firebaseDatabaseService.getUserFromSnapshot(user.id)
            _currentUser.value = fullUser ?: user
        }

        return result
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> {
        val result = firebaseAuthApi.signUp(name, email, password)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            userSession.refreshSession()
            firebaseDatabaseService.createUserIfNotExists(
                uid = user.id,
                name = name,
                email = email
            )
            val fullUser = firebaseDatabaseService.getUserFromSnapshot(user.id)
            _currentUser.value = fullUser ?: user
        }

        return result
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        val result = firebaseAuthApi.signInWithGoogle(idToken)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            userSession.refreshSession()
            firebaseDatabaseService.createUserIfNotExists(
                uid = user.id,
                name = user.name,
                email = user.account.email
            )
            val fullUser = firebaseDatabaseService.getUserFromSnapshot(user.id)
            _currentUser.value = fullUser ?: user
        }

        return result
    }

    override suspend fun logout() {
        firebaseAuthApi.signOut()
        userSession.clearSession()
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
