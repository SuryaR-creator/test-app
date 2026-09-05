package com.example.security

import com.example.domain.model.UserRole
import com.example.domain.model.UserSession

object RoleAccessPolicy {
    fun canAccessAdminConsole(session: UserSession?): Boolean {
        return session != null && session.role == UserRole.ADMIN && session.isActive
    }

    fun canAccessStaffConsole(session: UserSession?): Boolean {
        return session != null && session.role == UserRole.STAFF && session.isActive
    }

    fun isFieldEditableByStaff(fieldName: String): Boolean {
        return when (fieldName.lowercase()) {
            "bio", "emergencycontact", "bloodgroup", "address", "phonenumber", "avatarurl" -> true
            "staffid", "name", "email", "department", "designation", "joiningdate", "role", "assignedtarget", "completedtarget", "isactive", "uid" -> false
            else -> false
        }
    }
}

class SecurityManager {
    fun sanitizeInput(input: String): String {
        return input.trim()
    }

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()
    }

    fun validatePasswordStrength(password: String): Boolean {
        return password.length >= 6
    }
}
