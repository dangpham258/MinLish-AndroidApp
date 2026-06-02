package com.minlish.app.domain.model
import com.minlish.app.domain.model.enumration.LearningGoal

import com.minlish.app.domain.model.enumration.InitialLevel


data class User(
    val id: String = "",
    val name: String = "",
    val account: Account = Account(),
    val userProfile: UserProfile = UserProfile(),
    val userSetting: UserSetting = UserSetting()
)

data class Account(
    val email: String = "",
    val password: String = ""
)

data class UserProfile(
    val learningGoal: List<LearningGoal> = emptyList(),
    val initialLevel: InitialLevel = InitialLevel.B1
)

data class UserSetting(
    val dailyNewWordGoal: Int = 10,
    val dailyReviewGoal: Int = 25
)