package com.example.data.sync

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.coroutines.resume

private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWith(Result.failure(exception)) }
        addOnCanceledListener { continuation.cancel() }
    }

data class QueuedSyncOperation(
    val id: String = UUID.randomUUID().toString(),
    val collection: String,
    val documentId: String,
    val payload: Map<String, Any?>,
    val action: OperationAction,
    val maxRetries: Int = 3,
    var retryCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class OperationAction {
    UPSERT,
    DELETE
}

class SyncManager(
    private val db: AppDatabase,
    private val firestoreProvider: () -> FirebaseFirestore? = {
        runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _syncState = MutableStateFlow(SyncState.ONLINE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val writeQueue = mutableListOf<QueuedSyncOperation>()
    private val queueMutex = Any()

    fun getConflictStrategyForEntity(entityType: String): ConflictStrategy {
        return when (entityType) {
            "users", "user_role", "user_active_status" -> ConflictStrategy.SERVER_AUTHORITATIVE
            "attendance" -> ConflictStrategy.SERVER_AUTHORITATIVE
            "tasks_assignment" -> ConflictStrategy.SERVER_AUTHORITATIVE
            "leave_approval", "problem_resolution" -> ConflictStrategy.SERVER_AUTHORITATIVE
            "genzpluse_content_review" -> ConflictStrategy.SERVER_AUTHORITATIVE
            "genzpluse_content_draft" -> ConflictStrategy.CLIENT_AUTHORITATIVE
            "notes" -> ConflictStrategy.CLIENT_AUTHORITATIVE
            else -> ConflictStrategy.MERGE_PREFER_SERVER
        }
    }

    suspend fun syncAll(authenticatedUid: String?): SyncResult = withContext(dispatcher) {
        val fs = firestoreProvider()
        if (fs == null || authenticatedUid == null) {
            _syncState.value = SyncState.OFFLINE
            return@withContext SyncResult(
                state = SyncState.OFFLINE,
                itemsSynced = 0,
                errorMessage = "Remote backend unavailable. Operating in offline cache mode."
            )
        }

        _syncState.value = SyncState.SYNCING
        var totalSynced = 0

        try {
            // Process pending offline write queue first
            processPendingQueue()

            // 1. Sync Staff & Users
            val userDocs = fs.collection("users").get().await()
            for (doc in userDocs.documents) {
                val uid = doc.id
                val roleStr = doc.getString("role") ?: "STAFF"
                val isActive = doc.getBoolean("isActive") ?: true
                val staffProfile = StaffProfileEntity(
                    uid = uid,
                    staffId = doc.getString("staffId") ?: "GP-STAFF-${uid.take(4).uppercase()}",
                    name = doc.getString("name") ?: "Staff Member",
                    username = doc.getString("username") ?: "staff",
                    email = doc.getString("email") ?: "",
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    department = doc.getString("department") ?: "Content & Media",
                    designation = doc.getString("designation") ?: "Staff Member",
                    joiningDate = doc.getString("joiningDate") ?: "2026-09-04",
                    avatarUrl = doc.getString("avatarUrl") ?: "",
                    bio = doc.getString("bio") ?: "",
                    emergencyContact = doc.getString("emergencyContact") ?: "",
                    bloodGroup = doc.getString("bloodGroup") ?: "",
                    address = doc.getString("address") ?: "",
                    isActive = isActive,
                    assignedTarget = (doc.getLong("assignedTarget") ?: 100).toInt(),
                    completedTarget = (doc.getLong("completedTarget") ?: 0).toInt()
                )
                db.staffProfileDao().insertStaff(staffProfile)
                db.userDao().insertUser(
                    UserEntity(
                        uid = uid,
                        username = staffProfile.username,
                        email = staffProfile.email,
                        phoneNumber = staffProfile.phoneNumber,
                        name = staffProfile.name,
                        role = roleStr,
                        department = staffProfile.department,
                        designation = staffProfile.designation,
                        staffId = staffProfile.staffId,
                        avatarUrl = staffProfile.avatarUrl,
                        joiningDate = staffProfile.joiningDate,
                        isActive = isActive
                    )
                )
                totalSynced++
            }

            // 2. Sync Tasks
            val taskDocs = fs.collection("tasks").get().await()
            for (doc in taskDocs.documents) {
                val task = TaskEntity(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    assignedStaffId = doc.getString("assignedStaffId") ?: "",
                    assignedStaffName = doc.getString("assignedStaffName") ?: "",
                    assignedToMultipleNamesJson = "[\"${doc.getString("assignedStaffName") ?: ""}\"]",
                    priority = doc.getString("priority") ?: "MEDIUM",
                    status = doc.getString("status") ?: "NOT_STARTED",
                    deadline = doc.getString("deadline") ?: "",
                    targetUnits = (doc.getLong("targetUnits") ?: 0).toInt(),
                    completedUnits = (doc.getLong("completedUnits") ?: 0).toInt(),
                    progressPercentage = (doc.getLong("progressPercentage") ?: 0).toInt(),
                    resourcesLink = doc.getString("resourcesLink") ?: "",
                    adminFeedback = doc.getString("adminFeedback") ?: "",
                    staffNotes = doc.getString("staffNotes") ?: "",
                    createdAt = doc.getString("createdAt") ?: ""
                )
                db.taskDao().insertTask(task)
                totalSynced++
            }

            // 3. Sync Announcements
            val annDocs = fs.collection("announcements").get().await()
            for (doc in annDocs.documents) {
                val ann = AnnouncementEntity(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    message = doc.getString("message") ?: "",
                    authorName = doc.getString("authorName") ?: "",
                    publishedAt = doc.getString("publishedAt") ?: "",
                    priority = doc.getString("priority") ?: "NORMAL",
                    targetAudience = doc.getString("targetAudience") ?: "ALL_STAFF",
                    isRead = doc.getBoolean("isRead") ?: false,
                    actionUrl = doc.getString("actionUrl") ?: ""
                )
                db.announcementDao().insertAnnouncement(ann)
                totalSynced++
            }

            // 4. Sync Content
            val contentDocs = fs.collection("genzpluse_content").get().await()
            for (doc in contentDocs.documents) {
                val content = ContentEntity(
                    id = doc.id,
                    creatorId = doc.getString("creatorId") ?: "",
                    creatorStaffId = doc.getString("creatorStaffId") ?: "",
                    creatorName = doc.getString("creatorName") ?: "",
                    title = doc.getString("title") ?: "",
                    category = doc.getString("category") ?: "REELS",
                    description = doc.getString("description") ?: "",
                    mainText = doc.getString("mainText") ?: "",
                    mediaUrl = doc.getString("mediaUrl") ?: "",
                    referenceLinksJson = "[]",
                    tagsJson = "[]",
                    status = doc.getString("status") ?: "SUBMITTED",
                    adminReviewNotes = doc.getString("adminReviewNotes") ?: "",
                    createdAt = doc.getString("createdAt") ?: "",
                    updatedAt = doc.getString("updatedAt") ?: ""
                )
                db.contentDao().insertContent(content)
                totalSynced++
            }

            // 5. Sync Private Notes for user
            val noteDocs = fs.collection("notes")
                .whereEqualTo("userId", authenticatedUid)
                .get().await()
            for (doc in noteDocs.documents) {
                val note = NoteEntity(
                    id = doc.id,
                    userId = doc.getString("userId") ?: authenticatedUid,
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    linksJson = "[]",
                    tagsJson = "[]",
                    colorTag = doc.getLong("colorTag") ?: 0xFF3B82F6,
                    isPinned = doc.getBoolean("isPinned") ?: false,
                    createdAt = doc.getString("createdAt") ?: "",
                    updatedAt = doc.getString("updatedAt") ?: ""
                )
                db.noteDao().insertNote(note)
                totalSynced++
            }

            _syncState.value = SyncState.SYNC_SUCCESS
            SyncResult(SyncState.SYNC_SUCCESS, totalSynced)
        } catch (e: Exception) {
            _syncState.value = SyncState.SYNC_ERROR
            SyncResult(SyncState.SYNC_ERROR, totalSynced, e.message)
        }
    }

    fun enqueueOperation(op: QueuedSyncOperation) {
        synchronized(queueMutex) {
            // Prevent duplicate actions on the same document in queue
            writeQueue.removeAll { it.collection == op.collection && it.documentId == op.documentId }
            writeQueue.add(op)
        }
    }

    private suspend fun processPendingQueue() {
        val fs = firestoreProvider() ?: return
        val pendingOps: List<QueuedSyncOperation>
        synchronized(queueMutex) {
            pendingOps = writeQueue.toList()
        }

        for (op in pendingOps) {
            if (op.retryCount >= op.maxRetries) {
                synchronized(queueMutex) {
                    writeQueue.remove(op)
                }
                continue
            }

            try {
                when (op.action) {
                    OperationAction.UPSERT -> {
                        fs.collection(op.collection)
                            .document(op.documentId)
                            .set(op.payload, SetOptions.merge())
                            .await()
                    }
                    OperationAction.DELETE -> {
                        fs.collection(op.collection)
                            .document(op.documentId)
                            .delete()
                            .await()
                    }
                }
                synchronized(queueMutex) {
                    writeQueue.remove(op)
                }
            } catch (e: Exception) {
                op.retryCount++
                if (op.retryCount >= op.maxRetries) {
                    synchronized(queueMutex) {
                        writeQueue.remove(op)
                    }
                }
            }
        }
    }

    fun getPendingQueueSize(): Int {
        synchronized(queueMutex) {
            return writeQueue.size
        }
    }
}
