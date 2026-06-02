package com.minlish.app.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.domain.repository.AuthRepository
import com.minlish.app.domain.usecase.GetAuthStateUseCase
import com.minlish.app.domain.usecase.LoginUseCase
import com.minlish.app.domain.usecase.SignUpUseCase
import com.minlish.app.core.util.GoogleAuthManager
import com.google.firebase.auth.FirebaseAuth
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

    private val auth = FirebaseAuth.getInstance()

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        checkAuthState()
        googleAuthManager.initialize()
    }

    private fun checkAuthState() {
        // Xoa session cu de luon yeu cau dang nhap thu cong
        auth.signOut()
        googleAuthManager.signOutSilently()
        _isUserLoggedIn.value = false
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
                _isUserLoggedIn.value = true
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
                _isUserLoggedIn.value = true
                _eventFlow.emit(UiEvent.AuthSuccess)
            } else {
                _eventFlow.emit(UiEvent.ShowError(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }

    fun onGoogleSignInResult(
        idToken: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (idToken == null) {
            onError("Đăng nhập Google thất bại")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.loginWithGoogle(idToken)
            _isLoading.value = false
            if (result.isSuccess) {
                _isUserLoggedIn.value = true
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Đăng nhập Google thất bại")
            }
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        googleAuthManager.signOut {
            viewModelScope.launch {
                authRepository.logout()
                _isUserLoggedIn.value = false
                onComplete()
            }
        }
    }

    fun getGoogleAuthManager() = googleAuthManager

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        _isLoading.value = true
        val result = authRepository.sendPasswordResetEmail(email)
        _isLoading.value = false
        return result
    }

    suspend fun verifyResetCode(oobCode: String): Result<String> {
        _isLoading.value = true
        val result = authRepository.verifyPasswordResetCode(oobCode)
        _isLoading.value = false
        return result
    }

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
