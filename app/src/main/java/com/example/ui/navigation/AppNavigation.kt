package com.example.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.di.AppContainer
import com.example.domain.model.UserRole
import com.example.ui.admin.AdminMainScreen
import com.example.ui.admin.AdminViewModel
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.staff.StaffMainScreen
import com.example.ui.staff.StaffViewModel

object AppRoutes {
    const val AUTH = "auth"
    const val STAFF = "staff"
    const val ADMIN = "admin"
}

@Composable
fun AppNavigation(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(container.authRepository)
    )

    val currentSession by authViewModel.currentSession.collectAsState(initial = null)

    val startDestination = remember(currentSession) {
        when (currentSession?.role) {
            UserRole.ADMIN -> AppRoutes.ADMIN
            UserRole.STAFF -> AppRoutes.STAFF
            null -> AppRoutes.AUTH
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoutes.AUTH) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToStaff = {
                    navController.navigate(AppRoutes.STAFF) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(AppRoutes.ADMIN) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.STAFF) {
            val staffViewModel: StaffViewModel = viewModel(
                factory = StaffViewModel.Factory(
                    authRepository = container.authRepository,
                    staffRepository = container.staffRepository,
                    taskRepository = container.taskRepository,
                    attendanceRepository = container.attendanceRepository,
                    targetRepository = container.targetRepository,
                    contentRepository = container.contentRepository,
                    announcementRepository = container.announcementRepository,
                    noteRepository = container.noteRepository,
                    requestRepository = container.requestRepository,
                    notificationRepository = container.notificationRepository
                )
            )

            StaffMainScreen(
                viewModel = staffViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppRoutes.AUTH) {
                        popUpTo(AppRoutes.STAFF) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.ADMIN) {
            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModel.Factory(
                    authRepository = container.authRepository,
                    staffRepository = container.staffRepository,
                    taskRepository = container.taskRepository,
                    attendanceRepository = container.attendanceRepository,
                    targetRepository = container.targetRepository,
                    contentRepository = container.contentRepository,
                    announcementRepository = container.announcementRepository,
                    requestRepository = container.requestRepository,
                    notificationRepository = container.notificationRepository
                )
            )

            AdminMainScreen(
                viewModel = adminViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppRoutes.AUTH) {
                        popUpTo(AppRoutes.ADMIN) { inclusive = true }
                    }
                }
            )
        }
    }
}
