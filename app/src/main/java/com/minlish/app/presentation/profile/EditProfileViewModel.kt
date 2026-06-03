package com.minlish.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.domain.model.User
import com.minlish.app.domain.model.UserProfile
import com.minlish.app.domain.model.enumration.InitialLevel
import com.minlish.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _name = MutableStateFlow("...")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _emailAddress = MutableStateFlow("")
    val emailAddress: StateFlow<String> = _emailAddress.asStateFlow()

    private val _avatarIndex = MutableStateFlow(0)
    val avatarIndex: StateFlow<Int> = _avatarIndex.asStateFlow()

    private val _level = MutableStateFlow("...")
    val level: StateFlow<String> = _level.asStateFlow()

    private val _levelTag = MutableStateFlow("-")
    val levelTag: StateFlow<String> = _levelTag.asStateFlow()

    private var currentUser: User? = null

    fun loadProfile(user: User) {
        currentUser = user
        _name.value = user.name
        _emailAddress.value = user.account.email
        _avatarIndex.value = user.userProfile.avatarIndex
        _level.value = user.userProfile.initialLevel.name
        _levelTag.value = user.userProfile.initialLevel.name
    }

    fun updateFullProfile(newName: String, newLevel: InitialLevel, newAvatarIdx: Int) {
        viewModelScope.launch {
            try {
                val userToSave = currentUser?.copy(
                    id = currentUser?.id ?: "",
                    name = newName,
                    userProfile = (currentUser?.userProfile ?: UserProfile()).copy(
                        initialLevel = newLevel,
                        avatarIndex = newAvatarIdx
                    )
                ) ?: User(
                    id = "",
                    name = newName,
                    userProfile = UserProfile(initialLevel = newLevel, avatarIndex = newAvatarIdx)
                )

                userRepository.saveUser(userToSave)
                currentUser = userToSave
                _name.value = newName
                _level.value = newLevel.name
                _levelTag.value = newLevel.name
                _avatarIndex.value = newAvatarIdx
                android.util.Log.d("EmailTest", "Profile updated for user ${userToSave.id}")
            } catch (e: Exception) {
                android.util.Log.e("EmailTest", "Profile update failed", e)
            }
        }
    }
}
