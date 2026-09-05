package com.example.data.fcm

import android.content.Context
import android.content.SharedPreferences
import com.example.data.repository.awaitTask
import com.example.data.util.ErrorMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

interface FcmTokenManager {
    suspend fun registerDeviceToken(uid: String, token: String): Result<Boolean>
    suspend fun unregisterDeviceToken(uid: String): Result<Boolean>
    fun getDeviceId(): String
    fun getCachedToken(): String?
    fun saveCachedToken(token: String)
    fun clearCachedToken()
}

class FcmTokenManagerImpl(
    private val context: Context,
    private val firestoreProvider: () -> FirebaseFirestore? = {
        runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    },
    private val authProvider: () -> FirebaseAuth? = {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
) : FcmTokenManager {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("genzpluse_fcm_prefs", Context.MODE_PRIVATE)
    }

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun getDeviceId(): String {
        var deviceId = prefs.getString("device_unique_id", null)
        if (deviceId.isNullOrEmpty()) {
            deviceId = "dev_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            prefs.edit().putString("device_unique_id", deviceId).apply()
        }
        return deviceId
    }

    override fun getCachedToken(): String? {
        return prefs.getString("cached_fcm_token", null)
    }

    override fun saveCachedToken(token: String) {
        if (token.isNotBlank()) {
            prefs.edit().putString("cached_fcm_token", token).apply()
        }
    }

    override fun clearCachedToken() {
        prefs.edit().remove("cached_fcm_token").apply()
    }

    override suspend fun registerDeviceToken(uid: String, token: String): Result<Boolean> {
        if (uid.isBlank() || token.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID and FCM token must not be blank"))
        }

        val auth = authProvider()
        val currentUid = auth?.currentUser?.uid
        // Ensure the token registration cannot be forged for arbitrary foreign users
        if (currentUid != null && currentUid != uid) {
            return Result.failure(
                SecurityException("Security violation: Cannot register FCM device token for another user")
            )
        }

        saveCachedToken(token)

        val firestore = firestoreProvider() ?: return Result.success(true) // Offline fallback
        return try {
            val deviceId = getDeviceId()
            val deviceData = mapOf(
                "deviceId" to deviceId,
                "fcmToken" to token,
                "platform" to "android",
                "appVersion" to "1.0",
                "lastUpdated" to isoDateFormat.format(Date())
            )

            firestore.collection("users")
                .document(uid)
                .collection("devices")
                .document(deviceId)
                .set(deviceData, SetOptions.merge())
                .awaitTask()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ErrorMapper.mapException(e))
        }
    }

    override suspend fun unregisterDeviceToken(uid: String): Result<Boolean> {
        if (uid.isBlank()) return Result.success(true)

        val deviceId = getDeviceId()
        clearCachedToken()

        val firestore = firestoreProvider() ?: return Result.success(true)
        return try {
            firestore.collection("users")
                .document(uid)
                .collection("devices")
                .document(deviceId)
                .delete()
                .awaitTask()

            Result.success(true)
        } catch (e: Exception) {
            // Unregister errors during logout should not crash the app, but return failure result
            Result.failure(ErrorMapper.mapException(e))
        }
    }
}
