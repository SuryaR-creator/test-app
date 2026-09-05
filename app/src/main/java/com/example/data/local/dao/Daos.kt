package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUser(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE username = :username OR email = :username OR staffId = :username LIMIT 1")
    suspend fun findByCredentials(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface StaffProfileDao {
    @Query("SELECT * FROM staff_profiles")
    fun getAllStaff(): Flow<List<StaffProfileEntity>>

    @Query("SELECT * FROM staff_profiles WHERE uid = :uid OR staffId = :uid LIMIT 1")
    fun getStaffById(uid: String): Flow<StaffProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(staffList: List<StaffProfileEntity>)

    @Update
    suspend fun updateStaff(staff: StaffProfileEntity)

    @Query("UPDATE staff_profiles SET isActive = :isActive WHERE staffId = :staffId OR uid = :staffId")
    suspend fun setStaffActiveStatus(staffId: String, isActive: Boolean)

    @Query("UPDATE staff_profiles SET bio = :bio, emergencyContact = :emergencyContact, bloodGroup = :bloodGroup, address = :address, phoneNumber = :phone WHERE uid = :uid")
    suspend fun updateSelfProfile(uid: String, bio: String, emergencyContact: String, bloodGroup: String, address: String, phone: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE assignedStaffId = :staffId OR assignedToMultipleNamesJson LIKE '%' || :staffId || '%'")
    fun getTasksForStaff(staffId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: String): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, progressPercentage = :progress, staffNotes = :notes WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: String, progress: Int, notes: String)

    @Query("UPDATE tasks SET adminFeedback = :feedback WHERE id = :taskId")
    suspend fun updateAdminFeedback(taskId: String, feedback: String)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE (userId = :staffId OR staffId = :staffId) AND date = :date LIMIT 1")
    fun getAttendanceForDate(staffId: String, date: String): Flow<AttendanceEntity?>

    @Query("SELECT * FROM attendance WHERE (userId = :staffId OR staffId = :staffId) ORDER BY date DESC")
    fun getAttendanceHistory(staffId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAllAttendanceForDate(date: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceEntity>)
}

@Dao
interface TargetDao {
    @Query("SELECT * FROM targets WHERE staffId = :staffId LIMIT 1")
    fun getTargetForStaff(staffId: String): Flow<TargetEntity?>

    @Query("SELECT * FROM targets")
    fun getAllTargets(): Flow<List<TargetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: TargetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(targets: List<TargetEntity>)

    @Query("UPDATE targets SET completedValue = completedValue + :addedUnits WHERE staffId = :staffId")
    suspend fun addCompletedUnits(staffId: String, addedUnits: Int)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY publishedAt DESC")
    fun getAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(announcements: List<AnnouncementEntity>)

    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesForUser(userId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)
}

@Dao
interface ContentDao {
    @Query("SELECT * FROM genzpluse_content WHERE creatorId = :creatorId OR creatorStaffId = :creatorId ORDER BY createdAt DESC")
    fun getContentByCreator(creatorId: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM genzpluse_content ORDER BY createdAt DESC")
    fun getAllContent(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM genzpluse_content WHERE category = :category ORDER BY createdAt DESC")
    fun getContentByCategory(category: String): Flow<List<ContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contents: List<ContentEntity>)

    @Query("UPDATE genzpluse_content SET status = :status, adminReviewNotes = :adminNotes WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, adminNotes: String)

    @Query("DELETE FROM genzpluse_content WHERE id = :id")
    suspend fun deleteContent(id: String)
}

@Dao
interface RequestDao {
    @Query("SELECT * FROM leave_requests WHERE staffId = :staffId ORDER BY submittedAt DESC")
    fun getLeaveRequestsForStaff(staffId: String): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests ORDER BY submittedAt DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLeaveRequests(requests: List<LeaveRequestEntity>)

    @Query("UPDATE leave_requests SET status = :status, adminResponse = :response WHERE id = :id")
    suspend fun updateLeaveStatus(id: String, status: String, response: String)

    @Query("SELECT * FROM problem_reports WHERE staffId = :staffId ORDER BY submittedAt DESC")
    fun getProblemReportsForStaff(staffId: String): Flow<List<ProblemReportEntity>>

    @Query("SELECT * FROM problem_reports ORDER BY submittedAt DESC")
    fun getAllProblemReports(): Flow<List<ProblemReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblemReport(report: ProblemReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProblemReports(reports: List<ProblemReportEntity>)

    @Query("UPDATE problem_reports SET status = :status, adminNotes = :adminNotes WHERE id = :id")
    suspend fun updateProblemStatus(id: String, status: String, adminNotes: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE targetUserId = :userId OR targetUserId = 'ALL' ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}
