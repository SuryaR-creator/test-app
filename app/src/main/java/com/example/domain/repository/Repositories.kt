package com.example.domain.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentSession: Flow<UserSession?>
    suspend fun loginStaff(staffIdOrUsername: String, password: String):Result<UserSession>
    suspend fun loginAdmin(phoneNumber: String, otpCode: String): Result<UserSession>
    suspend fun requestAdminOtp(phoneNumber: String): Result<Boolean>
    suspend fun resetPassword(emailOrStaffId: String): Result<Boolean>
    suspend fun logout()
    suspend fun getCurrentUser(): UserSession?
}

interface StaffRepository {
    fun getAllStaff(): Flow<List<StaffProfile>>
    fun getStaffById(staffId: String): Flow<StaffProfile?>
    suspend fun addStaff(staff: StaffProfile, defaultPassword: String): Result<Boolean>
    suspend fun updateStaff(staff: StaffProfile): Result<Boolean>
    suspend fun updateStaffSelfProfile(uid: String, bio: String, emergencyContact: String, bloodGroup: String, address: String, phoneNumber: String): Result<Boolean>
    suspend fun deactivateStaff(staffId: String, isActive: Boolean): Result<Boolean>
    suspend fun resetStaffPassword(staffId: String, newPassword: String): Result<Boolean>
}

interface TaskRepository {
    fun getTasksForStaff(staffId: String): Flow<List<TaskItem>>
    fun getAllTasks(): Flow<List<TaskItem>>
    fun getTaskById(taskId: String): Flow<TaskItem?>
    suspend fun createTask(task: TaskItem): Result<String>
    suspend fun updateTask(task: TaskItem): Result<Boolean>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus, progress: Int, staffNotes: String): Result<Boolean>
    suspend fun addAdminFeedback(taskId: String, feedback: String): Result<Boolean>
    suspend fun addTaskComment(taskId: String, comment: TaskComment): Result<Boolean>
    suspend fun deleteTask(taskId: String): Result<Boolean>
}

interface AttendanceRepository {
    fun getTodayAttendance(staffId: String): Flow<AttendanceRecord?>
    fun getAttendanceHistory(staffId: String): Flow<List<AttendanceRecord>>
    fun getAllStaffTodayAttendance(): Flow<List<AttendanceRecord>>
    fun getAttendanceSummary(staffId: String): Flow<AttendanceSummary>
    suspend fun recordCheckIn(staffId: String, staffName: String): Result<AttendanceRecord>
    suspend fun recordCheckOut(staffId: String): Result<AttendanceRecord>
}

interface TargetRepository {
    fun getTargetForStaff(staffId: String): Flow<StaffTarget?>
    fun getAllStaffTargets(): Flow<List<StaffTarget>>
    fun getTargetProgress(staffId: String): Flow<TargetProgress>
    fun getAchievements(staffId: String): Flow<StaffAchievements>
    suspend fun setStaffTarget(target: StaffTarget): Result<Boolean>
    suspend fun updateTargetProgress(staffId: String, addedUnits: Int): Result<Boolean>
}

interface ContentRepository {
    fun getStaffContent(staffId: String): Flow<List<GenzPluseContentItem>>
    fun getAllContent(): Flow<List<GenzPluseContentItem>>
    fun getContentByCategory(category: ContentCategory): Flow<List<GenzPluseContentItem>>
    suspend fun createContent(content: GenzPluseContentItem): Result<String>
    suspend fun updateContent(content: GenzPluseContentItem): Result<Boolean>
    suspend fun updateContentStatus(contentId: String, status: ContentStatus, adminNotes: String): Result<Boolean>
    suspend fun deleteContent(contentId: String): Result<Boolean>
}

interface AnnouncementRepository {
    fun getAnnouncements(): Flow<List<AnnouncementItem>>
    suspend fun publishAnnouncement(announcement: AnnouncementItem): Result<String>
    suspend fun deleteAnnouncement(id: String): Result<Boolean>
    suspend fun markAsRead(id: String): Result<Boolean>
}

interface NoteRepository {
    fun getNotesForUser(userId: String): Flow<List<StaffNote>>
    suspend fun saveNote(note: StaffNote): Result<String>
    suspend fun deleteNote(noteId: String): Result<Boolean>
}

interface RequestRepository {
    fun getStaffLeaveRequests(staffId: String): Flow<List<LeaveRequest>>
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>
    suspend fun submitLeaveRequest(request: LeaveRequest): Result<String>
    suspend fun updateLeaveStatus(requestId: String, status: RequestStatus, adminResponse: String): Result<Boolean>

    fun getStaffProblemReports(staffId: String): Flow<List<ProblemReport>>
    fun getAllProblemReports(): Flow<List<ProblemReport>>
    suspend fun submitProblemReport(report: ProblemReport): Result<String>
    suspend fun updateProblemStatus(reportId: String, status: RequestStatus, adminNotes: String): Result<Boolean>
}

interface NotificationRepository {
    fun getNotificationsForUser(userId: String): Flow<List<AppNotification>>
    suspend fun sendNotification(notification: AppNotification): Result<Boolean>
    suspend fun markNotificationAsRead(id: String): Result<Boolean>
}
