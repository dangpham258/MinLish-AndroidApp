package com.minlish.app.presentation.dashboard.model

data class UserProgress(
    val currentLevelName: String = "Người Mới Bắt Đầu",
    val levelProgress: Int = 0,
    val nextLevelName: String = "...",
    val wordsCount: Int = 0,
    val streak: Int = 0,
    val retentionRate: Int = 85
)

data class ActivityReport(
    val dayName: String,
    val timeMinutes: Int
)

data class DailyPlanTelemetry(
    val newWordsCount: Int = 0,
    val reviewWordsCount: Int = 0
)
