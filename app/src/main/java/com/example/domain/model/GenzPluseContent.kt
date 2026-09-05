package com.example.domain.model

enum class ContentCategory {
    REELS,
    NEWS_TEMPLATE,
    LANDSCAPE
}

enum class ContentStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED,
    PUBLISHED
}

data class GenzPluseContentItem(
    val id: String,
    val creatorId: String,
    val creatorStaffId: String,
    val creatorName: String,
    val title: String,
    val category: ContentCategory,
    val description: String,
    val mainText: String = "",
    val mediaUrl: String = "",
    val referenceLinks: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val status: ContentStatus = ContentStatus.SUBMITTED,
    val adminReviewNotes: String = "",
    val createdAt: String,
    val updatedAt: String
)
