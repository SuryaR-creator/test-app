package com.example.data.fcm

import android.util.Log
import com.example.GenzPluseApplication
import com.example.data.local.entity.NotificationEntity
import com.example.domain.model.AppNotification
import com.example.domain.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GenzPluseFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = application as? GenzPluseApplication ?: return
        val tokenManager = app.container.fcmTokenManager

        tokenManager.saveCachedToken(token)

        val currentUserUid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (!currentUserUid.isNullOrBlank()) {
            serviceScope.launch {
                tokenManager.registerDeviceToken(currentUserUid, token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notification = parseRemoteMessage(remoteMessage) ?: return

        val app = application as? GenzPluseApplication
        if (app != null) {
            serviceScope.launch {
                try {
                    // Update in-app notifications Room cache
                    app.container.db.notificationDao().insertNotification(
                        NotificationEntity(
                            id = notification.id,
                            targetUserId = notification.targetUserId,
                            title = notification.title,
                            message = notification.message,
                            type = notification.type.name,
                            timestamp = notification.timestamp,
                            isRead = false,
                            actionDeepLink = notification.actionDeepLink
                        )
                    )
                } catch (e: Exception) {
                    Log.w("GenzPluseFCM", "Failed to cache incoming notification in Room: ${e.message}")
                }
            }
        }

        // Display system notification
        NotificationDisplayHelper.displayNotification(this, notification)
    }

    companion object {
        fun parseRemoteMessage(remoteMessage: RemoteMessage): AppNotification? {
            return try {
                val data = remoteMessage.data
                val notif = remoteMessage.notification

                val id = data["id"]?.takeIf { it.isNotBlank() }
                    ?: remoteMessage.messageId?.takeIf { it.isNotBlank() }
                    ?: "notif_${UUID.randomUUID().toString().take(8)}"

                val title = data["title"]?.takeIf { it.isNotBlank() }
                    ?: notif?.title?.takeIf { it.isNotBlank() }
                    ?: "GenzPluse Update"

                val message = data["message"]?.takeIf { it.isNotBlank() }
                    ?: data["body"]?.takeIf { it.isNotBlank() }
                    ?: notif?.body?.takeIf { it.isNotBlank() }
                    ?: ""

                val typeStr = data["type"] ?: "ANNOUNCEMENT"
                val type = runCatching { NotificationType.valueOf(typeStr) }.getOrDefault(NotificationType.ANNOUNCEMENT)

                val targetUserId = data["targetUserId"]?.takeIf { it.isNotBlank() } ?: "ALL"
                val actionDeepLink = data["actionDeepLink"] ?: ""
                val timestamp = data["timestamp"]?.takeIf { it.isNotBlank() }
                    ?: SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

                AppNotification(
                    id = id,
                    targetUserId = targetUserId,
                    title = title,
                    message = message,
                    type = type,
                    timestamp = timestamp,
                    isRead = false,
                    actionDeepLink = actionDeepLink
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
