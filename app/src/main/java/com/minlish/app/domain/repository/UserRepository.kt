package com.minlish.app.domain.repository

import com.minlish.app.domain.model.User
import com.minlish.app.domain.model.Notification

interface UserRepository {
    suspend fun getUser(email: String): User?
    suspend fun getUserById(uid: String): User?
    suspend fun saveUser(user: User)
    suspend fun updateProfileBasics(uid: String, name: String, initialLevel: String, avatarIndex: Int)
    suspend fun updateUserField(email: String, path: String, value: Any)
    suspend fun sendNotification(email: String, notification: Notification)
    suspend fun triggerEmailReminder(email: String, message: String)
    suspend fun saveFcmToken(email: String, token: String)
}
