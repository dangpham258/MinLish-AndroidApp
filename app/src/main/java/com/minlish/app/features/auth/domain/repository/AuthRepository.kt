package com.minlish.app.features.auth.domain.repository

import com.minlish.app.features.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signUp(name: String, email: String, password: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun logout()
    fun getAuthenticatedUser(): Flow<User?>
    
    // Password Reset
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun verifyPasswordResetCode(oobCode: String): Result<String>
    suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit>
}
