package com.example.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StaffUiState(
    val session: UserSession? = null,
    val profile: StaffProfile? = null,
    val attendanceRecord: AttendanceRecord? = null,
    val attendanceSummary: AttendanceSummary = AttendanceSummary(),
    val tasks: List<TaskItem> = emptyList(),
    val target: StaffTarget? = null,
    val targetProgress: TargetProgress = TargetProgress(),
    val achievements: StaffAchievements = StaffAchievements(""),
    val announcements: List<AnnouncementItem> = emptyList(),
    val contentList: List<GenzPluseContentItem> = emptyList(),
    val notes: List<StaffNote> = emptyList(),
    val leaveRequests: List<LeaveRequest> = emptyList(),
    val problemReports: List<ProblemReport> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

class StaffViewModel(
    private val authRepository: AuthRepository,
    private val staffRepository: StaffRepository,
    private val taskRepository: TaskRepository,
    private val attendanceRepository: AttendanceRepository,
    private val targetRepository: TargetRepository,
    private val contentRepository: ContentRepository,
    private val announcementRepository: AnnouncementRepository,
    private val noteRepository: NoteRepository,
    private val requestRepository: RequestRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState(isLoading = true))
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    init {
        loadStaffData()
    }

    private fun loadStaffData() {
        viewModelScope.launch {
            authRepository.currentSession.collect { session ->
                if (session != null) {
                    val staffId = session.staffId.ifEmpty { session.uid }
                    _uiState.update { it.copy(session = session) }

                    // Collect tasks
                    launch {
                        taskRepository.getTasksForStaff(staffId).collect { tasks ->
                            _uiState.update { it.copy(tasks = tasks) }
                        }
                    }

                    // Collect profile
                    launch {
                        staffRepository.getStaffById(staffId).collect { profile ->
                            _uiState.update { it.copy(profile = profile) }
                        }
                    }

                    // Collect today attendance
                    launch {
                        attendanceRepository.getTodayAttendance(staffId).collect { record ->
                            _uiState.update { it.copy(attendanceRecord = record) }
                        }
                    }

                    // Collect attendance summary
                    launch {
                        attendanceRepository.getAttendanceSummary(staffId).collect { summary ->
                            _uiState.update { it.copy(attendanceSummary = summary) }
                        }
                    }

                    // Collect Target & Achievements
                    launch {
                        targetRepository.getTargetForStaff(staffId).collect { target ->
                            _uiState.update { it.copy(target = target) }
                        }
                    }
                    launch {
                        targetRepository.getTargetProgress(staffId).collect { progress ->
                            _uiState.update { it.copy(targetProgress = progress) }
                        }
                    }
                    launch {
                        targetRepository.getAchievements(staffId).collect { ach ->
                            _uiState.update { it.copy(achievements = ach) }
                        }
                    }

                    // Collect Announcements
                    launch {
                        announcementRepository.getAnnouncements().collect { ann ->
                            _uiState.update { it.copy(announcements = ann) }
                        }
                    }

                    // Collect GenzPluse Content
                    launch {
                        contentRepository.getStaffContent(staffId).collect { content ->
                            _uiState.update { it.copy(contentList = content) }
                        }
                    }

                    // Collect Notes
                    launch {
                        noteRepository.getNotesForUser(staffId).collect { notes ->
                            _uiState.update { it.copy(notes = notes) }
                        }
                    }

                    // Collect Requests
                    launch {
                        requestRepository.getStaffLeaveRequests(staffId).collect { leaves ->
                            _uiState.update { it.copy(leaveRequests = leaves) }
                        }
                    }
                    launch {
                        requestRepository.getStaffProblemReports(staffId).collect { problems ->
                            _uiState.update { it.copy(problemReports = problems) }
                        }
                    }

                    // Collect Notifications
                    launch {
                        notificationRepository.getNotificationsForUser(staffId).collect { notifs ->
                            _uiState.update { it.copy(notifications = notifs, isLoading = false) }
                        }
                    }
                }
            }
        }
    }

    fun checkInToday() {
        val session = _uiState.value.session ?: return
        val staffId = session.staffId.ifEmpty { session.uid }
        viewModelScope.launch {
            attendanceRepository.recordCheckIn(staffId, session.name)
            showMessage("Attendance checked-in successfully at 09:00 AM!")
        }
    }

    fun updateTaskProgress(taskId: String, status: TaskStatus, progress: Int, notes: String) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, status, progress, notes)
            showMessage("Task progress updated to $progress%!")
        }
    }

    fun createGenzPluseContent(
        title: String,
        category: ContentCategory,
        description: String,
        mainText: String,
        links: List<String>,
        tags: List<String>
    ) {
        val session = _uiState.value.session ?: return
        val staffId = session.staffId.ifEmpty { session.uid }
        viewModelScope.launch {
            val item = GenzPluseContentItem(
                id = "",
                creatorId = session.uid,
                creatorStaffId = staffId,
                creatorName = session.name,
                title = title,
                category = category,
                description = description,
                mainText = mainText,
                mediaUrl = "",
                referenceLinks = links,
                tags = tags,
                status = ContentStatus.SUBMITTED,
                adminReviewNotes = "",
                createdAt = "2026-09-04 11:30",
                updatedAt = "2026-09-04 11:30"
            )
            contentRepository.createContent(item)
            showMessage("Content submitted to ${category.name.replace("_", " ")} category for review!")
        }
    }

    fun saveNote(title: String, content: String, colorTag: Long, isPinned: Boolean, noteId: String = "") {
        val session = _uiState.value.session ?: return
        val staffId = session.staffId.ifEmpty { session.uid }
        viewModelScope.launch {
            val note = StaffNote(
                id = noteId,
                userId = staffId,
                title = title,
                content = content,
                links = emptyList(),
                tags = listOf("Personal"),
                colorTag = colorTag,
                isPinned = isPinned,
                createdAt = "2026-09-04",
                updatedAt = "2026-09-04"
            )
            noteRepository.saveNote(note)
            showMessage(if (noteId.isEmpty()) "Note saved successfully!" else "Note updated!")
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
            showMessage("Note deleted.")
        }
    }

    fun submitLeaveRequest(
        type: LeaveType,
        fromDate: String,
        toDate: String,
        days: Int,
        reason: String
    ) {
        val session = _uiState.value.session ?: return
        val staffId = session.staffId.ifEmpty { session.uid }
        viewModelScope.launch {
            val request = LeaveRequest(
                id = "",
                staffId = staffId,
                staffName = session.name,
                department = session.department.ifEmpty { "General" },
                leaveType = type,
                fromDate = fromDate,
                toDate = toDate,
                numberOfDays = days,
                reason = reason,
                status = RequestStatus.PENDING,
                submittedAt = "2026-09-04 10:00 AM"
            )
            requestRepository.submitLeaveRequest(request)
            showMessage("Leave request submitted for Admin approval!")
        }
    }

    fun submitProblemReport(
        category: ProblemCategory,
        title: String,
        description: String,
        priority: ProblemPriority
    ) {
        val session = _uiState.value.session ?: return
        val staffId = session.staffId.ifEmpty { session.uid }
        viewModelScope.launch {
            val report = ProblemReport(
                id = "",
                staffId = staffId,
                staffName = session.name,
                department = session.department.ifEmpty { "General" },
                category = category,
                title = title,
                description = description,
                priority = priority,
                status = RequestStatus.PENDING,
                submittedAt = "2026-09-04 10:30 AM"
            )
            requestRepository.submitProblemReport(report)
            showMessage("Problem report logged to Admin & IT desk!")
        }
    }

    fun updateSelfProfile(
        bio: String,
        emergencyContact: String,
        bloodGroup: String,
        address: String,
        phoneNumber: String
    ) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            staffRepository.updateStaffSelfProfile(session.uid, bio, emergencyContact, bloodGroup, address, phoneNumber)
            showMessage("Profile updated successfully!")
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead(id)
        }
    }

    fun markAnnouncementRead(id: String) {
        viewModelScope.launch {
            announcementRepository.markAsRead(id)
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
        private val noteRepository: NoteRepository,
        private val requestRepository: RequestRepository,
        private val notificationRepository: NotificationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StaffViewModel(
                authRepository,
                staffRepository,
                taskRepository,
                attendanceRepository,
                targetRepository,
                contentRepository,
                announcementRepository,
                noteRepository,
                requestRepository,
                notificationRepository
            ) as T
        }
    }
}
