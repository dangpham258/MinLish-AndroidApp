package com.minlish.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.minlish.app.data.repository.UserRepositoryImpl
import com.minlish.app.domain.model.Notification
import com.minlish.app.domain.model.User
import com.minlish.app.domain.model.enumration.LearningGoal
import com.minlish.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {

    private val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "23110321@student.hcmute.edu.vn"
    private var currentUid: String = "" // Thêm biến lưu UID

    private val _emailNotification = MutableStateFlow(false)
    val emailNotification: StateFlow<Boolean> = _emailNotification.asStateFlow()

    private val _dailyReminder = MutableStateFlow(false)
    val dailyReminder: StateFlow<Boolean> = _dailyReminder.asStateFlow()

    private val _spacedRepetition = MutableStateFlow(false)
    val spacedRepetition: StateFlow<Boolean> = _spacedRepetition.asStateFlow()

    private val _selectedGoals = MutableStateFlow(setOf<String>())
    val selectedGoals: StateFlow<Set<String>> = _selectedGoals.asStateFlow()

    fun loadSettings(user: User) {
        currentUid = user.id // Lưu lại UID từ user object
        val profile = user.userProfile
        _emailNotification.value = profile.emailNotification
        _dailyReminder.value = profile.dailyReminder
        _spacedRepetition.value = profile.spacedRepetition
        _selectedGoals.value = profile.tags.map { it.name }.toSet()

        // Tự động kiểm tra và gửi mail nếu cả 2 đã bật sẵn khi load app
        if (profile.dailyReminder && profile.emailNotification) {
            viewModelScope.launch {
                val targetEmail = user.account.email.ifBlank { userEmail }
                android.util.Log.d("EmailTest", "App Start: Đã bật sẵn, gửi mail tới $targetEmail")
                userRepository.triggerEmailReminder(
                    targetEmail,
                    "Chào mừng bạn quay lại! Đừng quên học tập cùng MinLish hôm nay nhé 🐰"
                )
            }
        }
    }
    fun toggleEmailNotification(enabled: Boolean, currentEmail: String) {
        _emailNotification.value = enabled
        updateSettings("emailNotification", enabled, currentEmail)
    }

    fun setDailyReminder(enabled: Boolean, currentEmail: String) {
        _dailyReminder.value = enabled
        updateSettings("dailyReminder", enabled, currentEmail)
    }

    fun setSpacedRepetition(enabled: Boolean) {
        _spacedRepetition.value = enabled
        viewModelScope.launch {
            val identifier = if (currentUid.isNotBlank()) currentUid else userEmail
            userRepository.updateUserField(identifier, "userProfile/spacedRepetition", enabled)
        }
    }

    private fun updateSettings(field: String, value: Boolean, email: String) {
        viewModelScope.launch {
            val identifier = if (currentUid.isNotBlank()) currentUid else userEmail
            userRepository.updateUserField(identifier, "userProfile/$field", value)
            
            val daily = _dailyReminder.value
            val notify = _emailNotification.value
            android.util.Log.d("EmailTest", "Update settings: $field=$value, Daily=$daily, Notify=$notify, EmailArg=$email")

            // Nếu cả 2 nút cùng bật -> Gửi mail trực tiếp từ App qua EmailJS
            if (daily && notify) {
                val targetEmail = if (email.isNotBlank()) email else userEmail
                val message = "Bạn ơi ! Bạn đã quên minlish rồi sao. Hãy ghé ứng dụng để học tập nào"
                
                android.util.Log.d("EmailTest", "Điều kiện thỏa mãn, chuẩn bị gửi tới: $targetEmail")
                userRepository.triggerEmailReminder(targetEmail, message)
                
                userRepository.sendNotification(userEmail, Notification(
                    title = "Nhắc nhở học tập 🐰",
                    content = message,
                    isRead = false
                ))
            }
        }
    }

    fun toggleGoal(goalName: String) {
        val current = _selectedGoals.value.toMutableSet()
        if (current.contains(goalName)) {
            if (current.size > 1) current.remove(goalName)
        } else {
            current.add(goalName)
        }
        _selectedGoals.value = current
        viewModelScope.launch {
            val goals = current.mapNotNull { 
                try { LearningGoal.valueOf(it) } catch (e: Exception) { null } 
            }
            val identifier = if (currentUid.isNotBlank()) currentUid else userEmail
            userRepository.updateUserField(identifier, "userProfile/tags", goals)
        }
    }
}
