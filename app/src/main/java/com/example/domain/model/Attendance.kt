package com.example.domain.model

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    HALF_DAY,
    ON_LEAVE
}

data class AttendanceRecord(
    val id: String,
    val userId: String,
    val staffId: String,
    val staffName: String,
    val date: String, // YYYY-MM-DD
    val checkInTime: String,
    val checkOutTime: String? = null,
    val status: AttendanceStatus,
    val remarks: String = ""
)

data class AttendanceSummary(
    val todayStatus: AttendanceStatus = AttendanceStatus.PRESENT,
    val checkInTime: String = "09:00 AM",
    val checkOutTime: String? = null,
    val monthlyPresentDays: Int = 22,
    val monthlyWorkingDays: Int = 24,
    val attendancePercentage: Float = 91.6f,
    val lateCount: Int = 1,
    val leaveCount: Int = 1
)
