package com.minlish.app.data.auth.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.minlish.app.domain.auth.model.Account
import com.minlish.app.domain.auth.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthApi @Inject constructor() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun signUp(name: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser == null) {
                return@withContext Result.failure(Exception("Tạo tài khoản thất bại"))
            }

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            Result.success(
                User(
                    id = firebaseUser.uid,
                    name = name,
                    account = Account(email = email, password = "")
                )
            )
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("EMAIL_EXISTS") == true -> "Email đã được đăng ký"
                e.message?.contains("INVALID_EMAIL") == true -> "Email không hợp lệ"
                e.message?.contains("WEAK_PASSWORD") == true -> "Mật khẩu quá yếu (ít nhất 6 ký tự)"
                e.message?.contains("network") == true -> "Lỗi mạng, kiểm tra kết nối"
                else -> e.message ?: "Đăng ký thất bại"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser == null) {
                return@withContext Result.failure(Exception("Đăng nhập thất bại"))
            }

            Result.success(
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: email.substringBefore("@"),
                    account = Account(email = email, password = "")
                )
            )
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("INVALID_EMAIL") == true -> "Email không hợp lệ"
                e.message?.contains("INVALID_PASSWORD") == true -> "Mật khẩu không đúng"
                e.message?.contains("EMAIL_NOT_FOUND") == true -> "Email chưa được đăng ký"
                e.message?.contains("network") == true -> "Lỗi mạng, kiểm tra kết nối"
                else -> e.message ?: "Đăng nhập thất bại"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            
            if (firebaseUser == null) {
                return@withContext Result.failure(Exception("Đăng nhập Google thất bại"))
            }

            Result.success(
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Google User",
                    account = Account(email = firebaseUser.email ?: "", password = "")
                )
            )
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("INVALID_IDP") == true -> "Tài khoản Google không hợp lệ"
                e.message?.contains("network") == true -> "Lỗi mạng, kiểm tra kết nối"
                else -> e.message ?: "Đăng nhập Google thất bại"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("EMAIL_NOT_FOUND") == true -> "Email chưa được đăng ký"
                else -> e.message ?: "Không thể gửi email reset"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun verifyPasswordResetCode(oobCode: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val email = firebaseAuth.verifyPasswordResetCode(oobCode).await()
            Result.success(email)
        } catch (e: Exception) {
            Result.failure(Exception("Link không hợp lệ hoặc đã hết hạn"))
        }
    }

    suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.confirmPasswordReset(oobCode, newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Không thể đặt lại mật khẩu"))
        }
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun getIdToken(): String? = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }
}
