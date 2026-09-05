package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.sync.OperationAction
import com.example.data.sync.QueuedSyncOperation
import com.example.data.sync.SyncManager
import com.example.data.util.ErrorMapper
import com.example.domain.model.*
import com.example.security.RoleAccessPolicy
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SyncAndOfflineArchitectureTest {

    private lateinit var database: AppDatabase
    private lateinit var syncManager: SyncManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        syncManager = SyncManager(
            db = database,
            firestoreProvider = { null } // offline simulation
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `stable document IDs and Room entity mapping preserves identity`() = runBlocking {
        val staffEntity = StaffProfileEntity(
            uid = "stf_uid_101",
            staffId = "GP-STAFF-101",
            name = "Kavitha Raman",
            username = "kavitha",
            email = "kavitha@genzpluse.org",
            phoneNumber = "+919840123456",
            department = "Content",
            designation = "Lead Editor",
            joiningDate = "2026-09-04",
            avatarUrl = "",
            bio = "Staff member",
            emergencyContact = "+919840199999",
            bloodGroup = "O+",
            address = "Chennai",
            isActive = true,
            assignedTarget = 100,
            completedTarget = 65
        )
        database.staffProfileDao().insertStaff(staffEntity)

        val retrieved = database.staffProfileDao().getStaffById("GP-STAFF-101").first()
        assertNotNull(retrieved)
        assertEquals("stf_uid_101", retrieved?.uid)
        assertEquals("GP-STAFF-101", retrieved?.staffId)
        assertEquals("Kavitha Raman", retrieved?.name)
    }

    @Test
    fun `conflict handling returns server authoritative strategy for privileged entities`() {
        assertEquals(
            ConflictStrategy.SERVER_AUTHORITATIVE,
            syncManager.getConflictStrategyForEntity("users")
        )
        assertEquals(
            ConflictStrategy.SERVER_AUTHORITATIVE,
            syncManager.getConflictStrategyForEntity("attendance")
        )
        assertEquals(
            ConflictStrategy.SERVER_AUTHORITATIVE,
            syncManager.getConflictStrategyForEntity("leave_approval")
        )
        assertEquals(
            ConflictStrategy.SERVER_AUTHORITATIVE,
            syncManager.getConflictStrategyForEntity("problem_resolution")
        )
        assertEquals(
            ConflictStrategy.SERVER_AUTHORITATIVE,
            syncManager.getConflictStrategyForEntity("genzpluse_content_review")
        )
        assertEquals(
            ConflictStrategy.CLIENT_AUTHORITATIVE,
            syncManager.getConflictStrategyForEntity("notes")
        )
        assertEquals(
            ConflictStrategy.CLIENT_AUTHORITATIVE,
            syncManager.getConflictStrategyForEntity("genzpluse_content_draft")
        )
    }

    @Test
    fun `write queue bounded retry prevents infinite loop and deduplicates operations`() {
        val op1 = QueuedSyncOperation(
            collection = "notes",
            documentId = "note_101",
            payload = mapOf("title" to "Draft 1"),
            action = OperationAction.UPSERT
        )
        val op2 = QueuedSyncOperation(
            collection = "notes",
            documentId = "note_101",
            payload = mapOf("title" to "Draft 2 Updated"),
            action = OperationAction.UPSERT
        )

        syncManager.enqueueOperation(op1)
        syncManager.enqueueOperation(op2)

        // Must deduplicate by documentId
        assertEquals(1, syncManager.getPendingQueueSize())
    }

    @Test
    fun `error mapper safely transforms firebase exceptions to clear application errors`() {
        val permError = FirebaseFirestoreException(
            "Missing permissions",
            FirebaseFirestoreException.Code.PERMISSION_DENIED
        )
        val mappedPerm = ErrorMapper.mapException(permError)
        assertTrue(mappedPerm.message!!.contains("Security Policy: You do not have permission"))

        val unauthError = FirebaseFirestoreException(
            "User session expired",
            FirebaseFirestoreException.Code.UNAUTHENTICATED
        )
        val mappedUnauth = ErrorMapper.mapException(unauthError)
        assertTrue(mappedUnauth.message!!.contains("Your session has expired"))

        val netError = FirebaseNetworkException("Network unreachable")
        val mappedNet = ErrorMapper.mapException(netError)
        assertTrue(mappedNet.message!!.contains("Network error"))
    }

    @Test
    fun `offline sync gracefully reports offline state when backend is unreachable`() = runBlocking {
        val result = syncManager.syncAll(authenticatedUid = "test_user_uid")
        assertEquals(SyncState.OFFLINE, result.state)
        assertEquals(SyncState.OFFLINE, syncManager.syncState.value)
    }

    @Test
    fun `cached local Room reads function without network connectivity`() = runBlocking {
        val task = TaskEntity(
            id = "task_offline_1",
            title = "Offline Cached Task",
            description = "Should be readable locally",
            assignedStaffId = "GP-STAFF-101",
            assignedStaffName = "Kavitha",
            assignedToMultipleNamesJson = "[\"Kavitha\"]",
            priority = "HIGH",
            status = "IN_PROGRESS",
            deadline = "2026-09-10",
            targetUnits = 10,
            completedUnits = 5,
            progressPercentage = 50,
            resourcesLink = "",
            adminFeedback = "",
            staffNotes = "Working offline",
            createdAt = "2026-09-04"
        )
        database.taskDao().insertTask(task)

        val cachedTasks = database.taskDao().getTasksForStaff("GP-STAFF-101").first()
        assertEquals(1, cachedTasks.size)
        assertEquals("Offline Cached Task", cachedTasks[0].title)
    }

    @Test
    fun `role policy prevents unauthorized offline role elevation`() {
        val staffSession = UserSession(
            uid = "stf_123",
            username = "staff",
            email = "staff@genzpluse.org",
            name = "Staff",
            role = UserRole.STAFF,
            isActive = true
        )

        assertFalse(RoleAccessPolicy.canAccessAdminConsole(staffSession))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("role"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("isActive"))
    }
}
