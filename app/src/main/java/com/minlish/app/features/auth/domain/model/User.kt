package com.minlish.app.features.auth.domain.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val accessToken: String? = null,
    val refreshToken: String? = null
)
