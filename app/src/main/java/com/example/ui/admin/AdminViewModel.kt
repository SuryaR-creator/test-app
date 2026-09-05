package com.example.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUiState(
    val session: UserSession? = null,
    val staffList: List<StaffProfile> = emptyList(),
    val allTasks: List<TaskItem> = emptyList(),
    val todayAttendance: List<AttendanceRecord> = emptyList(),
    val allTargets: List<StaffTarget> = emptyList(),
    val contentList: List<GenzPluseContentItem> = emptyList(),
    val announcements: List<AnnouncementItem> = emptyList(),
    val leaveRequests: List<LeaveRequest> = emptyList(),
    val problemReports: List<ProblemReport> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

class AdminViewModel(
    private val authRepository: AuthRepository,
    private val staffRepository: StaffRepository,
    private val taskRepository: TaskRepository,
    private val attendanceRepository: AttendanceRepository,
    private val targetRepository: TargetRepository,
    private val contentRepository: ContentRepository,
    private val announcementRepository: AnnouncementRepository,
    private val requestRepository: RequestRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState(isLoading = true))
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAdminData()
    }

    private fun loadAdminData() {
        viewModelScope.launch {
            authRepository.currentSession.collect { session ->
                _uiState.update { it.copy(session = session) }
            }
        }

        viewModelScope.launch {
            staffRepository.getAllStaff().collect { staff ->
                _uiState.update { it.copy(staffList = staff) }
            }
        }

        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                _uiState.update { it.copy(allTasks = tasks) }
            }
        }

        viewModelScope.launch {
            attendanceRepository.getAllStaffTodayAttendance().collect { att ->
                _uiState.update { it.copy(todayAttendance = att) }
            }
        }

        viewModelScope.launch {
            targetRepository.getAllStaffTargets().collect { targets ->
                _uiState.update { it.copy(allTargets = targets) }
            }
        }

        viewModelScope.launch {
            contentRepository.getAllContent().collect { content ->
                _uiState.update { it.copy(contentList = content) }
            }
        }

        viewModelScope.launch {
            announcementRepository.getAnnouncements().collect { ann ->
                _uiState.update { it.copy(announcements = ann) }
            }
        }

        viewModelScope.launch {
            requestRepository.getAllLeaveRequests().collect { leaves ->
                _uiState.update { it.copy(leaveRequests = leaves) }
            }
        }

        viewModelScope.launch {
            requestRepository.getAllProblemReports().collect { problems ->
                _uiState.update { it.copy(problemReports = problems, isLoading = false) }
            }
        }
    }

    fun addNewStaff(
        staffId: String,
        name: String,
        username: String,
        email: String,
        phone: String,
        department: String,
        designation: String,
        target: Int
    ) {
        viewModelScope.launch {
            val profile = StaffProfile(
                uid = "staff_${staffId.lowercase().replace("-", "_")}",
                staffId = staffId,
                name = name,
                username = username,
                email = email,
                phoneNumber = phone,
                department = department,
                designation = designation,
                joiningDate = "2026-09-04",
                avatarUrl = "",
                bio = "Newly onboarded staff member.",
                emergencyContact = "",
                bloodGroup = "",
                address = "",
                isActive = true,
                assignedTarget = target,
                completedTarget = 0
            )
            staffRepository.addStaff(profile, "Welcome123")
            // Also seed a default target record
            targetRepository.setStaffTarget(
                StaffTarget(
                    id = "",
                    staffId = staffId,
                    staffName = name,
                    title = "Monthly Target",
                    description = "Monthly delivery goal",
                    targetValue = target,
                    completedValue = 0,
                    unit = "Units",
                    startDate = "2026-09-01",
                    endDate = "2026-09-30",
                    status = "ACTIVE"
                )
            )
            showMessage("Staff member $name onboarded successfully with ID $staffId!")
        }
    }

    fun toggleStaffActive(staffId: String, currentActive: Boolean) {
        viewModelScope.launch {
            staffRepository.deactivateStaff(staffId, !currentActive)
            showMessage("Staff status updated to ${if (!currentActive) "Active" else "Deactivated"}.")
        }
    }

    fun assignTask(
        title: String,
        description: String,
        staffId: String,
        staffName: String,
        priority: TaskPriority,
        deadline: String,
        targetUnits: Int,
        resourcesLink: String
    ) {
        viewModelScope.launch {
            val task = TaskItem(
                id = "",
                title = title,
                description = description,
                assignedStaffId = staffId,
                assignedStaffName = staffName,
                assignedToMultipleNames = listOf(staffName),
                priority = priority,
                status = TaskStatus.NOT_STARTED,
                deadline = deadline,
                targetUnits = targetUnits,
                completedUnits = 0,
                progressPercentage = 0,
                resourcesLink = resourcesLink,
                adminFeedback = "",
                staffNotes = "",
                createdAt = "2026-09-04"
            )
            taskRepository.createTask(task)
            // Notify staff
            notificationRepository.sendNotification(
                AppNotification(
                    id = "",
                    targetUserId = staffId,
                    title = "New Task Assigned: $title",
                    message = "Admin assigned a new $priority priority task due $deadline.",
                    type = NotificationType.TASK_ASSIGNED,
                    timestamp = "Just now",
                    isRead = false,
                    actionDeepLink = "tasks"
                )
            )
            showMessage("Task successfully assigned to $staffName!")
        }
    }

    fun addAdminTaskFeedback(taskId: String, feedback: String) {
        viewModelScope.launch {
            taskRepository.addAdminFeedback(taskId, feedback)
            showMessage("Admin feedback submitted!")
        }
    }

    fun reviewContent(contentId: String, status: ContentStatus, adminNotes: String) {
        viewModelScope.launch {
            contentRepository.updateContentStatus(contentId, status, adminNotes)
            showMessage("Content marked as ${status.name}!")
        }
    }

    fun broadcastAnnouncement(
        title: String,
        message: String,
        priority: AnnouncementPriority,
        targetAudience: TargetAudience,
        actionUrl: String
    ) {
        viewModelScope.launch {
            val ann = AnnouncementItem(
                id = "",
                title = title,
                message = message,
                authorName = "Executive Operations",
                publishedAt = "Just now",
                priority = priority,
                targetAudience = targetAudience,
                isRead = false,
                actionUrl = actionUrl
            )
            announcementRepository.publishAnnouncement(ann)
            // Send broadcast push notification
            notificationRepository.sendNotification(
                AppNotification(
                    id = "",
                    targetUserId = "ALL",
                    title = "Official Announcement: $title",
                    message = message,
                    type = NotificationType.ANNOUNCEMENT,
                    timestamp = "Just now",
                    isRead = false,
                    actionDeepLink = "announcements"
                )
            )
            showMessage("Announcement broadcasted to all staff members!")
        }
    }

    fun updateLeaveStatus(requestId: String, status: RequestStatus, adminResponse: String) {
        viewModelScope.launch {
            requestRepository.updateLeaveStatus(requestId, status, adminResponse)
            showMessage("Leave request status updated to ${status.name}!")
        }
    }

    fun updateProblemStatus(reportId: String, status: RequestStatus, adminNotes: String) {
        viewModelScope.launch {
            requestRepository.updateProblemStatus(reportId, status, adminNotes)
            showMessage("Problem report updated to ${status.name}!")
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(userMessage = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val staffRepository: StaffRepository,
        private val taskRepository: TaskRepository,
        private val attendanceRepository: AttendanceRepository,
        private val targetRepository: TargetRepository,
        private val contentRepository: ContentRepository,
        private val announcementRepository: AnnouncementRepository,
        private val requestRepository: RequestRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(
                authRepository,
                staffRepository,
                taskRepository,
                attendanceRepository,
                targetRepository,
                contentRepository,
                announcementRepository,
                requestRepository,
                notificationRepository
            ) as T
        }
    }
}
