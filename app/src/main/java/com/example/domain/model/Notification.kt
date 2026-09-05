package com.example.domain.model

enum class NotificationType {
    TASK_ASSIGNED,
    TASK_FEEDBACK,
    ANNOUNCEMENT,
    LEAVE_STATUS,
    PROBLEM_STATUS,
    TARGET_UPDATE,
    ACHIEVEMENT_UNLOCKED,
    CONTENT_APPROVED
}

data class AppNotification(
    val id: String,
    val targetUserId: String, // Or "ALL" for broadcast
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val isRead: Boolean = false,
    val actionDeepLink: String = ""
)
