package com.minlish.app.data.repository

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.minlish.app.core.database.FirebaseDatabaseService
import com.minlish.app.domain.model.User
import com.minlish.app.domain.model.Notification
import com.minlish.app.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val firebaseService: FirebaseDatabaseService = FirebaseDatabaseService()
) : UserRepository {

    private val database = Firebase.database("https://minlish-1e2ec-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val TAG = "EmailTest"

    // THÔNG TIN EMAILJS THẬT CỦA BẠN
    private val SERVICE_ID = "service_6ejtzeu"
    private val TEMPLATE_ID = "template_bop7juu"
    private val PUBLIC_KEY = "j-7kBH1LA9u5r5py5"

    override suspend fun getUser(email: String): User? = firebaseService.getUser(email)
    override suspend fun getUserById(uid: String): User? = firebaseService.getUserById(uid)
    override suspend fun saveUser(user: User) = firebaseService.saveUser(user)
    override suspend fun updateUserField(email: String, path: String, value: Any) = 
        firebaseService.updateUserField(email, path, value)

    override suspend fun sendNotification(email: String, notification: Notification) {
        val sanitizedEmail = email.replace(".", ",")
        val notifRef = database.child("notifications").child(sanitizedEmail).push()
        val finalNotif = notification.copy(id = notifRef.key ?: "")
        notifRef.setValue(finalNotif).await()
    }

    override suspend fun triggerEmailReminder(email: String, message: String) {
        Log.d(TAG, "Bắt đầu gọi hàm gửi mail tới: $email")
        if (email.isBlank() || !email.contains("@")) {
            Log.e(TAG, "Email không hợp lệ: '$email'")
            return
        }
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.emailjs.com/api/v1.0/email/send")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.doInput = true

                val jsonBody = JSONObject().apply {
                    put("service_id", SERVICE_ID)
                    put("template_id", TEMPLATE_ID)
                    put("user_id", PUBLIC_KEY)
                    put("template_params", JSONObject().apply {
                        put("to_email", email) 
                        put("email", email) // Thêm cả key 'email' phòng trường hợp template dùng key này
                        put("message", message)
                    })
                }

                Log.d(TAG, "JSON Body: ${jsonBody.toString()}")

                val os = conn.outputStream
                os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                os.flush()
                os.close()
                
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "EmailJS SUCCESS: Đã gửi thư thành công! Response: $response")
                } else {
                    val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "EmailJS ERROR: Mã lỗi $responseCode - $errorMsg")
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "LỖI KẾT NỐI EMAILJS: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override suspend fun saveFcmToken(email: String, token: String) {
        firebaseService.updateUserField(email, "fcmToken", token)
    }
}
