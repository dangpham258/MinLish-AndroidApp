package com.minlish.app.data.repository

import android.util.Log
import com.minlish.app.data.source.remote.FirebaseDatabaseService
import com.minlish.app.domain.model.Notification
import com.minlish.app.domain.model.User
import com.minlish.app.domain.model.enumration.LearningGoal
import com.minlish.app.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firebaseDatabaseService: FirebaseDatabaseService
) : UserRepository {

    private val tag = "EmailTest"
    private val serviceId = "service_6ejtzeu"
    private val templateId = "template_bop7juu"
    private val publicKey = "j-7kBH1LA9u5r5py5"

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
            ),
            "userProfile" to mapOf(
                "tags" to user.userProfile.tags.map { it.value },
                "initialLevel" to user.userProfile.initialLevel.value,
                "emailNotification" to user.userProfile.emailNotification,
                "dailyReminder" to user.userProfile.dailyReminder,
                "spacedRepetition" to user.userProfile.spacedRepetition,
                "avatarIndex" to user.userProfile.avatarIndex,
                "wordsLearned" to user.userProfile.wordsLearned,
                "streak" to user.userProfile.streak
            ),
            "userSetting" to mapOf(
                "dailyNewWordGoal" to user.userSetting.dailyNewWordGoal,
                "dailyReviewGoal" to user.userSetting.dailyReviewGoal
            )
        ))
    }

    override suspend fun updateProfileBasics(
        uid: String,
        name: String,
        initialLevel: String,
        avatarIndex: Int
    ) {
        if (uid.isBlank()) return
        firebaseDatabaseService.updateUser(uid, mapOf(
            "name" to name,
            "userProfile/initialLevel" to initialLevel,
            "userProfile/avatarIndex" to avatarIndex
        ))
    }

    override suspend fun updateUserField(email: String, path: String, value: Any) {
        val userId = resolveUserId(email)
        if (userId == null) {
            Log.e(tag, "Update user field failed: cannot resolve user id for $email")
            return
        }

        val firebaseValue = value.toFirebaseValue()
        firebaseDatabaseService.updateUserField(userId, path, firebaseValue)
        Log.d(tag, "Updated Firebase path users/$userId/$path value=$firebaseValue")
    }

    override suspend fun sendNotification(email: String, notification: Notification) {
        val userId = resolveUserId(email)
        if (userId == null) {
            Log.e(tag, "Create notification failed: cannot resolve user id for $email")
            return
        }

        firebaseDatabaseService.createNotification(userId, notification)
        Log.d(tag, "Created notification for $userId")
    }

    override suspend fun triggerEmailReminder(email: String, message: String) {
        Log.d(tag, "Bắt đầu gọi hàm gửi mail tới: $email")
        if (email.isBlank() || !email.contains("@")) {
            Log.e(tag, "Email không hợp lệ: '$email'")
            return
        }

        withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://api.emailjs.com/api/v1.0/email/send")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.doInput = true

                val jsonBody = JSONObject().apply {
                    put("service_id", serviceId)
                    put("template_id", templateId)
                    put("user_id", publicKey)
                    put("template_params", JSONObject().apply {
                        put("to_email", email)
                        put("email", email)
                        put("message", message)
                    })
                }

                Log.d(tag, "EmailJS request body: $jsonBody")
                conn.outputStream.use { outputStream ->
                    outputStream.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    Log.d(tag, "EmailJS SUCCESS: Đã gửi thư thành công! Response: $response")
                } else {
                    val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(tag, "EmailJS ERROR: Mã lỗi $responseCode - $errorMsg")
                }
            } catch (e: Exception) {
                Log.e(tag, "LỖI KẾT NỐI EMAILJS", e)
            } finally {
                conn?.disconnect()
            }
        }
    }

    override suspend fun saveFcmToken(email: String, token: String) {
        // TODO: Implement
    }

    private suspend fun resolveUserId(identifier: String): String? {
        if (identifier.isBlank()) return null
        if (!identifier.contains("@")) return identifier
        return firebaseDatabaseService.findUserIdByEmail(identifier)
    }

    private fun Any.toFirebaseValue(): Any {
        return when (this) {
            is LearningGoal -> value
            is Iterable<*> -> mapNotNull { item ->
                when (item) {
                    is LearningGoal -> item.value
                    null -> null
                    else -> item.toString()
                }
            }
            else -> this
        }
    }
}
