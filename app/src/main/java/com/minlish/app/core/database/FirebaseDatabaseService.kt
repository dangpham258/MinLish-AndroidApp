package com.minlish.app.core.database

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.minlish.app.domain.model.User
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseService {
    private val database = Firebase.database("https://minlish-1e2ec-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val usersRef = database.child("users")

    private fun sanitizeEmail(email: String): String = email.replace(".", ",")

    suspend fun saveUser(user: User) {
        // Ưu tiên dùng user.id (UID) làm Key, nếu không có mới dùng Email
        val key = if (user.id.isNotBlank()) user.id else sanitizeEmail(user.account.email)
        if (key.isBlank()) return
        try {
            usersRef.child(key).setValue(user).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUser(email: String): User? {
        if (email.isBlank()) return null
        return try {
            val snapshot = usersRef.orderByChild("account/email")
                .equalTo(email)
                .get()
                .await()
            val userNode = snapshot.children.firstOrNull()
            userNode?.getValue(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserById(uid: String): User? {
        if (uid.isBlank()) return null
        return try {
            val snapshot = usersRef.child(uid).get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUserField(email: String, path: String, value: Any) {
        if (email.isBlank()) return
        try {
            usersRef.child(sanitizeEmail(email)).child(path).setValue(value).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
