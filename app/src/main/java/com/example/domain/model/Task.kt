package com.example.domain.model

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class TaskStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE,
    CANCELLED
}

data class TaskComment(
    val id: String,
    val authorName: String,
    val authorRole: UserRole,
    val text: String,
    val timestamp: String
)

data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val assignedStaffId: String,
    val assignedStaffName: String,
    val assignedToMultipleNames: List<String> = emptyList(),
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val deadline: String,
    val targetUnits: Int = 1,
    val completedUnits: Int = 0,
    val progressPercentage: Int = 0,
    val resourcesLink: String = "",
    val adminFeedback: String = "",
    val staffNotes: String = "",
    val createdAt: String = "",
    val comments: List<TaskComment> = emptyList()
)
