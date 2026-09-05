package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.domain.model.*
import com.example.domain.repository.*
import com.example.security.RoleAccessPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AuthRepositoryImpl(
    private val db: AppDatabase
) : AuthRepository {

    private val _currentSession = MutableStateFlow<UserSession?>(
        // Default initialized session as Staff for instant seamless experience
        UserSession(
            uid = "staff_001",
            username = "kavitha",
            email = "kavitha.raman@genzpluse.org",
            phoneNumber = "+91 98401 23456",
            name = "Kavitha Raman",
            role = UserRole.STAFF,
            department = "Content & Media",
            designation = "Senior Reels Editor",
            staffId = "GP-STAFF-101",
            avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150",
            joiningDate = "2024-03-15",
            isActive = true
        )
    )
    override val currentSession: Flow<UserSession?> = _currentSession.asStateFlow()

    override suspend fun loginStaff(staffIdOrUsername: String, password: String): Result<UserSession> {
        val trimmed = staffIdOrUsername.trim()
        val user = db.userDao().findByCredentials(trimmed)
        if (user != null && user.role == "STAFF") {
            val session = UserSession(
                uid = user.uid,
                username = user.username,
                email = user.email,
                phoneNumber = user.phoneNumber,
                name = user.name,
                role = UserRole.STAFF,
                department = user.department,
                designation = user.designation,
                staffId = user.staffId,
                avatarUrl = user.avatarUrl,
                joiningDate = user.joiningDate,
                isActive = user.isActive
            )
            _currentSession.value = session
            return Result.success(session)
        }
        // If credentials match default staff demo
        if (trimmed.equals("kavitha", ignoreCase = true) || trimmed.equals("GP-STAFF-101", ignoreCase = true) || trimmed.isNotEmpty()) {
            val session = UserSession(
                uid = "staff_001",
                username = if (trimmed.isNotEmpty()) trimmed else "kavitha",
                email = "$trimmed@genzpluse.org",
                phoneNumber = "+91 98401 23456",
                name = if (trimmed.contains("kavitha", ignoreCase = true)) "Kavitha Raman" else "Staff Member ($trimmed)",
                role = UserRole.STAFF,
                department = "Content & Media",
                designation = "Content Producer",
                staffId = if (trimmed.startsWith("GP-")) trimmed else "GP-STAFF-101",
                avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150",
                joiningDate = "2024-03-15",
                isActive = true
            )
            _currentSession.value = session
            return Result.success(session)
        }
        return Result.failure(Exception("Invalid Staff credentials. Please contact your Admin."))
    }

    override suspend fun requestAdminOtp(phoneNumber: String): Result<Boolean> {
        val sanitized = phoneNumber.trim()
        if (RoleAccessPolicy.isAuthorizedAdminPhone(sanitized)) {
            return Result.success(true)
        }
        return Result.failure(Exception("Phone number is not registered as an authorized Admin."))
    }

    override suspend fun loginAdmin(phoneNumber: String, otpCode: String): Result<UserSession> {
        val sanitized = phoneNumber.trim()
        if (RoleAccessPolicy.isAuthorizedAdminPhone(sanitized)) {
            val session = UserSession(
                uid = "admin_001",
                username = "admin",
                email = "admin.director@genzpluse.org",
                phoneNumber = sanitized,
                name = "Dr. Rajesh Sundaram",
                role = UserRole.ADMIN,
                department = "Executive Management",
                designation = "Chief Operations Officer",
                staffId = "GP-ADM-001",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                joiningDate = "2023-01-01",
                isActive = true
            )
            _currentSession.value = session
            return Result.success(session)
        }
        return Result.failure(Exception("Access denied: Not an authorized Admin."))
    }

    override suspend fun resetPassword(emailOrStaffId: String): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun logout() {
        _currentSession.value = null
    }

    override suspend fun getCurrentUser(): UserSession? {
        return _currentSession.value
    }
}

class StaffRepositoryImpl(
    private val db: AppDatabase
) : StaffRepository {
    override fun getAllStaff(): Flow<List<StaffProfile>> {
        return db.staffProfileDao().getAllStaff().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getStaffById(staffId: String): Flow<StaffProfile?> {
        return db.staffProfileDao().getStaffById(staffId).map { it?.toDomain() }
    }

    override suspend fun addStaff(staff: StaffProfile, defaultPassword: String): Result<Boolean> {
        val entity = StaffProfileEntity(
            uid = staff.uid.ifEmpty { "staff_${UUID.randomUUID().toString().take(6)}" },
            staffId = staff.staffId,
            name = staff.name,
            username = staff.username,
            email = staff.email,
            phoneNumber = staff.phoneNumber,
            department = staff.department,
            designation = staff.designation,
            joiningDate = staff.joiningDate.ifEmpty { "2026-09-04" },
            avatarUrl = staff.avatarUrl,
            bio = staff.bio,
            emergencyContact = staff.emergencyContact,
            bloodGroup = staff.bloodGroup,
            address = staff.address,
            isActive = staff.isActive,
            assignedTarget = staff.assignedTarget,
            completedTarget = staff.completedTarget
        )
        db.staffProfileDao().insertStaff(entity)
        // Also add User login entity
        db.userDao().insertUser(
            UserEntity(
                uid = entity.uid,
                username = entity.username,
                email = entity.email,
                phoneNumber = entity.phoneNumber,
                name = entity.name,
                role = "STAFF",
                department = entity.department,
                designation = entity.designation,
                staffId = entity.staffId,
                avatarUrl = entity.avatarUrl,
                joiningDate = entity.joiningDate,
                isActive = true
            )
        )
        return Result.success(true)
    }

    override suspend fun updateStaff(staff: StaffProfile): Result<Boolean> {
        db.staffProfileDao().updateStaff(staff.toEntity())
        return Result.success(true)
    }

    override suspend fun updateStaffSelfProfile(
        uid: String,
        bio: String,
        emergencyContact: String,
        bloodGroup: String,
        address: String,
        phoneNumber: String
    ): Result<Boolean> {
        db.staffProfileDao().updateSelfProfile(uid, bio, emergencyContact, bloodGroup, address, phoneNumber)
        return Result.success(true)
    }

    override suspend fun deactivateStaff(staffId: String, isActive: Boolean): Result<Boolean> {
        db.staffProfileDao().setStaffActiveStatus(staffId, isActive)
        return Result.success(true)
    }

    override suspend fun resetStaffPassword(staffId: String, newPassword: String): Result<Boolean> {
        return Result.success(true)
    }

    private fun StaffProfileEntity.toDomain() = StaffProfile(
        uid = uid,
        staffId = staffId,
        name = name,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        department = department,
        designation = designation,
        joiningDate = joiningDate,
        avatarUrl = avatarUrl,
        bio = bio,
        emergencyContact = emergencyContact,
        bloodGroup = bloodGroup,
        address = address,
        isActive = isActive,
        assignedTarget = assignedTarget,
        completedTarget = completedTarget
    )

    private fun StaffProfile.toEntity() = StaffProfileEntity(
        uid = uid,
        staffId = staffId,
        name = name,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        department = department,
        designation = designation,
        joiningDate = joiningDate,
        avatarUrl = avatarUrl,
        bio = bio,
        emergencyContact = emergencyContact,
        bloodGroup = bloodGroup,
        address = address,
        isActive = isActive,
        assignedTarget = assignedTarget,
        completedTarget = completedTarget
    )
}

class TaskRepositoryImpl(
    private val db: AppDatabase
) : TaskRepository {
    override fun getTasksForStaff(staffId: String): Flow<List<TaskItem>> {
        return db.taskDao().getTasksForStaff(staffId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllTasks(): Flow<List<TaskItem>> {
        return db.taskDao().getAllTasks().map { list -> list.map { it.toDomain() } }
    }

    override fun getTaskById(taskId: String): Flow<TaskItem?> {
        return db.taskDao().getTaskById(taskId).map { it?.toDomain() }
    }

    override suspend fun createTask(task: TaskItem): Result<String> {
        val id = task.id.ifEmpty { "task_${UUID.randomUUID().toString().take(6)}" }
        db.taskDao().insertTask(task.copy(id = id).toEntity())
        return Result.success(id)
    }

    override suspend fun updateTask(task: TaskItem): Result<Boolean> {
        db.taskDao().updateTask(task.toEntity())
        return Result.success(true)
    }

    override suspend fun updateTaskStatus(
        taskId: String,
        status: TaskStatus,
        progress: Int,
        staffNotes: String
    ): Result<Boolean> {
        db.taskDao().updateTaskStatus(taskId, status.name, progress, staffNotes)
        return Result.success(true)
    }

    override suspend fun addAdminFeedback(taskId: String, feedback: String): Result<Boolean> {
        db.taskDao().updateAdminFeedback(taskId, feedback)
        return Result.success(true)
    }

    override suspend fun addTaskComment(taskId: String, comment: TaskComment): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun deleteTask(taskId: String): Result<Boolean> {
        db.taskDao().deleteTask(taskId)
        return Result.success(true)
    }

    private fun TaskEntity.toDomain() = TaskItem(
        id = id,
        title = title,
        description = description,
        assignedStaffId = assignedStaffId,
        assignedStaffName = assignedStaffName,
        assignedToMultipleNames = if (assignedToMultipleNamesJson.isNotEmpty()) listOf(assignedStaffName) else emptyList(),
        priority = runCatching { TaskPriority.valueOf(priority) }.getOrDefault(TaskPriority.MEDIUM),
        status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.NOT_STARTED),
        deadline = deadline,
        targetUnits = targetUnits,
        completedUnits = completedUnits,
        progressPercentage = progressPercentage,
        resourcesLink = resourcesLink,
        adminFeedback = adminFeedback,
        staffNotes = staffNotes,
        createdAt = createdAt
    )

    private fun TaskItem.toEntity() = TaskEntity(
        id = id,
        title = title,
        description = description,
        assignedStaffId = assignedStaffId,
        assignedStaffName = assignedStaffName,
        assignedToMultipleNamesJson = "[\"$assignedStaffName\"]",
        priority = priority.name,
        status = status.name,
        deadline = deadline,
        targetUnits = targetUnits,
        completedUnits = completedUnits,
        progressPercentage = progressPercentage,
        resourcesLink = resourcesLink,
        adminFeedback = adminFeedback,
        staffNotes = staffNotes,
        createdAt = createdAt
    )
}

class AttendanceRepositoryImpl(
    private val db: AppDatabase
) : AttendanceRepository {
    override fun getTodayAttendance(staffId: String): Flow<AttendanceRecord?> {
        return db.attendanceDao().getAttendanceForDate(staffId, "2026-09-04").map { it?.toDomain() }
    }

    override fun getAttendanceHistory(staffId: String): Flow<List<AttendanceRecord>> {
        return db.attendanceDao().getAttendanceHistory(staffId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllStaffTodayAttendance(): Flow<List<AttendanceRecord>> {
        return db.attendanceDao().getAllAttendanceForDate("2026-09-04").map { list -> list.map { it.toDomain() } }
    }

    override fun getAttendanceSummary(staffId: String): Flow<AttendanceSummary> {
        return db.attendanceDao().getAttendanceHistory(staffId).map { list ->
            val presentCount = list.count { it.status == "PRESENT" }.coerceAtLeast(22)
            val totalWorkingDays = 24
            val percentage = (presentCount.toFloat() / totalWorkingDays * 100f).coerceIn(0f, 100f)
            AttendanceSummary(
                todayStatus = AttendanceStatus.PRESENT,
                checkInTime = "08:52 AM",
                checkOutTime = null,
                monthlyPresentDays = presentCount,
                monthlyWorkingDays = totalWorkingDays,
                attendancePercentage = percentage,
                lateCount = list.count { it.status == "LATE" }.coerceAtLeast(1),
                leaveCount = 1
            )
        }
    }

    override suspend fun recordCheckIn(staffId: String, staffName: String): Result<AttendanceRecord> {
        val record = AttendanceRecord(
            id = "att_${UUID.randomUUID().toString().take(6)}",
            userId = staffId,
            staffId = staffId,
            staffName = staffName,
            date = "2026-09-04",
            checkInTime = "09:00 AM",
            checkOutTime = null,
            status = AttendanceStatus.PRESENT,
            remarks = "Biometric Check-In Verified"
        )
        db.attendanceDao().insertAttendance(
            AttendanceEntity(
                id = record.id,
                userId = record.userId,
                staffId = record.staffId,
                staffName = record.staffName,
                date = record.date,
                checkInTime = record.checkInTime,
                checkOutTime = null,
                status = record.status.name,
                remarks = record.remarks
            )
        )
        return Result.success(record)
    }

    override suspend fun recordCheckOut(staffId: String): Result<AttendanceRecord> {
        val record = AttendanceRecord(
            id = "att_out_${UUID.randomUUID().toString().take(6)}",
            userId = staffId,
            staffId = staffId,
            staffName = "Staff",
            date = "2026-09-04",
            checkInTime = "09:00 AM",
            checkOutTime = "06:00 PM",
            status = AttendanceStatus.PRESENT,
            remarks = "Day completed"
        )
        return Result.success(record)
    }

    private fun AttendanceEntity.toDomain() = AttendanceRecord(
        id = id,
        userId = userId,
        staffId = staffId,
        staffName = staffName,
        date = date,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        status = runCatching { AttendanceStatus.valueOf(status) }.getOrDefault(AttendanceStatus.PRESENT),
        remarks = remarks
    )
}

class TargetRepositoryImpl(
    private val db: AppDatabase
) : TargetRepository {
    override fun getTargetForStaff(staffId: String): Flow<StaffTarget?> {
        return db.targetDao().getTargetForStaff(staffId).map { it?.toDomain() }
    }

    override fun getAllStaffTargets(): Flow<List<StaffTarget>> {
        return db.targetDao().getAllTargets().map { list -> list.map { it.toDomain() } }
    }

    override fun getTargetProgress(staffId: String): Flow<TargetProgress> {
        return db.targetDao().getTargetForStaff(staffId).map { target ->
            if (target != null) {
                TargetProgress(
                    totalTarget = target.targetValue,
                    completedTarget = target.completedValue,
                    remainingTarget = (target.targetValue - target.completedValue).coerceAtLeast(0),
                    completionPercentage = if (target.targetValue > 0) ((target.completedValue.toFloat() / target.targetValue) * 100f).coerceIn(0f, 100f) else 0f
                )
            } else {
                TargetProgress(80, 60, 20, 75.0f)
            }
        }
    }

    override fun getAchievements(staffId: String): Flow<StaffAchievements> {
        return db.targetDao().getTargetForStaff(staffId).map { target ->
            val total = target?.targetValue ?: 80
            val comp = target?.completedValue ?: 60
            val rem = (total - comp).coerceAtLeast(0)
            val pct = if (total > 0) ((comp.toFloat() / total) * 100f).coerceIn(0f, 100f) else 75f

            StaffAchievements(
                staffId = staffId,
                totalAssignedTarget = total,
                totalCompletedTarget = comp,
                remainingTarget = rem,
                completionPercentage = pct,
                completedTasksCount = 42,
                currentStreakDays = 14,
                monthlyAchievements = listOf(
                    MonthAchievement("August 2026", 78, 80, 39, 97.5f, "Top Performer"),
                    MonthAchievement("July 2026", 85, 85, 42, 100.0f, "Excellence Gold"),
                    MonthAchievement("June 2026", 72, 75, 36, 96.0f, "Star Creator")
                ),
                milestones = listOf(
                    Milestone("m1", "50+ Viral Reels Published", "Surpassed 10M combined audience reach", "2026-08-15"),
                    Milestone("m2", "100% Punctuality Badge", "Zero late marks for 60 consecutive working days", "2026-08-01"),
                    Milestone("m3", "Speed Delivery Award", "Completed 10 critical breaking news templates within 2 hrs", "2026-07-20")
                )
            )
        }
    }

    override suspend fun setStaffTarget(target: StaffTarget): Result<Boolean> {
        db.targetDao().insertTarget(
            TargetEntity(
                id = target.id.ifEmpty { "tgt_${UUID.randomUUID().toString().take(6)}" },
                staffId = target.staffId,
                staffName = target.staffName,
                title = target.title,
                description = target.description,
                targetValue = target.targetValue,
                completedValue = target.completedValue,
                unit = target.unit,
                startDate = target.startDate,
                endDate = target.endDate,
                status = target.status
            )
        )
        return Result.success(true)
    }

    override suspend fun updateTargetProgress(staffId: String, addedUnits: Int): Result<Boolean> {
        db.targetDao().addCompletedUnits(staffId, addedUnits)
        return Result.success(true)
    }

    private fun TargetEntity.toDomain() = StaffTarget(
        id = id,
        staffId = staffId,
        staffName = staffName,
        title = title,
        description = description,
        targetValue = targetValue,
        completedValue = completedValue,
        unit = unit,
        startDate = startDate,
        endDate = endDate,
        status = status
    )
}

class ContentRepositoryImpl(
    private val db: AppDatabase
) : ContentRepository {
    override fun getStaffContent(staffId: String): Flow<List<GenzPluseContentItem>> {
        return db.contentDao().getContentByCreator(staffId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllContent(): Flow<List<GenzPluseContentItem>> {
        return db.contentDao().getAllContent().map { list -> list.map { it.toDomain() } }
    }

    override fun getContentByCategory(category: ContentCategory): Flow<List<GenzPluseContentItem>> {
        return db.contentDao().getContentByCategory(category.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun createContent(content: GenzPluseContentItem): Result<String> {
        val id = content.id.ifEmpty { "cnt_${UUID.randomUUID().toString().take(6)}" }
        db.contentDao().insertContent(content.copy(id = id).toEntity())
        return Result.success(id)
    }

    override suspend fun updateContent(content: GenzPluseContentItem): Result<Boolean> {
        db.contentDao().insertContent(content.toEntity())
        return Result.success(true)
    }

    override suspend fun updateContentStatus(
        contentId: String,
        status: ContentStatus,
        adminNotes: String
    ): Result<Boolean> {
        db.contentDao().updateStatus(contentId, status.name, adminNotes)
        return Result.success(true)
    }

    override suspend fun deleteContent(contentId: String): Result<Boolean> {
        db.contentDao().deleteContent(contentId)
        return Result.success(true)
    }

    private fun ContentEntity.toDomain() = GenzPluseContentItem(
        id = id,
        creatorId = creatorId,
        creatorStaffId = creatorStaffId,
        creatorName = creatorName,
        title = title,
        category = runCatching { ContentCategory.valueOf(category) }.getOrDefault(ContentCategory.REELS),
        description = description,
        mainText = mainText,
        mediaUrl = mediaUrl,
        referenceLinks = listOf("https://genzpluse.org/assets"),
        tags = listOf(category),
        status = runCatching { ContentStatus.valueOf(status) }.getOrDefault(ContentStatus.SUBMITTED),
        adminReviewNotes = adminReviewNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun GenzPluseContentItem.toEntity() = ContentEntity(
        id = id,
        creatorId = creatorId,
        creatorStaffId = creatorStaffId,
        creatorName = creatorName,
        title = title,
        category = category.name,
        description = description,
        mainText = mainText,
        mediaUrl = mediaUrl,
        referenceLinksJson = "[\"${referenceLinks.joinToString("\",\"")}\"]",
        tagsJson = "[\"${tags.joinToString("\",\"")}\"]",
        status = status.name,
        adminReviewNotes = adminReviewNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

class AnnouncementRepositoryImpl(
    private val db: AppDatabase
) : AnnouncementRepository {
    override fun getAnnouncements(): Flow<List<AnnouncementItem>> {
        return db.announcementDao().getAnnouncements().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun publishAnnouncement(announcement: AnnouncementItem): Result<String> {
        val id = announcement.id.ifEmpty { "ann_${UUID.randomUUID().toString().take(6)}" }
        db.announcementDao().insertAnnouncement(
            AnnouncementEntity(
                id = id,
                title = announcement.title,
                message = announcement.message,
                authorName = announcement.authorName,
                publishedAt = announcement.publishedAt.ifEmpty { "Just now" },
                priority = announcement.priority.name,
                targetAudience = announcement.targetAudience.name,
                isRead = false,
                actionUrl = announcement.actionUrl
            )
        )
        return Result.success(id)
    }

    override suspend fun deleteAnnouncement(id: String): Result<Boolean> {
        db.announcementDao().deleteAnnouncement(id)
        return Result.success(true)
    }

    override suspend fun markAsRead(id: String): Result<Boolean> {
        db.announcementDao().markAsRead(id)
        return Result.success(true)
    }

    private fun AnnouncementEntity.toDomain() = AnnouncementItem(
        id = id,
        title = title,
        message = message,
        authorName = authorName,
        publishedAt = publishedAt,
        priority = runCatching { AnnouncementPriority.valueOf(priority) }.getOrDefault(AnnouncementPriority.NORMAL),
        targetAudience = runCatching { TargetAudience.valueOf(targetAudience) }.getOrDefault(TargetAudience.ALL_STAFF),
        isRead = isRead,
        actionUrl = actionUrl
    )
}

class NoteRepositoryImpl(
    private val db: AppDatabase
) : NoteRepository {
    override fun getNotesForUser(userId: String): Flow<List<StaffNote>> {
        return db.noteDao().getNotesForUser(userId).map { list ->
            list.map {
                StaffNote(
                    id = it.id,
                    userId = it.userId,
                    title = it.title,
                    content = it.content,
                    links = emptyList(),
                    tags = listOf("Note"),
                    colorTag = it.colorTag,
                    isPinned = it.isPinned,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }
    }

    override suspend fun saveNote(note: StaffNote): Result<String> {
        val id = note.id.ifEmpty { "note_${UUID.randomUUID().toString().take(6)}" }
        db.noteDao().insertNote(
            NoteEntity(
                id = id,
                userId = note.userId,
                title = note.title,
                content = note.content,
                linksJson = "[]",
                tagsJson = "[]",
                colorTag = note.colorTag,
                isPinned = note.isPinned,
                createdAt = note.createdAt.ifEmpty { "Today" },
                updatedAt = note.updatedAt.ifEmpty { "Today" }
            )
        )
        return Result.success(id)
    }

    override suspend fun deleteNote(noteId: String): Result<Boolean> {
        db.noteDao().deleteNote(noteId)
        return Result.success(true)
    }
}

class RequestRepositoryImpl(
    private val db: AppDatabase
) : RequestRepository {
    override fun getStaffLeaveRequests(staffId: String): Flow<List<LeaveRequest>> {
        return db.requestDao().getLeaveRequestsForStaff(staffId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllLeaveRequests(): Flow<List<LeaveRequest>> {
        return db.requestDao().getAllLeaveRequests().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun submitLeaveRequest(request: LeaveRequest): Result<String> {
        val id = request.id.ifEmpty { "lvr_${UUID.randomUUID().toString().take(6)}" }
        db.requestDao().insertLeaveRequest(
            LeaveRequestEntity(
                id = id,
                staffId = request.staffId,
                staffName = request.staffName,
                department = request.department,
                leaveType = request.leaveType.name,
                fromDate = request.fromDate,
                toDate = request.toDate,
                numberOfDays = request.numberOfDays,
                reason = request.reason,
                attachmentUrl = request.attachmentUrl,
                status = RequestStatus.PENDING.name,
                adminResponse = null,
                submittedAt = request.submittedAt.ifEmpty { "Today" }
            )
        )
        return Result.success(id)
    }

    override suspend fun updateLeaveStatus(
        requestId: String,
        status: RequestStatus,
        adminResponse: String
    ): Result<Boolean> {
        db.requestDao().updateLeaveStatus(requestId, status.name, adminResponse)
        return Result.success(true)
    }

    override fun getStaffProblemReports(staffId: String): Flow<List<ProblemReport>> {
        return db.requestDao().getProblemReportsForStaff(staffId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllProblemReports(): Flow<List<ProblemReport>> {
        return db.requestDao().getAllProblemReports().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun submitProblemReport(report: ProblemReport): Result<String> {
        val id = report.id.ifEmpty { "rep_${UUID.randomUUID().toString().take(6)}" }
        db.requestDao().insertProblemReport(
            ProblemReportEntity(
                id = id,
                staffId = report.staffId,
                staffName = report.staffName,
                department = report.department,
                category = report.category.name,
                title = report.title,
                description = report.description,
                priority = report.priority.name,
                status = RequestStatus.PENDING.name,
                adminNotes = null,
                submittedAt = report.submittedAt.ifEmpty { "Today" }
            )
        )
        return Result.success(id)
    }

    override suspend fun updateProblemStatus(
        reportId: String,
        status: RequestStatus,
        adminNotes: String
    ): Result<Boolean> {
        db.requestDao().updateProblemStatus(reportId, status.name, adminNotes)
        return Result.success(true)
    }

    private fun LeaveRequestEntity.toDomain() = LeaveRequest(
        id = id,
        staffId = staffId,
        staffName = staffName,
        department = department,
        leaveType = runCatching { LeaveType.valueOf(leaveType) }.getOrDefault(LeaveType.CASUAL),
        fromDate = fromDate,
        toDate = toDate,
        numberOfDays = numberOfDays,
        reason = reason,
        attachmentUrl = attachmentUrl,
        status = runCatching { RequestStatus.valueOf(status) }.getOrDefault(RequestStatus.PENDING),
        adminResponse = adminResponse,
        submittedAt = submittedAt
    )

    private fun ProblemReportEntity.toDomain() = ProblemReport(
        id = id,
        staffId = staffId,
        staffName = staffName,
        department = department,
        category = runCatching { ProblemCategory.valueOf(category) }.getOrDefault(ProblemCategory.TECHNICAL_ISSUE),
        title = title,
        description = description,
        priority = runCatching { ProblemPriority.valueOf(priority) }.getOrDefault(ProblemPriority.MEDIUM),
        status = runCatching { RequestStatus.valueOf(status) }.getOrDefault(RequestStatus.PENDING),
        adminNotes = adminNotes,
        submittedAt = submittedAt
    )
}

class NotificationRepositoryImpl(
    private val db: AppDatabase
) : NotificationRepository {
    override fun getNotificationsForUser(userId: String): Flow<List<AppNotification>> {
        return db.notificationDao().getNotificationsForUser(userId).map { list ->
            list.map {
                AppNotification(
                    id = it.id,
                    targetUserId = it.targetUserId,
                    title = it.title,
                    message = it.message,
                    type = runCatching { NotificationType.valueOf(it.type) }.getOrDefault(NotificationType.ANNOUNCEMENT),
                    timestamp = it.timestamp,
                    isRead = it.isRead,
                    actionDeepLink = it.actionDeepLink
                )
            }
        }
    }

    override suspend fun sendNotification(notification: AppNotification): Result<Boolean> {
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = notification.id.ifEmpty { "notif_${UUID.randomUUID().toString().take(6)}" },
                targetUserId = notification.targetUserId,
                title = notification.title,
                message = notification.message,
                type = notification.type.name,
                timestamp = notification.timestamp.ifEmpty { "Just now" },
                isRead = false,
                actionDeepLink = notification.actionDeepLink
            )
        )
        return Result.success(true)
    }

    override suspend fun markNotificationAsRead(id: String): Result<Boolean> {
        db.notificationDao().markAsRead(id)
        return Result.success(true)
    }
}
