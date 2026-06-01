package com.minlish.app.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor() {
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()
    
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()
    
    init {
        // Khoi tao voi user hien tai (neu co)
        refreshSession()
    }
    
    fun refreshSession() {
        val user = FirebaseAuth.getInstance().currentUser
        _currentUserId.value = user?.uid
        _currentUserEmail.value = user?.email
    }
    
    fun clearSession() {
        _currentUserId.value = null
        _currentUserEmail.value = null
    }
    
    fun getUserId(): String? = _currentUserId.value
    
    fun getUserEmail(): String? = _currentUserEmail.value
}
