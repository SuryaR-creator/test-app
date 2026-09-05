package com.example.data.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.domain.model.NotificationType

object NotificationChannelHelper {

    const val CHANNEL_TASKS = "genzpluse_tasks_channel"
    const val CHANNEL_ANNOUNCEMENTS = "genzpluse_announcements_channel"
    const val CHANNEL_REQUESTS = "genzpluse_requests_channel"
    const val CHANNEL_CONTENT = "genzpluse_content_channel"
    const val CHANNEL_GENERAL = "genzpluse_general_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TASKS,
                    "Task Updates & Assignments",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for newly assigned tasks, deadlines, and task feedback"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_ANNOUNCEMENTS,
                    "Company Announcements & Broadcasts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Official executive announcements, policy updates, and emergency broadcasts"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_REQUESTS,
                    "Leave & Problem Report Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Status updates for submitted leave requests and internal problem reports"
                },
                NotificationChannel(
                    CHANNEL_CONTENT,
                    "Content Pipeline & Review",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates on GenzPluse content drafts, review notes, and publication approvals"
                },
                NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General system alerts and operational notifications"
                }
            )

            notificationManager.createNotificationChannels(channels)
        }
    }

    fun getChannelIdForType(type: NotificationType?): String {
        return when (type) {
            NotificationType.TASK_ASSIGNED,
            NotificationType.TASK_FEEDBACK,
            NotificationType.TARGET_UPDATE,
            NotificationType.ACHIEVEMENT_UNLOCKED -> CHANNEL_TASKS

            NotificationType.ANNOUNCEMENT -> CHANNEL_ANNOUNCEMENTS

            NotificationType.LEAVE_STATUS,
            NotificationType.PROBLEM_STATUS -> CHANNEL_REQUESTS

            NotificationType.CONTENT_APPROVED -> CHANNEL_CONTENT

            null -> CHANNEL_GENERAL
        }
    }
}
