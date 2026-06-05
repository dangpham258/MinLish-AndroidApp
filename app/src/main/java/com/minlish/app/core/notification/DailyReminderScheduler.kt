package com.minlish.app.core.notification

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DailyReminderScheduler {
    private const val WORK_NAME = "minlish_daily_reminder"
    private const val REMINDER_HOUR = 16
    private const val REMINDER_MINUTE = 55

    fun scheduleIfAbsent(context: Context, userId: String, email: String) {
        schedule(context, userId, email, ExistingWorkPolicy.KEEP)
    }

    fun forceReschedule(context: Context, userId: String, email: String) {
        schedule(context, userId, email, ExistingWorkPolicy.REPLACE)
    }

    fun scheduleNextAfterWorker(context: Context, userId: String, email: String) {
        schedule(context, userId, email, ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    fun schedule(context: Context, userId: String, email: String) {
        forceReschedule(context, userId, email)
    }

    private fun schedule(context: Context, userId: String, email: String, policy: ExistingWorkPolicy) {
        if (userId.isBlank()) return

        val initialDelayMs = calculateInitialDelayMs()
        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    DailyReminderWorker.KEY_USER_ID to userId,
                    DailyReminderWorker.KEY_EMAIL to email
                )
            )
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            WORK_NAME,
            policy,
            request
        )
        Log.d(
            "DailyReminder",
            "Scheduled daily reminder for user=$userId at %02d:%02d, initialDelayMs=$initialDelayMs, policy=$policy"
                .format(REMINDER_HOUR, REMINDER_MINUTE)
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
        Log.d("DailyReminder", "Cancelled daily reminder work")
    }

    private fun calculateInitialDelayMs(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return target.timeInMillis - now.timeInMillis
    }
}
