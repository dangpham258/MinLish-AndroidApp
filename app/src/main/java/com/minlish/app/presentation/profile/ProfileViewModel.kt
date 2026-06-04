package com.minlish.app.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.minlish.app.core.notification.DailyReminderScheduler
import com.minlish.app.domain.model.User
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userRepository: UserRepository,
    private val bunnyRepository: BunnyRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _wordsLearned = MutableStateFlow(0)
    val wordsLearned: StateFlow<Int> = _wordsLearned.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.w("EmailTest", "ProfileViewModel - No signed-in user")
            _isLoggedIn.value = false
            _userData.value = null
            _wordsLearned.value = 0
            _streak.value = 0
            return
        }

        _isLoggedIn.value = true
        viewModelScope.launch {
            val user = userRepository.getUserById(currentUser.uid)
            handleUserResult(user)

            val progress = bunnyRepository.getUserProgress(currentUser.uid)
            if (progress != null) {
                _wordsLearned.value = progress.wordsCount
                _streak.value = progress.streak
            }
        }
    }

    private fun handleUserResult(user: User?) {
        if (user != null) {
            android.util.Log.d("EmailTest", "Loaded profile successfully: ${user.name}")
            _userData.value = user
            _wordsLearned.value = user.userProfile.wordsLearned
            _streak.value = user.userProfile.streak
        } else {
            android.util.Log.e("EmailTest", "Load profile failed")
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        DailyReminderScheduler.cancel(appContext)
        auth.signOut()
    }

    fun login(email: String, name: String) {
        _isLoggedIn.value = true
        refreshProfile()
    }
}
