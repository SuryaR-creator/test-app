package com.example.domain.model

data class StaffNote(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val links: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val colorTag: Long = 0xFF3B82F6,
    val isPinned: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)
