package com.example.domain.model

enum class AnnouncementPriority {
    NORMAL,
    IMPORTANT,
    URGENT
}

enum class TargetAudience {
    ALL_STAFF,
    CONTENT_CREATORS,
    OPERATIONS,
    ADMINS_ONLY
}

data class AnnouncementItem(
    val id: String,
    val title: String,
    val message: String,
    val authorName: String = "Admin Department",
    val publishedAt: String,
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val targetAudience: TargetAudience = TargetAudience.ALL_STAFF,
    val isRead: Boolean = false,
    val actionUrl: String = ""
)
