package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.domain.model.TaskItem
import com.example.domain.model.TaskPriority
import com.example.domain.model.TaskStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminTaskManagementScreen(
    uiState: AdminUiState,
    onAssignTask: (title: String, desc: String, staffId: String, staffName: String, priority: TaskPriority, deadline: String, targetUnits: Int, link: String) -> Unit,
    onAddFeedback: (taskId: String, feedback: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var taskToFeedback by remember { mutableStateOf<TaskItem?>(null) }

    val filteredTasks = remember(uiState.allTasks, selectedFilter) {
        if (selectedFilter == null) {
            uiState.allTasks
        } else {
            uiState.allTasks.filter { it.status == selectedFilter }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAssignDialog = true },
                containerColor = BrandBluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.AddTask, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Assign Task", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Slate50,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Task Assignments & Tracking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${uiState.allTasks.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == TaskStatus.IN_PROGRESS,
                    onClick = { selectedFilter = TaskStatus.IN_PROGRESS },
                    label = { Text("In Progress") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == TaskStatus.COMPLETED,
                    onClick = { selectedFilter = TaskStatus.COMPLETED },
                    label = { Text("Completed") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTasks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.AssignmentTurnedIn,
                    title = "No Tasks in this Filter",
                    description = "Assign new tasks to staff members with deadlines, resources, and target milestones."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks) { task ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = when (task.priority) {
                                            TaskPriority.CRITICAL, TaskPriority.HIGH -> StatusErrorContainer
                                            TaskPriority.MEDIUM -> StatusWarningContainer
                                            TaskPriority.LOW -> StatusInfoContainer
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${task.priority.name} PRIORITY",
                                            color = when (task.priority) {
                                                TaskPriority.CRITICAL, TaskPriority.HIGH -> StatusError
                                                TaskPriority.MEDIUM -> Color(0xFFD97706)
                                                TaskPriority.LOW -> StatusInfo
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    StatusBadge(status = task.status.name)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )

                                if (task.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Assigned to: ${task.assignedStaffName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBluePrimary
                                    )
                                    Text(
                                        text = "Due: ${task.deadline}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { task.progressPercentage / 100f },
                                    trackColor = Slate200,
                                    color = if (task.progressPercentage == 100) StatusSuccess else BrandBluePrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )

                                if (task.staffNotes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Slate100,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Staff Note: ${task.staffNotes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate800,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                if (task.adminFeedback.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = StatusSuccessContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Admin Feedback: ${task.adminFeedback}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StatusSuccess,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { taskToFeedback = task },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Feedback", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Assign Task Dialog
    if (showAssignDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedStaff by remember { mutableStateOf(uiState.staffList.firstOrNull()) }
        var priority by remember { mutableStateOf(TaskPriority.HIGH) }
        var deadline by remember { mutableStateOf("Tomorrow, 05:00 PM") }
        var targetUnits by remember { mutableIntStateOf(5) }
        var resourcesLink by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign Work to Staff", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text("Select Staff Member", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.staffList.forEach { staff ->
                                FilterChip(
                                    selected = selectedStaff?.staffId == staff.staffId,
                                    onClick = { selectedStaff = staff },
                                    label = { Text(staff.name.split(" ").first(), fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandBluePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Task Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description & Deliverables") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            label = { Text("Deadline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = resourcesLink,
                            onValueChange = { resourcesLink = it },
                            label = { Text("Asset / Figma / Drive Link") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val staff = selectedStaff ?: uiState.staffList.firstOrNull()
                        if (title.isNotBlank() && staff != null) {
                            onAssignTask(title, description, staff.staffId, staff.name, priority, deadline, targetUnits, resourcesLink)
                            showAssignDialog = false
                        }
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("Assign Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Feedback Dialog
    taskToFeedback?.let { task ->
        var feedbackText by remember { mutableStateOf(task.adminFeedback) }

        AlertDialog(
            onDismissRequest = { taskToFeedback = null },
            title = { Text("Admin Feedback for ${task.assignedStaffName}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(task.title, style = MaterialTheme.typography.bodySmall, color = Slate600)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Review Feedback & Instructions") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddFeedback(task.id, feedbackText)
                        taskToFeedback = null
                    }
                ) {
                    Text("Submit Feedback")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToFeedback = null }) { Text("Cancel") }
            }
        )
    }
}
