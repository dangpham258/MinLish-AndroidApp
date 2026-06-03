package com.minlish.app.data.repository

import com.minlish.app.data.source.remote.FirebaseDatabaseService
import com.minlish.app.domain.model.Notification
import com.minlish.app.domain.model.User
import com.minlish.app.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseDatabaseService: FirebaseDatabaseService
) : UserRepository {

    override suspend fun getUser(email: String): User? {
        // Lấy user bằng email - tìm trong database
        return null // TODO: Implement nếu cần
    }

    override suspend fun getUserById(uid: String): User? {
        return firebaseDatabaseService.getUserFromSnapshot(uid)
    }

    override suspend fun saveUser(user: User) {
        firebaseDatabaseService.updateUser(user.id, mapOf(
            "name" to user.name,
            "account" to mapOf(
                "email" to user.account.email,
                "password" to user.account.password
            )
        ))
    }

    override suspend fun updateUserField(email: String, path: String, value: Any) {
        // TODO: Implement bằng cách tìm user theo email trước
    }

    override suspend fun sendNotification(email: String, notification: Notification) {
        // Implementation if needed
    }

    override suspend fun triggerEmailReminder(email: String, message: String) {
        // Implementation if needed
    }

    override suspend fun saveFcmToken(email: String, token: String) {
        // TODO: Implement
    }
}
