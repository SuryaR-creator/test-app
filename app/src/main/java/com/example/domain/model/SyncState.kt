package com.example.domain.model

enum class SyncState {
    ONLINE,
    OFFLINE,
    SYNCING,
    SYNC_SUCCESS,
    SYNC_ERROR
}

data class SyncResult(
    val state: SyncState,
    val itemsSynced: Int = 0,
    val errorMessage: String? = null,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

enum class ConflictStrategy {
    SERVER_AUTHORITATIVE,
    CLIENT_AUTHORITATIVE,
    MERGE_PREFER_SERVER,
    MERGE_PREFER_CLIENT
}
