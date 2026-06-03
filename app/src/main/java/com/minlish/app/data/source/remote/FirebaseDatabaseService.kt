package com.minlish.app.data.source.remote

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.minlish.app.domain.model.User
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseService {
    private val database = Firebase.database("https://minlish-1e2ec-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val usersRef = database.child("users")

    suspend fun saveUser(user: User) {
        // RÀO CHẮN: Nếu ID rỗng, không cho phép lưu để tránh làm hỏng cấu trúc "sổ xuống"
        if (user.id.isBlank()) {
            println("ERROR: Không thể lưu User vì ID bị rỗng!")
            return
        }

        try {
            // Đảm bảo dữ liệu nằm trong users/[userId]
            usersRef.child(user.id).setValue(user).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUser(uid: String): User? {
        if (uid.isBlank()) return null
        return try {
            val snapshot = usersRef.child(uid).get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUserField(uid: String, path: String, value: Any) {
        if (uid.isBlank()) return
        try {
            usersRef.child(uid).child(path).setValue(value).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}