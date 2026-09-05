package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AnnouncementPriority
import com.example.domain.model.TargetAudience
import com.example.domain.model.UserRole
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

sealed class AdminTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Dashboard : AdminTab("Overview", Icons.Default.Dashboard)
    data object Staff : AdminTab("Staff", Icons.Default.Groups)
    data object Tasks : AdminTab("Tasks", Icons.Default.Assignment)
    data object Content : AdminTab("Editorial", Icons.Default.VideoLibrary)
    data object Requests : AdminTab("Helpdesk", Icons.Default.ReportProblem)
}

@Composable
fun AdminMainScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf<AdminTab>(AdminTab.Dashboard) }

    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                session = uiState.session,
                unreadNotificationsCount = 0,
                onNotificationClick = { showBroadcastDialog = true },
                onProfileClick = { },
                onRoleSwitchClick = onSwitchRole
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    AdminTab.Dashboard,
                    AdminTab.Staff,
                    AdminTab.Tasks,
                    AdminTab.Content,
                    AdminTab.Requests
                )

                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandBluePrimary,
                            selectedTextColor = BrandBluePrimary,
                            indicatorColor = BrandBluePrimary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate50,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AdminTab.Dashboard -> AdminDashboardScreen(
                    uiState = uiState,
                    onNavigateToStaff = { currentTab = AdminTab.Staff },
                    onNavigateToTasks = { currentTab = AdminTab.Tasks },
                    onNavigateToContent = { currentTab = AdminTab.Content },
                    onNavigateToRequests = { currentTab = AdminTab.Requests },
                    onOpenAddStaffDialog = { currentTab = AdminTab.Staff },
                    onOpenAssignTaskDialog = { currentTab = AdminTab.Tasks },
                    onOpenBroadcastDialog = { showBroadcastDialog = true }
                )
                AdminTab.Staff -> AdminStaffManagementScreen(
                    uiState = uiState,
                    onAddStaff = { staffId, name, uname, email, phone, dept, desig, target ->
                        viewModel.addNewStaff(staffId, name, uname, email, phone, dept, desig, target)
                    },
                    onToggleActive = { staffId, currentActive ->
                        viewModel.toggleStaffActive(staffId, currentActive)
                    }
                )
                AdminTab.Tasks -> AdminTaskManagementScreen(
                    uiState = uiState,
                    onAssignTask = { title, desc, staffId, staffName, prio, deadline, targetUnits, link ->
                        viewModel.assignTask(title, desc, staffId, staffName, prio, deadline, targetUnits, link)
                    },
                    onAddFeedback = { taskId, feedback ->
                        viewModel.addAdminTaskFeedback(taskId, feedback)
                    }
                )
                AdminTab.Content -> AdminContentReviewScreen(
                    uiState = uiState,
                    onReviewContent = { id, status, notes ->
                        viewModel.reviewContent(id, status, notes)
                    }
                )
                AdminTab.Requests -> AdminRequestsReviewScreen(
                    uiState = uiState,
                    onUpdateLeaveStatus = { id, status, response ->
                        viewModel.updateLeaveStatus(id, status, response)
                    },
                    onUpdateProblemStatus = { id, status, notes ->
                        viewModel.updateProblemStatus(id, status, notes)
                    }
                )
            }
        }
    }

    // Broadcast Announcement Dialog
    if (showBroadcastDialog) {
        var title by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(AnnouncementPriority.IMPORTANT) }

        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Broadcast Org Announcement", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Priority Level", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AnnouncementPriority.entries.forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Headline *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Detailed Message Body *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && message.isNotBlank()) {
                            viewModel.broadcastAnnouncement(title, message, priority, TargetAudience.ALL_STAFF, "")
                            showBroadcastDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && message.isNotBlank()
                ) {
                    Text("Broadcast to All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) { Text("Cancel") }
            }
        )
    }
}
