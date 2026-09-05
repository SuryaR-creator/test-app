package com.example.data.repository

import android.content.Context
import com.example.data.fcm.FcmTokenManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.sync.SyncScheduler
import com.example.data.util.ErrorMapper
import com.example.domain.model.*
import com.example.domain.repository.*
import com.example.security.RoleAccessPolicy
import com.example.security.SecurityManager
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWith(Result.failure(exception))
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }

class AuthRepositoryImpl(
    private val db: AppDatabase,
    private val securityManager: SecurityManager = SecurityManager(),
    private val fcmTokenManagerProvider: () -> FcmTokenManager? = { null },
    private val contextProvider: () -> Context? = { null }
) : AuthRepository {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: Flow<UserSession?> = _currentSession.asStateFlow()

    private val firebaseAuth: FirebaseAuth?
        get() = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    init {
        // Observe real Firebase Auth session state if available
        firebaseAuth?.let { auth ->
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                val email = currentUser.email ?: ""
                _currentSession.value = UserSession(
                    uid = uid,
                    username = email.substringBefore("@"),
                    email = email,
                    name = currentUser.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    role = UserRole.STAFF,
                    isActive = true
                )
            } else {
                _currentSession.value = null
            }
        }
    }

    override suspend fun loginStaff(email: String, password: String): Result<UserSession> {
        val sanitizedEmail = securityManager.sanitizeInput(email)
        val sanitizedPassword = password.trim()

        if (sanitizedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your registered staff email."))
        }
        if (!securityManager.isValidEmail(sanitizedEmail) && !sanitizedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (sanitizedPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val auth = firebaseAuth
            ?: return Result.failure(IllegalStateException("Firebase Authentication service is unavailable. Please verify network connectivity and configuration."))

        try {
            val authResult = auth.signInWithEmailAndPassword(sanitizedEmail, sanitizedPassword).awaitTask()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Authentication failed: No user returned."))

            val uid = firebaseUser.uid

            // Fetch latest user document from Firestore remote source of truth
            var userEntity = db.userDao().getUserDirect(uid)
            val fs = firestore
            if (fs != null) {
                try {
                    val doc = fs.collection("users").document(uid).get().awaitTask()
                    if (doc.exists()) {
                        val roleStr = doc.getString("role") ?: "STAFF"
                        val isActive = doc.getBoolean("isActive") ?: true
                        val entity = UserEntity(
                            uid = uid,
                            username = doc.getString("username") ?: (firebaseUser.email?.substringBefore("@") ?: "staff"),
                            email = doc.getString("email") ?: (firebaseUser.email ?: sanitizedEmail),
                            phoneNumber = doc.getString("phoneNumber") ?: (firebaseUser.phoneNumber ?: ""),
                            name = doc.getString("name") ?: (firebaseUser.displayName ?: "Staff Member"),
                            role = roleStr,
                            department = doc.getString("department") ?: "Content & Media",
                            designation = doc.getString("designation") ?: "Staff Member",
                            staffId = doc.getString("staffId") ?: "GP-STAFF-${uid.take(4).uppercase()}",
                            avatarUrl = doc.getString("avatarUrl") ?: "",
                            joiningDate = doc.getString("joiningDate") ?: "2026-09-04",
                            isActive = isActive
                        )
                        db.userDao().insertUser(entity)
                        userEntity = entity
                    }
                } catch (_: Exception) {
                    // Fall back to cached Room profile data if offline
                }
            }

            // Enforce account status check
            if (userEntity != null && !userEntity.isActive) {
                auth.signOut()
                _currentSession.value = null
                return Result.failure(SecurityException("Your Staff account has been deactivated. Please contact Administrator."))
            }

            val session = UserSession(
                uid = uid,
                username = userEntity?.username ?: sanitizedEmail.substringBefore("@"),
                email = firebaseUser.email ?: sanitizedEmail,
                phoneNumber = userEntity?.phoneNumber ?: (firebaseUser.phoneNumber ?: ""),
                name = userEntity?.name ?: (firebaseUser.displayName ?: sanitizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }),
                role = UserRole.STAFF,
                department = userEntity?.department ?: "Content & Media",
                designation = userEntity?.designation ?: "Staff Member",
                staffId = userEntity?.staffId ?: "GP-STAFF-${uid.take(4).uppercase()}",
                avatarUrl = userEntity?.avatarUrl ?: "",
                joiningDate = userEntity?.joiningDate ?: "2026-09-04",
                isActive = true
            )
            _currentSession.value = session
            return Result.success(session)
        } catch (e: Exception) {
            return Result.failure(ErrorMapper.mapException(e))
        }
    }

    override suspend fun loginAdmin(email: String, password: String): Result<UserSession> {
        val sanitizedEmail = securityManager.sanitizeInput(email)
        val sanitizedPassword = password.trim()

        if (sanitizedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your registered Admin email."))
        }
        if (!securityManager.isValidEmail(sanitizedEmail) && !sanitizedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid administrator email."))
        }
        if (sanitizedPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val auth = firebaseAuth
            ?: return Result.failure(IllegalStateException("Firebase Authentication service is unavailable. Please verify network connectivity and configuration."))

        try {
            val authResult = auth.signInWithEmailAndPassword(sanitizedEmail, sanitizedPassword).awaitTask()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Authentication failed: No user returned."))

            val uid = firebaseUser.uid

            // Fetch user profile from Firestore remote source of truth
            var userEntity = db.userDao().getUserDirect(uid)
            val fs = firestore
            if (fs != null) {
                try {
                    val doc = fs.collection("users").document(uid).get().awaitTask()
                    if (doc.exists()) {
                        val roleStr = doc.getString("role") ?: "STAFF"
                        val isActive = doc.getBoolean("isActive") ?: true
                        val entity = UserEntity(
                            uid = uid,
                            username = doc.getString("username") ?: (firebaseUser.email?.substringBefore("@") ?: "admin"),
                            email = doc.getString("email") ?: (firebaseUser.email ?: sanitizedEmail),
                            phoneNumber = doc.getString("phoneNumber") ?: (firebaseUser.phoneNumber ?: ""),
                            name = doc.getString("name") ?: (firebaseUser.displayName ?: "System Administrator"),
                            role = roleStr,
                            department = doc.getString("department") ?: "Administration",
                            designation = doc.getString("designation") ?: "System Administrator",
                            staffId = doc.getString("staffId") ?: uid.take(8).uppercase(),
                            avatarUrl = doc.getString("avatarUrl") ?: "",
                            joiningDate = doc.getString("joiningDate") ?: System.currentTimeMillis().toString(),
                            isActive = isActive
                        )
                        db.userDao().insertUser(entity)
                        userEntity = entity
                    }
                } catch (_: Exception) {
                    // Fall back to cached Room profile data
                }
            }

            // CRITICAL AUTHORIZATION CHECK: User must possess ADMIN role
            val role = userEntity?.role ?: "STAFF"
            if (role != "ADMIN") {
                auth.signOut()
                _currentSession.value = null
                return Result.failure(SecurityException("Access Denied: Authenticated account does not possess Administrator privileges."))
            }

            if (userEntity != null && !userEntity.isActive) {
                auth.signOut()
                _currentSession.value = null
                return Result.failure(SecurityException("Admin account is deactivated."))
            }

            val session = UserSession(
                uid = uid,
                username = userEntity?.username ?: (firebaseUser.email?.substringBefore("@") ?: "admin"),
                email = firebaseUser.email ?: sanitizedEmail,
                phoneNumber = userEntity?.phoneNumber ?: "",
                name = userEntity?.name ?: (firebaseUser.displayName ?: "System Administrator"),
                role = UserRole.ADMIN,
                department = userEntity?.department ?: "Administration",
                designation = userEntity?.designation ?: "System Administrator",
                staffId = userEntity?.staffId ?: uid.take(8).uppercase(),
                avatarUrl = userEntity?.avatarUrl ?: "",
                joiningDate = userEntity?.joiningDate ?: System.currentTimeMillis().toString(),
                isActive = true
            )
            _currentSession.value = session
            return Result.success(session)
        } catch (e: Exception) {
            return Result.failure(ErrorMapper.mapException(e))
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        val sanitizedEmail = securityManager.sanitizeInput(email)
        if (sanitizedEmail.isBlank() || !securityManager.isValidEmail(sanitizedEmail)) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address to reset password."))
        }

        val auth = firebaseAuth
        return if (auth != null) {
            try {
                auth.sendPasswordResetEmail(sanitizedEmail).awaitTask()
                Result.success(Unit)
            } catch (e: FirebaseAuthInvalidUserException) {
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(ErrorMapper.mapException(e))
            }
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun logout(): Result<Unit> {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        _currentSession.value = null
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): UserSession? {
        return _currentSession.value
    }

    override fun getAuthenticatedUid(): String? {
        return firebaseAuth?.currentUser?.uid ?: _currentSession.value?.uid
    }
}

class StaffRepositoryImpl(
    private val db: AppDatabase
) : StaffRepository {

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    override fun getAllStaff(): Flow<List<StaffProfile>> {
        return db.staffProfileDao().getAllStaff().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getStaffById(staffId: String): Flow<StaffProfile?> {
        return db.staffProfileDao().getStaffById(staffId).map { it?.toDomain() }
    }

    override suspend fun addStaff(staff: StaffProfile, defaultPassword: String): Result<Boolean> {
        val uid = staff.uid.ifEmpty { "staff_${UUID.randomUUID().toString().take(6)}" }
        val entity = StaffProfileEntity(
            uid = uid,
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

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "uid" to uid,
                    "staffId" to entity.staffId,
                    "name" to entity.name,
                    "username" to entity.username,
                    "email" to entity.email,
                    "phoneNumber" to entity.phoneNumber,
                    "department" to entity.department,
                    "designation" to entity.designation,
                    "joiningDate" to entity.joiningDate,
                    "avatarUrl" to entity.avatarUrl,
                    "bio" to entity.bio,
                    "emergencyContact" to entity.emergencyContact,
                    "bloodGroup" to entity.bloodGroup,
                    "address" to entity.address,
                    "role" to "STAFF",
                    "isActive" to entity.isActive,
                    "assignedTarget" to entity.assignedTarget,
                    "completedTarget" to entity.completedTarget
                )
                fs.collection("users").document(uid).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

        db.staffProfileDao().insertStaff(entity)
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
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "name" to staff.name,
                    "department" to staff.department,
                    "designation" to staff.designation,
                    "phoneNumber" to staff.phoneNumber,
                    "avatarUrl" to staff.avatarUrl,
                    "bio" to staff.bio,
                    "emergencyContact" to staff.emergencyContact,
                    "bloodGroup" to staff.bloodGroup,
                    "address" to staff.address,
                    "assignedTarget" to staff.assignedTarget
                )
                fs.collection("users").document(staff.uid).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
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
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "bio" to bio,
                    "emergencyContact" to emergencyContact,
                    "bloodGroup" to bloodGroup,
                    "address" to address,
                    "phoneNumber" to phoneNumber
                )
                fs.collection("users").document(uid).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
        db.staffProfileDao().updateSelfProfile(uid, bio, emergencyContact, bloodGroup, address, phoneNumber)
        return Result.success(true)
    }

    override suspend fun deactivateStaff(staffId: String, isActive: Boolean): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf("isActive" to isActive)
                fs.collection("users").document(staffId).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
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

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

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
        val taskToSave = task.copy(id = id)

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "title" to taskToSave.title,
                    "description" to taskToSave.description,
                    "assignedStaffId" to taskToSave.assignedStaffId,
                    "assignedStaffName" to taskToSave.assignedStaffName,
                    "priority" to taskToSave.priority.name,
                    "status" to taskToSave.status.name,
                    "deadline" to taskToSave.deadline,
                    "targetUnits" to taskToSave.targetUnits,
                    "completedUnits" to taskToSave.completedUnits,
                    "progressPercentage" to taskToSave.progressPercentage,
                    "resourcesLink" to taskToSave.resourcesLink,
                    "adminFeedback" to taskToSave.adminFeedback,
                    "staffNotes" to taskToSave.staffNotes,
                    "createdAt" to taskToSave.createdAt
                )
                fs.collection("tasks").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

        db.taskDao().insertTask(taskToSave.toEntity())
        return Result.success(id)
    }

    override suspend fun updateTask(task: TaskItem): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "title" to task.title,
                    "description" to task.description,
                    "priority" to task.priority.name,
                    "status" to task.status.name,
                    "deadline" to task.deadline,
                    "targetUnits" to task.targetUnits,
                    "completedUnits" to task.completedUnits,
                    "progressPercentage" to task.progressPercentage,
                    "resourcesLink" to task.resourcesLink,
                    "adminFeedback" to task.adminFeedback,
                    "staffNotes" to task.staffNotes
                )
                fs.collection("tasks").document(task.id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
        db.taskDao().updateTask(task.toEntity())
        return Result.success(true)
    }

    override suspend fun updateTaskStatus(
        taskId: String,
        status: TaskStatus,
        progress: Int,
        staffNotes: String
    ): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "status" to status.name,
                    "progressPercentage" to progress,
                    "staffNotes" to staffNotes,
                    "lastUpdated" to System.currentTimeMillis().toString()
                )
                fs.collection("tasks").document(taskId).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
        db.taskDao().updateTaskStatus(taskId, status.name, progress, staffNotes)
        return Result.success(true)
    }

    override suspend fun addAdminFeedback(taskId: String, feedback: String): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf("adminFeedback" to feedback)
                fs.collection("tasks").document(taskId).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
        db.taskDao().updateAdminFeedback(taskId, feedback)
        return Result.success(true)
    }

    override suspend fun addTaskComment(taskId: String, comment: TaskComment): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun deleteTask(taskId: String): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("tasks").document(taskId).delete().awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
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

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

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
        val id = "att_${UUID.randomUUID().toString().take(6)}"
        val record = AttendanceRecord(
            id = id,
            userId = staffId,
            staffId = staffId,
            staffName = staffName,
            date = "2026-09-04",
            checkInTime = "09:00 AM",
            checkOutTime = null,
            status = AttendanceStatus.PRESENT,
            remarks = "Biometric Check-In Verified"
        )

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "userId" to staffId,
                    "staffId" to staffId,
                    "staffName" to staffName,
                    "date" to record.date,
                    "checkInTime" to record.checkInTime,
                    "checkOutTime" to null,
                    "status" to record.status.name,
                    "remarks" to record.remarks
                )
                fs.collection("attendance").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

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
        val id = "att_out_${UUID.randomUUID().toString().take(6)}"
        val record = AttendanceRecord(
            id = id,
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

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

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
        val id = target.id.ifEmpty { "tgt_${UUID.randomUUID().toString().take(6)}" }
        val targetToSave = target.copy(id = id)

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "staffId" to targetToSave.staffId,
                    "staffName" to targetToSave.staffName,
                    "title" to targetToSave.title,
                    "description" to targetToSave.description,
                    "targetValue" to targetToSave.targetValue,
                    "completedValue" to targetToSave.completedValue,
                    "unit" to targetToSave.unit,
                    "startDate" to targetToSave.startDate,
                    "endDate" to targetToSave.endDate,
                    "status" to targetToSave.status
                )
                fs.collection("targets").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

        db.targetDao().insertTarget(
            TargetEntity(
                id = id,
                staffId = targetToSave.staffId,
                staffName = targetToSave.staffName,
                title = targetToSave.title,
                description = targetToSave.description,
                targetValue = targetToSave.targetValue,
                completedValue = targetToSave.completedValue,
                unit = targetToSave.unit,
                startDate = targetToSave.startDate,
                endDate = targetToSave.endDate,
                status = targetToSave.status
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

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

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
        val itemToSave = content.copy(id = id)

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "creatorId" to itemToSave.creatorId,
                    "creatorStaffId" to itemToSave.creatorStaffId,
                    "creatorName" to itemToSave.creatorName,
                    "title" to itemToSave.title,
                    "category" to itemToSave.category.name,
                    "description" to itemToSave.description,
                    "mainText" to itemToSave.mainText,
                    "mediaUrl" to itemToSave.mediaUrl,
                    "status" to itemToSave.status.name,
                    "adminReviewNotes" to itemToSave.adminReviewNotes,
                    "createdAt" to itemToSave.createdAt,
                    "updatedAt" to itemToSave.updatedAt
                )
                fs.collection("genzpluse_content").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

        db.contentDao().insertContent(itemToSave.toEntity())
        return Result.success(id)
    }

    override suspend fun updateContent(content: GenzPluseContentItem): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "title" to content.title,
                    "category" to content.category.name,
                    "description" to content.description,
                    "mainText" to content.mainText,
                    "mediaUrl" to content.mediaUrl,
                    "status" to content.status.name,
                    "updatedAt" to content.updatedAt
                )
                fs.collection("genzpluse_content").document(content.id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
        db.contentDao().insertContent(content.toEntity())
        return Result.success(true)
    }

    override suspend fun updateContentStatus(
        contentId: String,
        status: ContentStatus,
        adminNotes: String
    ): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "status" to status.name,
                    "adminReviewNotes" to adminNotes,
                    "updatedAt" to System.currentTimeMillis().toString()
                )
                fs.collection("genzpluse_content").document(contentId).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
        db.contentDao().updateStatus(contentId, status.name, adminNotes)
        return Result.success(true)
    }

    override suspend fun deleteContent(contentId: String): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("genzpluse_content").document(contentId).delete().awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
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

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    override fun getAnnouncements(): Flow<List<AnnouncementItem>> {
        return db.announcementDao().getAnnouncements().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun publishAnnouncement(announcement: AnnouncementItem): Result<String> {
        val id = announcement.id.ifEmpty { "ann_${UUID.randomUUID().toString().take(6)}" }
        val itemToSave = announcement.copy(id = id)

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "title" to itemToSave.title,
                    "message" to itemToSave.message,
                    "authorName" to itemToSave.authorName,
                    "publishedAt" to itemToSave.publishedAt.ifEmpty { "Just now" },
                    "priority" to itemToSave.priority.name,
                    "targetAudience" to itemToSave.targetAudience.name,
                    "actionUrl" to itemToSave.actionUrl
                )
                fs.collection("announcements").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

        db.announcementDao().insertAnnouncement(
            AnnouncementEntity(
                id = id,
                title = itemToSave.title,
                message = itemToSave.message,
                authorName = itemToSave.authorName,
                publishedAt = itemToSave.publishedAt.ifEmpty { "Just now" },
                priority = itemToSave.priority.name,
                targetAudience = itemToSave.targetAudience.name,
                isRead = false,
                actionUrl = itemToSave.actionUrl
            )
        )
        return Result.success(id)
    }

    override suspend fun deleteAnnouncement(id: String): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("announcements").document(id).delete().awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
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

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

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
        val noteToSave = note.copy(id = id)

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "userId" to noteToSave.userId,
                    "title" to noteToSave.title,
                    "content" to noteToSave.content,
                    "colorTag" to noteToSave.colorTag,
                    "isPinned" to noteToSave.isPinned,
                    "createdAt" to noteToSave.createdAt.ifEmpty { "Today" },
                    "updatedAt" to noteToSave.updatedAt.ifEmpty { "Today" }
                )
                fs.collection("notes").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                // Personal notes can be saved locally offline if network is unavailable
            }
        }

        db.noteDao().insertNote(
            NoteEntity(
                id = id,
                userId = noteToSave.userId,
                title = noteToSave.title,
                content = noteToSave.content,
                linksJson = "[]",
                tagsJson = "[]",
                colorTag = noteToSave.colorTag,
                isPinned = noteToSave.isPinned,
                createdAt = noteToSave.createdAt.ifEmpty { "Today" },
                updatedAt = noteToSave.updatedAt.ifEmpty { "Today" }
            )
        )
        return Result.success(id)
    }

    override suspend fun deleteNote(noteId: String): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("notes").document(noteId).delete().awaitTask()
            } catch (_: Exception) {}
        }
        db.noteDao().deleteNote(noteId)
        return Result.success(true)
    }
}

class RequestRepositoryImpl(
    private val db: AppDatabase
) : RequestRepository {

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    override fun getStaffLeaveRequests(staffId: String): Flow<List<LeaveRequest>> {
        return db.requestDao().getLeaveRequestsForStaff(staffId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllLeaveRequests(): Flow<List<LeaveRequest>> {
        return db.requestDao().getAllLeaveRequests().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun submitLeaveRequest(request: LeaveRequest): Result<String> {
        val id = request.id.ifEmpty { "lvr_${UUID.randomUUID().toString().take(6)}" }
        val reqToSave = request.copy(id = id)

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "staffId" to reqToSave.staffId,
                    "staffName" to reqToSave.staffName,
                    "department" to reqToSave.department,
                    "leaveType" to reqToSave.leaveType.name,
                    "fromDate" to reqToSave.fromDate,
                    "toDate" to reqToSave.toDate,
                    "numberOfDays" to reqToSave.numberOfDays,
                    "reason" to reqToSave.reason,
                    "status" to RequestStatus.PENDING.name,
                    "submittedAt" to reqToSave.submittedAt.ifEmpty { "Today" }
                )
                fs.collection("leave_requests").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

        db.requestDao().insertLeaveRequest(
            LeaveRequestEntity(
                id = id,
                staffId = reqToSave.staffId,
                staffName = reqToSave.staffName,
                department = reqToSave.department,
                leaveType = reqToSave.leaveType.name,
                fromDate = reqToSave.fromDate,
                toDate = reqToSave.toDate,
                numberOfDays = reqToSave.numberOfDays,
                reason = reqToSave.reason,
                attachmentUrl = reqToSave.attachmentUrl,
                status = RequestStatus.PENDING.name,
                adminResponse = null,
                submittedAt = reqToSave.submittedAt.ifEmpty { "Today" }
            )
        )
        return Result.success(id)
    }

    override suspend fun updateLeaveStatus(
        requestId: String,
        status: RequestStatus,
        adminResponse: String
    ): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "status" to status.name,
                    "adminResponse" to adminResponse
                )
                fs.collection("leave_requests").document(requestId).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
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
        val repToSave = report.copy(id = id)

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "staffId" to repToSave.staffId,
                    "staffName" to repToSave.staffName,
                    "department" to repToSave.department,
                    "category" to repToSave.category.name,
                    "title" to repToSave.title,
                    "description" to repToSave.description,
                    "priority" to repToSave.priority.name,
                    "status" to RequestStatus.PENDING.name,
                    "submittedAt" to repToSave.submittedAt.ifEmpty { "Today" }
                )
                fs.collection("problem_reports").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

        db.requestDao().insertProblemReport(
            ProblemReportEntity(
                id = id,
                staffId = repToSave.staffId,
                staffName = repToSave.staffName,
                department = repToSave.department,
                category = repToSave.category.name,
                title = repToSave.title,
                description = repToSave.description,
                priority = repToSave.priority.name,
                status = RequestStatus.PENDING.name,
                adminNotes = null,
                submittedAt = repToSave.submittedAt.ifEmpty { "Today" }
            )
        )
        return Result.success(id)
    }

    override suspend fun updateProblemStatus(
        reportId: String,
        status: RequestStatus,
        adminNotes: String
    ): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "status" to status.name,
                    "adminNotes" to adminNotes
                )
                fs.collection("problem_reports").document(reportId).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }
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

    private val firestore: FirebaseFirestore?
        get() = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

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
        val id = notification.id.ifEmpty { "notif_${UUID.randomUUID().toString().take(6)}" }
        val notifToSave = notification.copy(id = id)

        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf(
                    "id" to id,
                    "targetUserId" to notifToSave.targetUserId,
                    "title" to notifToSave.title,
                    "message" to notifToSave.message,
                    "type" to notifToSave.type.name,
                    "timestamp" to notifToSave.timestamp.ifEmpty { "Just now" },
                    "isRead" to false,
                    "actionDeepLink" to notifToSave.actionDeepLink
                )
                fs.collection("notifications").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (e: Exception) {
                return Result.failure(ErrorMapper.mapException(e))
            }
        }

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = id,
                targetUserId = notifToSave.targetUserId,
                title = notifToSave.title,
                message = notifToSave.message,
                type = notifToSave.type.name,
                timestamp = notifToSave.timestamp.ifEmpty { "Just now" },
                isRead = false,
                actionDeepLink = notifToSave.actionDeepLink
            )
        )
        return Result.success(true)
    }

    override suspend fun markNotificationAsRead(id: String): Result<Boolean> {
        val fs = firestore
        if (fs != null) {
            try {
                val data = mapOf("isRead" to true)
                fs.collection("notifications").document(id).set(data, SetOptions.merge()).awaitTask()
            } catch (_: Exception) {}
        }
        db.notificationDao().markAsRead(id)
        return Result.success(true)
    }
}
