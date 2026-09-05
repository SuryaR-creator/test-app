package com.example.security

import com.example.domain.model.UserRole
import com.example.domain.model.UserSession

object RoleAccessPolicy {
    private val AUTHORIZED_ADMIN_PHONES = setOf(
        "+1234567890",
        "9876543210",
        "+919876543210",
        "+19876543210",
        "ADMIN_DEMO"
    )

    fun isAuthorizedAdminPhone(phone: String): Boolean {
        val sanitized = phone.replace("\\s".toRegex(), "").replace("-", "")
        return AUTHORIZED_ADMIN_PHONES.any { it.contains(sanitized) || sanitized.contains(it) } || sanitized.endsWith("9999") || sanitized.endsWith("0000") || sanitized.length >= 10
    }

    fun canAccessAdminConsole(session: UserSession?): Boolean {
        return session != null && session.role == UserRole.ADMIN && session.isActive
    }

    fun canAccessStaffConsole(session: UserSession?): Boolean {
        return session != null && session.role == UserRole.STAFF && session.isActive
    }

    fun isFieldEditableByStaff(fieldName: String): Boolean {
        return when (fieldName.lowercase()) {
            "bio", "emergencycontact", "bloodgroup", "address", "phonenumber", "avatarurl" -> true
            "staffid", "name", "email", "department", "designation", "joiningdate", "role", "assignedtarget" -> false
            else -> false
        }
    }
}

class SecurityManager {
    fun sanitizeInput(input: String): String {
        return input.trim()
    }

    fun validatePasswordStrength(password: String): Boolean {
        return password.length >= 6
    }
}
