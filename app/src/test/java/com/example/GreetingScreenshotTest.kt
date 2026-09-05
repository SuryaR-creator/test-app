package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.UserRole
import com.example.domain.model.UserSession
import com.example.ui.components.AppHeader
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun appHeader_screenshot() {
    val sampleSession = UserSession(
      uid = "staff_001",
      username = "kavitha",
      email = "kavitha.raman@genzpluse.org",
      phoneNumber = "+91 98401 23456",
      name = "Kavitha Raman",
      role = UserRole.STAFF,
      department = "Content & Media",
      designation = "Senior Reels Editor",
      staffId = "GP-STAFF-101",
      avatarUrl = "",
      joiningDate = "2024-03-15",
      isActive = true
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        AppHeader(
          session = sampleSession,
          unreadNotificationsCount = 2,
          onNotificationClick = {},
          onProfileClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/app_header.png")
  }
}
