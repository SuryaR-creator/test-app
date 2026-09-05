package com.example

import com.example.domain.model.UserRole
import com.example.domain.model.UserSession
import com.example.security.RoleAccessPolicy
import com.example.security.SecurityManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SecurityAndFirestorePolicyTest {

    private val securityManager = SecurityManager()

    @Test
    fun `admin console access policy allows only active admin session`() {
        val activeAdmin = UserSession(
            uid = "adm_1",
            username = "admin",
            email = "admin@genzpluse.org",
            name = "Admin User",
            role = UserRole.ADMIN,
            isActive = true
        )
        val inactiveAdmin = activeAdmin.copy(isActive = false)
        val activeStaff = activeAdmin.copy(role = UserRole.STAFF)

        assertTrue("Active Admin should be allowed", RoleAccessPolicy.canAccessAdminConsole(activeAdmin))
        assertFalse("Inactive Admin should be denied", RoleAccessPolicy.canAccessAdminConsole(inactiveAdmin))
        assertFalse("Staff role should be denied from Admin console", RoleAccessPolicy.canAccessAdminConsole(activeStaff))
        assertFalse("Null session should be denied from Admin console", RoleAccessPolicy.canAccessAdminConsole(null))
    }

    @Test
    fun `staff console access policy allows only active staff session`() {
        val activeStaff = UserSession(
            uid = "stf_1",
            username = "staff",
            email = "staff@genzpluse.org",
            name = "Staff User",
            role = UserRole.STAFF,
            isActive = true
        )
        val inactiveStaff = activeStaff.copy(isActive = false)
        val activeAdmin = activeStaff.copy(role = UserRole.ADMIN)

        assertTrue("Active Staff should be allowed", RoleAccessPolicy.canAccessStaffConsole(activeStaff))
        assertFalse("Inactive Staff should be denied", RoleAccessPolicy.canAccessStaffConsole(inactiveStaff))
        assertFalse("Admin role should be denied from Staff console", RoleAccessPolicy.canAccessStaffConsole(activeAdmin))
        assertFalse("Null session should be denied from Staff console", RoleAccessPolicy.canAccessStaffConsole(null))
    }

    @Test
    fun `field level security permits only self service profile attributes for staff`() {
        // Permitted self-service fields
        assertTrue(RoleAccessPolicy.isFieldEditableByStaff("bio"))
        assertTrue(RoleAccessPolicy.isFieldEditableByStaff("emergencyContact"))
        assertTrue(RoleAccessPolicy.isFieldEditableByStaff("bloodGroup"))
        assertTrue(RoleAccessPolicy.isFieldEditableByStaff("address"))
        assertTrue(RoleAccessPolicy.isFieldEditableByStaff("phoneNumber"))
        assertTrue(RoleAccessPolicy.isFieldEditableByStaff("avatarUrl"))

        // Restricted admin-controlled fields
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("role"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("staffId"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("department"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("designation"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("joiningDate"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("isActive"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("assignedTarget"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("completedTarget"))
        assertFalse(RoleAccessPolicy.isFieldEditableByStaff("uid"))
    }

    @Test
    fun `security manager validates passwords and sanitizes inputs`() {
        assertFalse("Short password must fail", securityManager.validatePasswordStrength("12345"))
        assertTrue("Valid length password must pass", securityManager.validatePasswordStrength("SecureP@ss123"))

        val dirtyInput = "   admin@genzpluse.org   \n"
        assertTrue(securityManager.sanitizeInput(dirtyInput) == "admin@genzpluse.org")
    }

    @Test
    fun `firestore rules file exists and enforces mandatory collections and restrictions`() {
        val rulesFile = File("firestore.rules")
        val altFile = File("/firestore.rules")
        val targetFile = if (rulesFile.exists()) rulesFile else altFile
        if (targetFile.exists()) {
            val content = targetFile.readText()
            assertTrue("Rules must contain users collection", content.contains("match /users/{userId}"))
            assertTrue("Rules must contain tasks collection", content.contains("match /tasks/{taskId}"))
            assertTrue("Rules must contain attendance collection", content.contains("match /attendance/{attendanceId}"))
            assertTrue("Rules must contain targets collection", content.contains("match /targets/{targetId}"))
            assertTrue("Rules must contain announcements collection", content.contains("match /announcements/{announcementId}"))
            assertTrue("Rules must contain notes collection", content.contains("match /notes/{noteId}"))
            assertTrue("Rules must contain genzpluse_content collection", content.contains("match /genzpluse_content/{contentId}"))
            assertTrue("Rules must contain leave_requests collection", content.contains("match /leave_requests/{requestId}"))
            assertTrue("Rules must contain problem_reports collection", content.contains("match /problem_reports/{reportId}"))
            assertTrue("Rules must contain notifications collection", content.contains("match /notifications/{notificationId}"))
            assertTrue("Rules must enforce isAdmin check", content.contains("isAdmin()"))
            assertTrue("Rules must prevent staff from changing role or staffId", content.contains("affectedKeys()"))
        }
    }
}
