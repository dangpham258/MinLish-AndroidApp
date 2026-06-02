package com.minlish.app.domain.model

import com.minlish.app.domain.model.enumration.InitialLevel
import com.minlish.app.domain.model.enumration.LearningGoal

data class User(
    val id: String = "",
    val name: String = "",
    val account: Account = Account(),
    val userProfile: UserProfile = UserProfile(),
    val userSetting: UserSetting = UserSetting(),
    val avatarIndex: Int = 0,
    val wordsLearned: Int = 0,
    val streak: Int = 0
)

data class Account(
    val email: String = "",
    val password: String = ""
)

data class UserProfile(
    val learningGoal: List<LearningGoal> = emptyList(),
    val initialLevel: InitialLevel = InitialLevel.B1,
    val emailNotification: Boolean = false,
    val dailyReminder: Boolean = true,
    val spacedRepetition: Boolean = true
)

data class UserSetting(
    val dailyNewWordGoal: Int = 10,
    val dailyReviewGoal: Int = 50
)
