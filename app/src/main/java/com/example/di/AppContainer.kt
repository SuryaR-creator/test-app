package com.example.di

import android.content.Context
import com.example.data.fcm.FcmTokenManager
import com.example.data.fcm.FcmTokenManagerImpl
import com.example.data.local.AppDatabase
import com.example.data.repository.*
import com.example.data.sync.SyncManager
import com.example.domain.repository.*
import com.example.security.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface AppContainer {
    val db: AppDatabase
    val authRepository: AuthRepository
    val staffRepository: StaffRepository
    val taskRepository: TaskRepository
    val attendanceRepository: AttendanceRepository
    val targetRepository: TargetRepository
    val contentRepository: ContentRepository
    val announcementRepository: AnnouncementRepository
    val noteRepository: NoteRepository
    val requestRepository: RequestRepository
    val notificationRepository: NotificationRepository
    val securityManager: SecurityManager
    val syncManager: SyncManager
    val fcmTokenManager: FcmTokenManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val fcmTokenManager: FcmTokenManager by lazy {
        FcmTokenManagerImpl(context)
    }

    override val db: AppDatabase by lazy {
        AppDatabase.getDatabase(context, appScope)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            db = db,
            fcmTokenManagerProvider = { fcmTokenManager },
            contextProvider = { context }
        )
    }

    override val staffRepository: StaffRepository by lazy {
        StaffRepositoryImpl(db)
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(db)
    }

    override val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepositoryImpl(db)
    }

    override val targetRepository: TargetRepository by lazy {
        TargetRepositoryImpl(db)
    }

    override val contentRepository: ContentRepository by lazy {
        ContentRepositoryImpl(db)
    }

    override val announcementRepository: AnnouncementRepository by lazy {
        AnnouncementRepositoryImpl(db)
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(db)
    }

    override val requestRepository: RequestRepository by lazy {
        RequestRepositoryImpl(db)
    }

    override val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(db)
    }

    override val securityManager: SecurityManager by lazy {
        SecurityManager()
    }

    override val syncManager: SyncManager by lazy {
        SyncManager(db)
    }
}
