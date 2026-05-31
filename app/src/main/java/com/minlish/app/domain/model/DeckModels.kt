package com.minlish.app.domain.model

data class Deck(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val learned: Int = 0,
    val streak: Int = 0,
    val totalWords: Int = 0
)
