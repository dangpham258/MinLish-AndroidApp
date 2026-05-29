package com.minlish.app.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.core.util.Constants
import com.minlish.app.core.util.GoogleAuthManager
import com.minlish.app.features.auth.domain.repository.AuthRepository
import com.minlish.app.features.auth.domain.usecase.GetAuthStateUseCase
import com.minlish.app.features.auth.domain.usecase.LoginUseCase
import com.minlish.app.features.auth.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val googleAuthManager: GoogleAuthManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _isUserLoggedIn.value = getAuthStateUseCase()
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onLogin(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = loginUseCase(email, password)
            _isLoading.value = false
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.AuthSuccess)
            } else {
                _eventFlow.emit(UiEvent.ShowError(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }

    fun onSignUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = signUpUseCase(name, email, password)
            _isLoading.value = false
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.AuthSuccess)
            } else {
                _eventFlow.emit(UiEvent.ShowError(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }

    fun onGoogleSignIn() {
        viewModelScope.launch {
            _isLoading.value = true
            val idToken = googleAuthManager.signIn(Constants.GOOGLE_SERVER_CLIENT_ID)
            if (idToken != null) {
                val result = authRepository.loginWithGoogle(idToken)
                if (result.isSuccess) {
                    _eventFlow.emit(UiEvent.AuthSuccess)
                } else {
                    _eventFlow.emit(UiEvent.ShowError(result.exceptionOrNull()?.message ?: "Đăng nhập Google thất bại"))
                }
            } else {
                _eventFlow.emit(UiEvent.ShowError("Không tìm thấy tài khoản Google. Vui lòng thêm tài khoản Google trong Cài đặt > Tài khoản > Thêm tài khoản > Google"))
            }
            _isLoading.value = false
        }
    }

    // Firebase Auth Password Reset
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        _isLoading.value = true
        val result = authRepository.sendPasswordResetEmail(email)
        _isLoading.value = false
        return result
    }

    // Xác minh reset code từ deep link
    suspend fun verifyResetCode(oobCode: String): Result<String> {
        _isLoading.value = true
        val result = authRepository.verifyPasswordResetCode(oobCode)
        _isLoading.value = false
        return result
    }

    // Đặt lại mật khẩu với code từ deep link
    suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> {
        _isLoading.value = true
        val result = authRepository.confirmPasswordReset(oobCode, newPassword)
        _isLoading.value = false
        return result
    }

    sealed class UiEvent {
        object AuthSuccess : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}
