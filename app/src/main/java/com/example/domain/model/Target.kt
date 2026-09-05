package com.example.domain.model

data class StaffTarget(
    val id: String,
    val staffId: String,
    val staffName: String,
    val title: String,
    val description: String = "",
    val targetValue: Int,
    val completedValue: Int,
    val unit: String = "Tasks/Units",
    val startDate: String,
    val endDate: String,
    val status: String = "ACTIVE"
) {
    val remainingValue: Int get() = (targetValue - completedValue).coerceAtLeast(0)
    val completionPercentage: Float get() = if (targetValue > 0) ((completedValue.toFloat() / targetValue) * 100f).coerceIn(0f, 100f) else 0f
}

data class TargetProgress(
    val totalTarget: Int = 100,
    val completedTarget: Int = 75,
    val remainingTarget: Int = 25,
    val completionPercentage: Float = 75.0f
)
