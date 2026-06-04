package com.minlish.app.domain.model

data class Notification(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,

    @field:JvmField
    val isRead: Boolean = false
)
