package com.example.domain.model

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    UNDER_REVIEW,
    RESOLVED,
    CLOSED
}

enum class LeaveType {
    CASUAL,
    SICK,
    EMERGENCY,
    ANNUAL,
    UNPAID
}

data class LeaveRequest(
    val id: String,
    val staffId: String,
    val staffName: String,
    val department: String,
    val leaveType: LeaveType,
    val fromDate: String,
    val toDate: String,
    val numberOfDays: Int,
    val reason: String,
    val attachmentUrl: String? = null,
    val status: RequestStatus = RequestStatus.PENDING,
    val adminResponse: String? = null,
    val submittedAt: String
)

enum class ProblemCategory {
    TECHNICAL_ISSUE,
    WORK_RELATED,
    SYSTEM_ACCESS,
    HARDWARE_EQUIPMENT,
    PAYROLL_HR,
    OTHER_CONCERN
}

enum class ProblemPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class ProblemReport(
    val id: String,
    val staffId: String,
    val staffName: String,
    val department: String,
    val category: ProblemCategory,
    val title: String,
    val description: String,
    val priority: ProblemPriority = ProblemPriority.MEDIUM,
    val status: RequestStatus = RequestStatus.PENDING,
    val adminNotes: String? = null,
    val submittedAt: String
)
