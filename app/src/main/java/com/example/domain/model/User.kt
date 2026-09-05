package com.example.domain.model

enum class UserRole {
    STAFF,
    ADMIN
}

data class UserSession(
    val uid: String,
    val username: String,
    val email: String,
    val phoneNumber: String = "",
    val name: String,
    val role: UserRole,
    val department: String = "",
    val designation: String = "",
    val staffId: String = "",
    val avatarUrl: String = "",
    val joiningDate: String = "",
    val isActive: Boolean = true
)

data class StaffProfile(
    val uid: String,
    val staffId: String,
    val name: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val department: String,
    val designation: String,
    val joiningDate: String,
    val avatarUrl: String = "",
    val bio: String = "",
    val emergencyContact: String = "",
    val bloodGroup: String = "",
    val address: String = "",
    val isActive: Boolean = true,
    val assignedTarget: Int = 100,
    val completedTarget: Int = 0
)

data class AdminProfile(
    val uid: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val roleTitle: String = "Super Administrator",
    val accessLevel: String = "LEVEL_1"
)
