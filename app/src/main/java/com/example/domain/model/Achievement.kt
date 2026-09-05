package com.example.domain.model

data class Milestone(
    val id: String,
    val title: String,
    val description: String,
    val dateAchieved: String,
    val iconName: String = "trophy",
    val badgeColor: Long = 0xFF2563EB
)

data class MonthAchievement(
    val monthYear: String, // e.g. "September 2026"
    val targetCompleted: Int,
    val targetAssigned: Int,
    val tasksFinished: Int,
    val onTimeRate: Float,
    val rating: String = "Top Performer"
)

data class StaffAchievements(
    val staffId: String,
    val totalAssignedTarget: Int = 200,
    val totalCompletedTarget: Int = 150,
    val remainingTarget: Int = 50,
    val completionPercentage: Float = 75.0f,
    val completedTasksCount: Int = 48,
    val currentStreakDays: Int = 14,
    val monthlyAchievements: List<MonthAchievement> = emptyList(),
    val milestones: List<Milestone> = emptyList(),
    val topSkillBadges: List<String> = listOf("GenzPluse Creator", "Punctual", "Target Crusher", "Fast Delivery")
)
