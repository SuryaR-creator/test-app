package com.example.data.fcm

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.AppNotification

object NotificationDisplayHelper {

    const val EXTRA_NOTIFICATION_ID = "com.example.genzpluse.NOTIFICATION_ID"
    const val EXTRA_NOTIFICATION_TYPE = "com.example.genzpluse.NOTIFICATION_TYPE"
    const val EXTRA_ACTION_DEEP_LINK = "com.example.genzpluse.ACTION_DEEP_LINK"
    const val EXTRA_TARGET_USER_ID = "com.example.genzpluse.TARGET_USER_ID"

    fun displayNotification(context: Context, notification: AppNotification) {
        // Android 13+ runtime notification permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted; skip system notification gracefully without crash
                return
            }
        }

        // Ensure channels are created
        NotificationChannelHelper.createNotificationChannels(context)

        val channelId = NotificationChannelHelper.getChannelIdForType(notification.type)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_ID, notification.id)
            putExtra(EXTRA_NOTIFICATION_TYPE, notification.type.name)
            putExtra(EXTRA_ACTION_DEEP_LINK, notification.actionDeepLink)
            putExtra(EXTRA_TARGET_USER_ID, notification.targetUserId)
        }

        val requestCode = notification.id.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val safeTitle = notification.title.trim().take(100)
        val safeMessage = notification.message.trim().take(500)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(safeTitle)
            .setContentText(safeMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(safeMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(requestCode, builder.build())
        } catch (_: SecurityException) {
            // Gracefully ignore notification dispatch failures
        }
    }
}
