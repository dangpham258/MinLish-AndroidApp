package com.minlish.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.minlish.app.data.repository.UserRepositoryImpl
import com.minlish.app.domain.model.User
import com.minlish.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userEmail = auth.currentUser?.email ?: ""

    private val _wordsLearned = MutableStateFlow(0)
    val wordsLearned: StateFlow<Int> = _wordsLearned.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            android.util.Log.d("EmailTest", "ProfileViewModel - Đang tải dữ liệu cho UID: ${currentUser.uid}")
            loadBaseProfileById(currentUser.uid)
        } else {
            // Trường hợp chưa đăng nhập, dùng UID của "Lê Thảo est" để bạn dễ test giao diện
            val testUid = "a0H3rLFOXwOtjhBFUxhrvJE7C8A3"
            android.util.Log.d("EmailTest", "ProfileViewModel - Chưa login, dùng UID test: $testUid")
            loadBaseProfileById(testUid)
        }
    }

    private fun loadBaseProfileById(uid: String) {
        viewModelScope.launch {
            val user = userRepository.getUserById(uid)
            handleUserResult(user)
        }
    }

    private fun loadBaseProfileByEmail(email: String) {
        viewModelScope.launch {
            val user = userRepository.getUser(email)

            handleUserResult(user)
        }
    }

    private fun handleUserResult(user: User?) {
        if (user != null) {
            android.util.Log.d("EmailTest", "Tải profile THÀNH CÔNG: ${user.name}")
            _userData.value = user
            _wordsLearned.value = user.userProfile.wordsLearned
            _streak.value = user.userProfile.streak
        } else {
            android.util.Log.e("EmailTest", "Tải profile THẤT BẠI")
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        auth.signOut()
    }

    fun login(email: String, name: String) {
        // Logic login/register demo
        _isLoggedIn.value = true
    }
}
