package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.domain.model.AppNotification
import com.example.domain.model.TaskItem
import com.example.domain.model.TaskStatus
import com.example.domain.model.UserRole
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

sealed class StaffTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home : StaffTab("Home", Icons.Default.Home)
    data object Gid : StaffTab("GID", Icons.Default.Hub)
    data object GenzPluse : StaffTab("GenzPluse", Icons.Default.VideoLibrary)
    data object Achievements : StaffTab("Achievements", Icons.Default.EmojiEvents)
    data object Profile : StaffTab("Profile", Icons.Default.Person)
}

@Composable
fun StaffMainScreen(
    viewModel: StaffViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf<StaffTab>(StaffTab.Home) }

    var taskToUpdate by remember { mutableStateOf<TaskItem?>(null) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showNotesScreen by remember { mutableStateOf(false) }
    var showRequestsScreen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    if (showNotesScreen) {
        StaffNotesScreen(
            uiState = uiState,
            onSaveNote = { title, content, color, pinned, id ->
                viewModel.saveNote(title, content, color, pinned, id)
            },
            onDeleteNote = { viewModel.deleteNote(it) },
            onBack = { showNotesScreen = false }
        )
        return
    }

    Scaffold(
        topBar = {
            AppHeader(
                session = uiState.session,
                unreadNotificationsCount = uiState.notifications.count { !it.isRead },
                onNotificationClick = { showNotificationsDialog = true },
                onProfileClick = { currentTab = StaffTab.Profile }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    StaffTab.Home,
                    StaffTab.Gid,
                    StaffTab.GenzPluse,
                    StaffTab.Achievements,
                    StaffTab.Profile
                )

                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = {
                            currentTab = tab
                            showRequestsScreen = false
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
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
            if (showRequestsScreen) {
                StaffRequestsScreen(
                    uiState = uiState,
                    onSubmitLeave = { type, from, to, days, reason ->
                        viewModel.submitLeaveRequest(type, from, to, days, reason)
                    },
                    onSubmitProblem = { cat, title, desc, prio ->
                        viewModel.submitProblemReport(cat, title, desc, prio)
                    }
                )
            } else {
                when (currentTab) {
                    StaffTab.Home -> StaffHomeDashboard(
                        uiState = uiState,
                        onCheckInClick = { viewModel.checkInToday() },
                        onUpdateTaskClick = { taskToUpdate = it },
                        onNavigateToTasks = { /* Already on dashboard list */ },
                        onNavigateToAchievements = { currentTab = StaffTab.Achievements },
                        onNavigateToRequests = { showRequestsScreen = true }
                    )
                    StaffTab.Gid -> StaffGidScreen(
                        uiState = uiState,
                        onAnnouncementClick = { viewModel.markAnnouncementRead(it.id) },
                        onNavigateToNotes = { showNotesScreen = true }
                    )
                    StaffTab.GenzPluse -> StaffGenzPluseScreen(
                        uiState = uiState,
                        onCreateContent = { title, cat, desc, text, links, tags ->
                            viewModel.createGenzPluseContent(title, cat, desc, text, links, tags)
                        }
                    )
                    StaffTab.Achievements -> StaffAchievementsScreen(
                        uiState = uiState
                    )
                    StaffTab.Profile -> StaffProfileScreen(
                        uiState = uiState,
                        onUpdateSelfProfile = { bio, emergency, blood, address, phone ->
                            viewModel.updateSelfProfile(bio, emergency, blood, address, phone)
                        },
                        onLogout = onLogout,
                        onOpenHelpdesk = { showRequestsScreen = true }
                    )
                }
            }
        }
    }

    // Task Update Dialog
    taskToUpdate?.let { task ->
        var selectedStatus by remember { mutableStateOf(task.status) }
        var progressSlider by remember { mutableFloatStateOf(task.progressPercentage.toFloat()) }
        var staffNotes by remember { mutableStateOf(task.staffNotes) }

        AlertDialog(
            onDismissRequest = { taskToUpdate = null },
            title = {
                Text(
                    text = "Update Task Progress",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate900,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate700
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(TaskStatus.NOT_STARTED, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED).forEach { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = {
                                    selectedStatus = status
                                    if (status == TaskStatus.COMPLETED) progressSlider = 100f
                                    if (status == TaskStatus.NOT_STARTED) progressSlider = 0f
                                },
                                label = { Text(status.name.replace("_", " "), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text(
                        text = "Progress: ${progressSlider.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate700
                    )

                    Slider(
                        value = progressSlider,
                        onValueChange = {
                            progressSlider = it
                            if (it >= 100f) selectedStatus = TaskStatus.COMPLETED
                            else if (it > 0f) selectedStatus = TaskStatus.IN_PROGRESS
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = BrandBluePrimary,
                            activeTrackColor = BrandBluePrimary
                        )
                    )

                    OutlinedTextField(
                        value = staffNotes,
                        onValueChange = { staffNotes = it },
                        label = { Text("Staff Work Notes / Updates") },
                        placeholder = { Text("e.g. Exported cut, uploaded to drive...") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTaskProgress(
                            taskId = task.id,
                            status = selectedStatus,
                            progress = progressSlider.toInt(),
                            notes = staffNotes
                        )
                        taskToUpdate = null
                    }
                ) {
                    Text("Save Progress")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToUpdate = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Notifications", fontWeight = FontWeight.Bold)
                    Surface(
                        color = StatusInfoContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${uiState.notifications.size} Total",
                            color = BrandBluePrimary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                if (uiState.notifications.isEmpty()) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        Text("No new notifications.", color = Slate500)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.notifications) { notif ->
                            Surface(
                                color = if (notif.isRead) Slate50 else Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (notif.isRead) Slate400 else BrandBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Slate900
                                        )
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate600
                                        )
                                        Text(
                                            text = notif.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
