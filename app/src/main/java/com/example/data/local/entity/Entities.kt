package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val name: String,
    val role: String,
    val department: String,
    val designation: String,
    val staffId: String,
    val avatarUrl: String,
    val joiningDate: String,
    val isActive: Boolean
)

@Entity(tableName = "staff_profiles")
data class StaffProfileEntity(
    @PrimaryKey val uid: String,
    val staffId: String,
    val name: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val department: String,
    val designation: String,
    val joiningDate: String,
    val avatarUrl: String,
    val bio: String,
    val emergencyContact: String,
    val bloodGroup: String,
    val address: String,
    val isActive: Boolean,
    val assignedTarget: Int,
    val completedTarget: Int
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val assignedStaffId: String,
    val assignedStaffName: String,
    val assignedToMultipleNamesJson: String,
    val priority: String,
    val status: String,
    val deadline: String,
    val targetUnits: Int,
    val completedUnits: Int,
    val progressPercentage: Int,
    val resourcesLink: String,
    val adminFeedback: String,
    val staffNotes: String,
    val createdAt: String
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val staffId: String,
    val staffName: String,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val status: String,
    val remarks: String
)

@Entity(tableName = "targets")
data class TargetEntity(
    @PrimaryKey val id: String,
    val staffId: String,
    val staffName: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val completedValue: Int,
    val unit: String,
    val startDate: String,
    val endDate: String,
    val status: String
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val authorName: String,
    val publishedAt: String,
    val priority: String,
    val targetAudience: String,
    val isRead: Boolean,
    val actionUrl: String
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val linksJson: String,
    val tagsJson: String,
    val colorTag: Long,
    val isPinned: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "genzpluse_content")
data class ContentEntity(
    @PrimaryKey val id: String,
    val creatorId: String,
    val creatorStaffId: String,
    val creatorName: String,
    val title: String,
    val category: String,
    val description: String,
    val mainText: String,
    val mediaUrl: String,
    val referenceLinksJson: String,
    val tagsJson: String,
    val status: String,
    val adminReviewNotes: String,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey val id: String,
    val staffId: String,
    val staffName: String,
    val department: String,
    val leaveType: String,
    val fromDate: String,
    val toDate: String,
    val numberOfDays: Int,
    val reason: String,
    val attachmentUrl: String?,
    val status: String,
    val adminResponse: String?,
    val submittedAt: String
)

@Entity(tableName = "problem_reports")
data class ProblemReportEntity(
    @PrimaryKey val id: String,
    val staffId: String,
    val staffName: String,
    val department: String,
    val category: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val adminNotes: String?,
    val submittedAt: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val targetUserId: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: String,
    val isRead: Boolean,
    val actionDeepLink: String
)
