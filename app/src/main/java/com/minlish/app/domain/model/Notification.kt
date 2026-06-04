package com.minlish.app.domain.model

data class Notification(
    val id: String = "",
    val title: String = "",
    val content: String = "",

    @field:JvmField
    val isRead: Boolean = false
)
