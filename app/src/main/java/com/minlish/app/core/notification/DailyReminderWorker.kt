package com.minlish.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import com.minlish.app.R
import com.minlish.app.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID).orEmpty()
        val fallbackEmail = inputData.getString(KEY_EMAIL).orEmpty()
        Log.d("DailyReminder", "Worker started userId=$userId")
        if (userId.isBlank()) return Result.success()

        return try {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId)
            val reminderStateRef = database.getReference("reminderStates").child(userId)
            val snapshot = userRef.get().await()
            if (!snapshot.exists()) return Result.success()

            val dailyReminder = snapshot.child("userProfile/dailyReminder").getValue(Boolean::class.java) ?: false
            val emailNotification = snapshot.child("userProfile/emailNotification").getValue(Boolean::class.java) ?: false
            Log.d(
                "DailyReminder",
                "Settings dailyReminder=$dailyReminder emailNotification=$emailNotification"
            )
            if (!dailyReminder) {
                Log.d("DailyReminder", "Daily reminder disabled, worker exits")
                return Result.success()
            }

            val email = snapshot.child("account/email").getValue(String::class.java) ?: fallbackEmail
            val today = todayKey()
            val title = "MinLish study reminder"
            val content = "Take a few minutes to review today's vocabulary."
            Log.d("DailyReminder", "Resolved reminder email=$email today=$today")

            val reminderStateSnapshot = reminderStateRef.get().await()
            val lastAppDate = reminderStateSnapshot.child("lastReminderSentDate").getValue(String::class.java)
            val lastEmailDate = reminderStateSnapshot.child("lastEmailSentDate").getValue(String::class.java)
            Log.d(
                "DailyReminder",
                "Reminder state lastReminderSentDate=$lastAppDate lastEmailSentDate=$lastEmailDate"
            )
            if (lastAppDate != today) {
                showLocalNotification(title, content)
                createAppNotification(database, userId, title, content)
                reminderStateRef.child("lastReminderSentDate").setValue(today).await()
                Log.d("DailyReminder", "Created system/app notification for date=$today")
            } else {
                Log.d("DailyReminder", "App notification already sent for date=$today")
            }

            if (emailNotification && email.isNotBlank()) {
                if (lastEmailDate != today) {
                    sendEmail(email, content)
                    reminderStateRef.child("lastEmailSentDate").setValue(today).await()
                    Log.d("DailyReminder", "Sent reminder email to $email for date=$today")
                } else {
                    Log.d("DailyReminder", "Email already sent for date=$today")
                }
            } else {
                Log.d("DailyReminder", "Email reminder skipped")
            }

            DailyReminderScheduler.scheduleNextAfterWorker(applicationContext, userId, email)
            Result.success()
        } catch (e: Exception) {
            Log.e("DailyReminder", "Worker failed", e)
            Result.retry()
        }
    }

    private fun showLocalNotification(title: String, content: String) {
        createChannel()
        Log.d(
            "DailyReminder",
            "Notifications enabled=${NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()}"
        )

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(REMINDER_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ may block notifications until the runtime permission is granted.
            Log.e("DailyReminder", "Notification permission is not granted")
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily MinLish study reminders"
        }
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private suspend fun createAppNotification(
        database: FirebaseDatabase,
        userId: String,
        title: String,
        content: String
    ) {
        val ref = database.getReference("notifications").child(userId).push()
        val id = ref.key ?: UUID.randomUUID().toString()
        val data = mapOf(
            "id" to id,
            "title" to title,
            "content" to content,
            "isRead" to false,
            "createdAt" to com.google.firebase.database.ServerValue.TIMESTAMP
        )
        ref.setValue(data).await()
    }

    private suspend fun sendEmail(email: String, message: String) = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = (URL("https://api.emailjs.com/api/v1.0/email/send").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                doInput = true
            }

            val body = JSONObject().apply {
                put("service_id", "service_6ejtzeu")
                put("template_id", "template_bop7juu")
                put("user_id", "j-7kBH1LA9u5r5py5")
                put("template_params", JSONObject().apply {
                    put("to_email", email)
                    put("email", email)
                    put("message", message)
                })
            }

            conn.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            val responseBody = if (responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            Log.d("DailyReminder", "EmailJS responseCode=$responseCode response=$responseBody")

            if (responseCode !in 200..299) {
                throw IllegalStateException("EmailJS failed: $responseCode $responseBody")
            }
        } finally {
            conn?.disconnect()
        }
    }

    private fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"
        private const val CHANNEL_ID = "minlish_daily_reminders"
        private const val REMINDER_NOTIFICATION_ID = 1001
    }
}
