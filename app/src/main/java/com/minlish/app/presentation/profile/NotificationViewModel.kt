package com.minlish.app.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.minlish.app.core.notification.DailyReminderScheduler
import com.minlish.app.domain.model.User
import com.minlish.app.domain.model.enumration.LearningGoal
import com.minlish.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userRepository: UserRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userEmail = auth.currentUser?.email.orEmpty()
    private val authUid = auth.currentUser?.uid.orEmpty()
    private var currentUid: String = ""
    private var hasSyncedReminderSchedule = false

    private val _emailNotification = MutableStateFlow(false)
    val emailNotification: StateFlow<Boolean> = _emailNotification.asStateFlow()

    private val _dailyReminder = MutableStateFlow(false)
    val dailyReminder: StateFlow<Boolean> = _dailyReminder.asStateFlow()

    private val _spacedRepetition = MutableStateFlow(false)
    val spacedRepetition: StateFlow<Boolean> = _spacedRepetition.asStateFlow()

    private val _selectedGoals = MutableStateFlow(setOf<String>())
    val selectedGoals: StateFlow<Set<String>> = _selectedGoals.asStateFlow()

    fun loadSettings(user: User) {
        currentUid = user.id.ifBlank { authUid }
        val profile = user.userProfile
        _emailNotification.value = profile.emailNotification
        _dailyReminder.value = profile.dailyReminder
        _spacedRepetition.value = profile.spacedRepetition
        _selectedGoals.value = profile.tags.map { it.name }.toSet()
        if (!hasSyncedReminderSchedule) {
            syncDailyReminderSchedule()
            hasSyncedReminderSchedule = true
        }
        android.util.Log.d("EmailTest", "Loaded profile settings for uid=$currentUid tags=${_selectedGoals.value}")
    }

    fun toggleEmailNotification(enabled: Boolean, currentEmail: String) {
        _emailNotification.value = enabled
        updateSettings("emailNotification", enabled, currentEmail)
    }

    fun setDailyReminder(enabled: Boolean, currentEmail: String) {
        _dailyReminder.value = enabled
        updateSettings("dailyReminder", enabled, currentEmail)
        updateDailyReminderScheduleFromToggle()
        hasSyncedReminderSchedule = true
    }

    fun setSpacedRepetition(enabled: Boolean) {
        _spacedRepetition.value = enabled
        viewModelScope.launch {
            val identifier = resolveIdentifier()
            if (identifier.isNotBlank()) {
                userRepository.updateUserField(identifier, "userProfile/spacedRepetition", enabled)
            }
        }
    }

    private fun updateSettings(field: String, value: Boolean, email: String) {
        viewModelScope.launch {
            try {
                val identifier = resolveIdentifier()
                if (identifier.isBlank()) return@launch
                userRepository.updateUserField(identifier, "userProfile/$field", value)
                android.util.Log.d("EmailTest", "Updated reminder setting: $field=$value, email=$email")
            } catch (e: Exception) {
                android.util.Log.e("EmailTest", "Update settings failed", e)
            }
        }
    }

    private fun syncDailyReminderSchedule() {
        val userId = currentUid.ifBlank { authUid }
        if (_dailyReminder.value && userId.isNotBlank()) {
            DailyReminderScheduler.scheduleIfAbsent(appContext, userId, userEmail)
        } else {
            DailyReminderScheduler.cancel(appContext)
        }
    }

    private fun updateDailyReminderScheduleFromToggle() {
        val userId = currentUid.ifBlank { authUid }
        if (_dailyReminder.value && userId.isNotBlank()) {
            DailyReminderScheduler.forceReschedule(appContext, userId, userEmail)
        } else {
            DailyReminderScheduler.cancel(appContext)
        }
    }

    fun toggleGoal(goalName: String, onSynced: () -> Unit = {}) {
        val current = _selectedGoals.value.toMutableSet()
        if (current.contains(goalName)) {
            if (current.size > 1) current.remove(goalName)
        } else {
            current.add(goalName)
        }
        _selectedGoals.value = current
        viewModelScope.launch {
            try {
                val goals = current.mapNotNull {
                    try {
                        LearningGoal.valueOf(it)
                    } catch (_: Exception) {
                        null
                    }
                }
                val identifier = resolveIdentifier()
                if (identifier.isBlank()) return@launch
                android.util.Log.d("EmailTest", "Updating tags for uid=$identifier tags=${goals.map { it.value }}")
                userRepository.updateUserField(identifier, "userProfile/tags", goals)
                android.util.Log.d("EmailTest", "Updated tags successfully for uid=$identifier")
                onSynced()
            } catch (e: Exception) {
                android.util.Log.e("EmailTest", "Update tags failed", e)
            }
        }
    }

    private fun resolveIdentifier(): String {
        return currentUid.ifBlank { authUid.ifBlank { userEmail } }
    }
}
