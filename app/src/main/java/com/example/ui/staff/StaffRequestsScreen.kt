package com.example.ui.staff

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun StaffRequestsScreen(
    uiState: StaffUiState,
    onSubmitLeave: (type: LeaveType, fromDate: String, toDate: String, days: Int, reason: String) -> Unit,
    onSubmitProblem: (category: ProblemCategory, title: String, description: String, priority: ProblemPriority) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Leave, 1: Problem
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showProblemDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showLeaveDialog = true else showProblemDialog = true
                },
                containerColor = BrandBluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedTab == 0) "Apply Leave" else "Report Problem", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Slate50,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tab Switcher
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Slate200,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Button(
                        onClick = { selectedTab = 0 },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) Color.White else Color.Transparent,
                            contentColor = if (selectedTab == 0) BrandBluePrimary else Slate600
                        ),
                        elevation = if (selectedTab == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Leave Requests", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedTab = 1 },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) Color.White else Color.Transparent,
                            contentColor = if (selectedTab == 1) BrandBluePrimary else Slate600
                        ),
                        elevation = if (selectedTab == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Problem Reports", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // LEAVE REQUESTS LIST
                if (uiState.leaveRequests.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.EventAvailable,
                        title = "No Leave Requests",
                        description = "You have no active or historical leave requests on record."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.leaveRequests) { leave ->
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
                                            color = Color(0xFFDBEAFE),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${leave.leaveType.name} LEAVE",
                                                color = BrandBluePrimary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        StatusBadge(status = leave.status.name)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "${leave.fromDate} to ${leave.toDate} (${leave.numberOfDays} Days)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Reason: ${leave.reason}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700
                                    )

                                    if (!leave.adminResponse.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = StatusSuccessContainer,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Admin Note: ${leave.adminResponse}",
                                                color = StatusSuccess,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Submitted: ${leave.submittedAt}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // PROBLEM REPORTS LIST
                if (uiState.problemReports.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.CheckCircleOutline,
                        title = "No Problem Reports Logged",
                        description = "If you encounter hardware, software, or workflow issues, submit a report here."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.problemReports) { prob ->
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
                                            color = Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = prob.category.name.replace("_", " "),
                                                color = StatusError,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        StatusBadge(status = prob.status.name)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = prob.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = prob.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700
                                    )

                                    if (!prob.adminNotes.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = StatusInfoContainer,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "IT / Admin Response: ${prob.adminNotes}",
                                                color = StatusInfo,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Priority: ${prob.priority.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (prob.priority == ProblemPriority.HIGH || prob.priority == ProblemPriority.CRITICAL) StatusError else Slate600
                                        )
                                        Text(
                                            text = prob.submittedAt,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Apply Leave Dialog
    if (showLeaveDialog) {
        var leaveType by remember { mutableStateOf(LeaveType.CASUAL) }
        var fromDate by remember { mutableStateOf("2026-09-15") }
        var toDate by remember { mutableStateOf("2026-09-16") }
        var daysCount by remember { mutableIntStateOf(2) }
        var reason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Apply for Official Leave", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Leave Type", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(LeaveType.CASUAL, LeaveType.SICK, LeaveType.EMERGENCY).forEach { type ->
                            FilterChip(
                                selected = leaveType == type,
                                onClick = { leaveType = type },
                                label = { Text(type.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        label = { Text("From Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        label = { Text("To Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave *") },
                        placeholder = { Text("Detailed reason...") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isNotBlank()) {
                            onSubmitLeave(leaveType, fromDate, toDate, daysCount, reason)
                            showLeaveDialog = false
                        }
                    },
                    enabled = reason.isNotBlank()
                ) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Report Problem Dialog
    if (showProblemDialog) {
        var category by remember { mutableStateOf(ProblemCategory.HARDWARE_EQUIPMENT) }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(ProblemPriority.HIGH) }

        AlertDialog(
            onDismissRequest = { showProblemDialog = false },
            title = { Text("Report Problem / IT Ticket", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Issue Category", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(ProblemCategory.TECHNICAL_ISSUE, ProblemCategory.HARDWARE_EQUIPMENT, ProblemCategory.WORK_RELATED).forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.name.replace("_", " "), fontSize = 10.sp) },
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
                        label = { Text("Issue Title *") },
                        placeholder = { Text("e.g. Workstation fan noise") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Description *") },
                        placeholder = { Text("Specify error codes, workstation #, or details...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            onSubmitProblem(category, title, description, priority)
                            showProblemDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && description.isNotBlank()
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProblemDialog = false }) { Text("Cancel") }
            }
        )
    }
}
