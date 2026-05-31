package com.minlish.app.features.auth.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: String = "",
    val userProfile: UserProfile = UserProfile(),
    val userSetting: UserSetting = UserSetting()
)

data class UserProfile(
    val learningGoals: List<String> = emptyList(),  // Enum LearningGoal duoc luu la String
    val initialLevel: String = "B1"  // Enum InitialLevel duoc luu la String
)

data class UserSetting(
    val dailyNewWordGoal: Int = 10,
    val dailyReviewGoal: Int = 50
)
