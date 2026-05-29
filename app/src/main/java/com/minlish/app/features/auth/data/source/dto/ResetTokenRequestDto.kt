package com.minlish.app.features.auth.data.source.dto

data class ResetTokenRequestDto(
    val token: String,
    val expiresAt: Long,
    val email: String
)
