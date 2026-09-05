package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        StaffProfileEntity::class,
        TaskEntity::class,
        AttendanceEntity::class,
        TargetEntity::class,
        AnnouncementEntity::class,
        NoteEntity::class,
        ContentEntity::class,
        LeaveRequestEntity::class,
        ProblemReportEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun staffProfileDao(): StaffProfileDao
    abstract fun taskDao(): TaskDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun targetDao(): TargetDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun noteDao(): NoteDao
    abstract fun contentDao(): ContentDao
    abstract fun requestDao(): RequestDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "genzpluse_staff_db"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialEnterpriseData(database)
                    }
                }
            }
        }

        suspend fun populateInitialEnterpriseData(db: AppDatabase) {
            // Seed Staff Profiles
            val staffProfiles = listOf(
                StaffProfileEntity(
                    uid = "staff_001",
                    staffId = "GP-STAFF-101",
                    name = "Kavitha Raman",
                    username = "kavitha",
                    email = "kavitha.raman@genzpluse.org",
                    phoneNumber = "+91 98401 23456",
                    department = "Content & Media",
                    designation = "Senior Reels Editor",
                    joiningDate = "2024-03-15",
                    avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150",
                    bio = "Creative multimedia editor specializing in viral Genz short-form storytelling and brand reels.",
                    emergencyContact = "+91 98401 99999 (Spouse)",
                    bloodGroup = "O+",
                    address = "42 Silicon Avenue, Chennai",
                    isActive = true,
                    assignedTarget = 80,
                    completedTarget = 62
                ),
                StaffProfileEntity(
                    uid = "staff_002",
                    staffId = "GP-STAFF-102",
                    name = "Arjun Verma",
                    username = "arjun",
                    email = "arjun.verma@genzpluse.org",
                    phoneNumber = "+91 98402 34567",
                    department = "Editorial & News",
                    designation = "Lead News Template Designer",
                    joiningDate = "2024-01-10",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                    bio = "Design journalist passionate about structured news graphics and visual communication.",
                    emergencyContact = "+91 98402 88888 (Brother)",
                    bloodGroup = "B+",
                    address = "18 Lake Road, Bangalore",
                    isActive = true,
                    assignedTarget = 100,
                    completedTarget = 85
                ),
                StaffProfileEntity(
                    uid = "staff_003",
                    staffId = "GP-STAFF-103",
                    name = "Priya Sharma",
                    username = "priya",
                    email = "priya.sharma@genzpluse.org",
                    phoneNumber = "+91 98403 45678",
                    department = "Motion Graphics",
                    designation = "Landscape Visual Artist",
                    joiningDate = "2024-06-01",
                    avatarUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150",
                    bio = "Motion designer crafting cinematic landscape visual assets for GenzPluse broadcast.",
                    emergencyContact = "+91 98403 77777 (Father)",
                    bloodGroup = "A+",
                    address = "77 Tech Park, Hyderabad",
                    isActive = true,
                    assignedTarget = 60,
                    completedTarget = 45
                )
            )
            db.staffProfileDao().insertAll(staffProfiles)

            // Seed Users (for Staff and Admin login)
            val users = listOf(
                UserEntity(
                    uid = "staff_001",
                    username = "kavitha",
                    email = "kavitha.raman@genzpluse.org",
                    phoneNumber = "+91 98401 23456",
                    name = "Kavitha Raman",
                    role = "STAFF",
                    department = "Content & Media",
                    designation = "Senior Reels Editor",
                    staffId = "GP-STAFF-101",
                    avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150",
                    joiningDate = "2024-03-15",
                    isActive = true
                ),
                UserEntity(
                    uid = "admin_001",
                    username = "admin",
                    email = "admin.director@genzpluse.org",
                    phoneNumber = "+91 98765 43210",
                    name = "Dr. Rajesh Sundaram",
                    role = "ADMIN",
                    department = "Executive Management",
                    designation = "Chief Operations Officer",
                    staffId = "GP-ADM-001",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                    joiningDate = "2023-01-01",
                    isActive = true
                )
            )
            users.forEach { db.userDao().insertUser(it) }

            // Seed Tasks
            val tasks = listOf(
                TaskEntity(
                    id = "task_001",
                    title = "Produce Weekly Tech Pulse Reel",
                    description = "Assemble footage, add dynamic typography and audio sync for 60-second Instagram Tech wrap-up.",
                    assignedStaffId = "GP-STAFF-101",
                    assignedStaffName = "Kavitha Raman",
                    assignedToMultipleNamesJson = "[\"Kavitha Raman\"]",
                    priority = "HIGH",
                    status = "IN_PROGRESS",
                    deadline = "Tomorrow, 05:00 PM",
                    targetUnits = 5,
                    completedUnits = 3,
                    progressPercentage = 60,
                    resourcesLink = "https://drive.google.com/genzpluse/assets/tech-reel",
                    adminFeedback = "Excellent pacing on previous cut. Keep sound effects subtle.",
                    staffNotes = "Rough cut exported. Finalizing color grade.",
                    createdAt = "2026-09-03"
                ),
                TaskEntity(
                    id = "task_002",
                    title = "Breaking News Banner Template v2",
                    description = "Update the typography and layout grid for live breaking news ticker templates.",
                    assignedStaffId = "GP-STAFF-101",
                    assignedStaffName = "Kavitha Raman",
                    assignedToMultipleNamesJson = "[\"Kavitha Raman\", \"Arjun Verma\"]",
                    priority = "MEDIUM",
                    status = "NOT_STARTED",
                    deadline = "Friday, 02:00 PM",
                    targetUnits = 2,
                    completedUnits = 0,
                    progressPercentage = 0,
                    resourcesLink = "https://figma.com/file/genzpluse-templates",
                    adminFeedback = "Follow the 2026 brand design token guide.",
                    staffNotes = "",
                    createdAt = "2026-09-04"
                ),
                TaskEntity(
                    id = "task_003",
                    title = "Landscape Infographic: EV Market Trend",
                    description = "Design 16:9 infographic chart displaying electric vehicle sales in Asian markets.",
                    assignedStaffId = "GP-STAFF-101",
                    assignedStaffName = "Kavitha Raman",
                    assignedToMultipleNamesJson = "[\"Kavitha Raman\"]",
                    priority = "LOW",
                    status = "COMPLETED",
                    deadline = "Yesterday, 06:00 PM",
                    targetUnits = 3,
                    completedUnits = 3,
                    progressPercentage = 100,
                    resourcesLink = "https://data.genzpluse.org/reports/ev",
                    adminFeedback = "Approved and published to master feed.",
                    staffNotes = "Uploaded to GenzPluse media bank.",
                    createdAt = "2026-09-01"
                ),
                TaskEntity(
                    id = "task_004",
                    title = "Youth Entrepreneurship Series Reel",
                    description = "Interviews cutdown for youth founder spotlight #StartupStory.",
                    assignedStaffId = "GP-STAFF-102",
                    assignedStaffName = "Arjun Verma",
                    assignedToMultipleNamesJson = "[\"Arjun Verma\"]",
                    priority = "HIGH",
                    status = "IN_PROGRESS",
                    deadline = "Thursday, 12:00 PM",
                    targetUnits = 4,
                    completedUnits = 2,
                    progressPercentage = 50,
                    resourcesLink = "https://assets.genzpluse.org/interviews",
                    adminFeedback = "Ensure subtitle accuracy in Tamil and English.",
                    staffNotes = "Subtitles generated and aligned.",
                    createdAt = "2026-09-02"
                )
            )
            db.taskDao().insertAllTasks(tasks)

            // Seed Attendance
            val attendance = listOf(
                AttendanceEntity(
                    id = "att_001",
                    userId = "staff_001",
                    staffId = "GP-STAFF-101",
                    staffName = "Kavitha Raman",
                    date = "2026-09-04",
                    checkInTime = "08:52 AM",
                    checkOutTime = null,
                    status = "PRESENT",
                    remarks = "On-time biometric verification"
                ),
                AttendanceEntity(
                    id = "att_002",
                    userId = "staff_001",
                    staffId = "GP-STAFF-101",
                    staffName = "Kavitha Raman",
                    date = "2026-09-03",
                    checkInTime = "09:05 AM",
                    checkOutTime = "06:10 PM",
                    status = "PRESENT",
                    remarks = "Regular day"
                ),
                AttendanceEntity(
                    id = "att_003",
                    userId = "staff_001",
                    staffId = "GP-STAFF-101",
                    staffName = "Kavitha Raman",
                    date = "2026-09-02",
                    checkInTime = "08:48 AM",
                    checkOutTime = "06:00 PM",
                    status = "PRESENT",
                    remarks = "Regular day"
                ),
                AttendanceEntity(
                    id = "att_004",
                    userId = "staff_002",
                    staffId = "GP-STAFF-102",
                    staffName = "Arjun Verma",
                    date = "2026-09-04",
                    checkInTime = "09:18 AM",
                    checkOutTime = null,
                    status = "LATE",
                    remarks = "Traffic delay on metro route"
                )
            )
            db.attendanceDao().insertAll(attendance)

            // Seed Targets
            val targets = listOf(
                TargetEntity(
                    id = "tgt_001",
                    staffId = "GP-STAFF-101",
                    staffName = "Kavitha Raman",
                    title = "Monthly Video Production Target",
                    description = "Deliver minimum 80 verified reels and graphics packages for September.",
                    targetValue = 80,
                    completedValue = 60,
                    unit = "Reels & Assets",
                    startDate = "2026-09-01",
                    endDate = "2026-09-30",
                    status = "ACTIVE"
                ),
                TargetEntity(
                    id = "tgt_002",
                    staffId = "GP-STAFF-102",
                    staffName = "Arjun Verma",
                    title = "News Article & Template Target",
                    description = "Publish 100 structured news feeds and editorial infographics.",
                    targetValue = 100,
                    completedValue = 85,
                    unit = "News Pieces",
                    startDate = "2026-09-01",
                    endDate = "2026-09-30",
                    status = "ACTIVE"
                )
            )
            db.targetDao().insertAll(targets)

            // Seed Announcements
            val announcements = listOf(
                AnnouncementEntity(
                    id = "ann_001",
                    title = "Quarterly Productivity Bonus & Review Guidelines",
                    message = "Management is pleased to announce that staff members maintaining >=85% target completion by Sept 25th will be eligible for performance awards.",
                    authorName = "HR Director",
                    publishedAt = "Today, 09:30 AM",
                    priority = "IMPORTANT",
                    targetAudience = "ALL_STAFF",
                    isRead = false,
                    actionUrl = "https://genzpluse.org/policies/bonus"
                ),
                AnnouncementEntity(
                    id = "ann_002",
                    title = "New GenzPluse AI Assisted Reels Suite Available",
                    message = "The content studio has updated all creator workstations with auto-captioning and HD landscape rendering pipelines.",
                    authorName = "Admin Operations",
                    publishedAt = "Yesterday, 04:15 PM",
                    priority = "NORMAL",
                    targetAudience = "CONTENT_CREATORS",
                    isRead = true,
                    actionUrl = ""
                )
            )
            db.announcementDao().insertAll(announcements)

            // Seed Notes
            val notes = listOf(
                NoteEntity(
                    id = "note_001",
                    userId = "GP-STAFF-101",
                    title = "Reel B-Roll Assets & Safe Margins",
                    content = "Remember to keep text overlays within 1080x1350 box so Instagram and TikTok UI buttons don't block titles.",
                    linksJson = "[\"https://brand.genzpluse.org/guide\"]",
                    tagsJson = "[\"Reels\", \"Production\"]",
                    colorTag = 0xFF2563EB,
                    isPinned = true,
                    createdAt = "2026-09-02",
                    updatedAt = "2026-09-02"
                ),
                NoteEntity(
                    id = "note_002",
                    userId = "GP-STAFF-101",
                    title = "Ideas for AI Week Special",
                    content = "1. Generative 3D avatar demo\n2. Interview with student developers\n3. Top 5 open-weight models explained in 45 seconds.",
                    linksJson = "[]",
                    tagsJson = "[\"Ideas\", \"Upcoming\"]",
                    colorTag = 0xFF0891B2,
                    isPinned = false,
                    createdAt = "2026-09-03",
                    updatedAt = "2026-09-04"
                )
            )
            db.noteDao().insertAll(notes)

            // Seed Content
            val contents = listOf(
                ContentEntity(
                    id = "cnt_001",
                    creatorId = "staff_001",
                    creatorStaffId = "GP-STAFF-101",
                    creatorName = "Kavitha Raman",
                    title = "AI in 60 Seconds: Local LLMs on Mobile",
                    category = "REELS",
                    description = "Short-form vertical video showcasing on-device inference speed on modern Snapdragon chipset.",
                    mainText = "Here is what happens when you run modern AI offline on your phone without sending a single byte to the cloud...",
                    mediaUrl = "https://video.genzpluse.org/reels/ai-mobile.mp4",
                    referenceLinksJson = "[\"https://ai.google.dev\", \"https://huggingface.co\"]",
                    tagsJson = "[\"AI\", \"TechReels\", \"GenzTech\"]",
                    status = "APPROVED",
                    adminReviewNotes = "Great visual hook in the first 3 seconds.",
                    createdAt = "2026-09-03 14:20",
                    updatedAt = "2026-09-03 16:00"
                ),
                ContentEntity(
                    id = "cnt_002",
                    creatorId = "staff_001",
                    creatorStaffId = "GP-STAFF-101",
                    creatorName = "Kavitha Raman",
                    title = "Breaking: Global Space Startup Funding Surge",
                    category = "NEWS_TEMPLATE",
                    description = "Clean card format with bullet metrics and headline banner.",
                    mainText = "Space tech investments have crossed $12B in Q3 2026, led by satellite propulsion and lunar logistics startups.",
                    mediaUrl = "https://cdn.genzpluse.org/templates/space-news.png",
                    referenceLinksJson = "[\"https://spacenews.com/q3-report\"]",
                    tagsJson = "[\"News\", \"SpaceTech\", \"Editorial\"]",
                    status = "SUBMITTED",
                    adminReviewNotes = "",
                    createdAt = "2026-09-04 10:15",
                    updatedAt = "2026-09-04 10:15"
                ),
                ContentEntity(
                    id = "cnt_003",
                    creatorId = "staff_001",
                    creatorStaffId = "GP-STAFF-101",
                    creatorName = "Kavitha Raman",
                    title = "Renewable Energy Grid 2026 Overview",
                    category = "LANDSCAPE",
                    description = "Full 16:9 widescreen animated infographic for studio video podcast backdrop.",
                    mainText = "Interactive breakdown of solar, wind and battery storage installations across India.",
                    mediaUrl = "https://cdn.genzpluse.org/landscape/grid-2026.png",
                    referenceLinksJson = "[\"https://energy.gov.in/stats\"]",
                    tagsJson = "[\"Landscape\", \"Design\", \"Infographic\"]",
                    status = "APPROVED",
                    adminReviewNotes = "High visual fidelity.",
                    createdAt = "2026-09-02 11:00",
                    updatedAt = "2026-09-02 15:30"
                )
            )
            db.contentDao().insertAll(contents)

            // Seed Requests
            val leaveRequests = listOf(
                LeaveRequestEntity(
                    id = "lvr_001",
                    staffId = "GP-STAFF-101",
                    staffName = "Kavitha Raman",
                    department = "Content & Media",
                    leaveType = "CASUAL",
                    fromDate = "2026-09-18",
                    toDate = "2026-09-19",
                    numberOfDays = 2,
                    reason = "Family function in native town.",
                    attachmentUrl = null,
                    status = "PENDING",
                    adminResponse = null,
                    submittedAt = "2026-09-04 09:10 AM"
                )
            )
            db.requestDao().insertAllLeaveRequests(leaveRequests)

            val problemReports = listOf(
                ProblemReportEntity(
                    id = "rep_001",
                    staffId = "GP-STAFF-101",
                    staffName = "Kavitha Raman",
                    department = "Content & Media",
                    category = "HARDWARE_EQUIPMENT",
                    title = "4K Rendering GPU Fan Noise and Throttling",
                    description = "Workstation #4 GPU temperature exceeds 88C during batch 4K reel export. Needs thermal paste replacement.",
                    priority = "HIGH",
                    status = "UNDER_REVIEW",
                    adminNotes = "IT department ticket #IT-409 assigned to hardware engineer.",
                    submittedAt = "2026-09-03 03:30 PM"
                )
            )
            db.requestDao().insertAllProblemReports(problemReports)

            // Seed Notifications
            val notifications = listOf(
                NotificationEntity(
                    id = "notif_001",
                    targetUserId = "GP-STAFF-101",
                    title = "New Task Assigned",
                    message = "Admin assigned 'Produce Weekly Tech Pulse Reel' with High priority.",
                    type = "TASK_ASSIGNED",
                    timestamp = "1 hour ago",
                    isRead = false,
                    actionDeepLink = "tasks"
                ),
                NotificationEntity(
                    id = "notif_002",
                    targetUserId = "GP-STAFF-101",
                    title = "Content Approved: AI in 60 Seconds",
                    message = "Your Reel submission has been verified and approved by the Editorial desk.",
                    type = "CONTENT_APPROVED",
                    timestamp = "Yesterday",
                    isRead = true,
                    actionDeepLink = "genzpluse"
                )
            )
            db.notificationDao().insertAll(notifications)
        }
    }
}
