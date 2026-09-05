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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LeaveRequest
import com.example.domain.model.ProblemReport
import com.example.domain.model.RequestStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminRequestsReviewScreen(
    uiState: AdminUiState,
    onUpdateLeaveStatus: (requestId: String, status: RequestStatus, response: String) -> Unit,
    onUpdateProblemStatus: (reportId: String, status: RequestStatus, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Leaves, 1: Problems
    var leaveToReview by remember { mutableStateOf<LeaveRequest?>(null) }
    var problemToReview by remember { mutableStateOf<ProblemReport?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Staff Requests & IT Helpdesk",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector
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
                    Text("Leave Requests (${uiState.leaveRequests.size})", fontWeight = FontWeight.Bold)
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
                    Text("Problem Reports (${uiState.problemReports.size})", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // LEAVES LIST
            if (uiState.leaveRequests.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.EventAvailable,
                    title = "No Leave Requests",
                    description = "All employee leave submissions have been processed."
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
                                    Column {
                                        Text(leave.staffName, fontWeight = FontWeight.Bold, color = Slate900)
                                        Text("${leave.department} • ${leave.leaveType.name}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                    }
                                    StatusBadge(status = leave.status.name)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Dates: ${leave.fromDate} to ${leave.toDate} (${leave.numberOfDays} Days)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )

                                Text(
                                    text = "Reason: ${leave.reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )

                                if (!leave.adminResponse.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Admin Note: ${leave.adminResponse}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusSuccess
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { leaveToReview = leave },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Action", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // PROBLEMS LIST
            if (uiState.problemReports.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "No Open Issues",
                    description = "No unresolved problem tickets from staff members."
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
                                    Column {
                                        Text(prob.staffName, fontWeight = FontWeight.Bold, color = Slate900)
                                        Text(prob.category.name.replace("_", " "), style = MaterialTheme.typography.bodySmall, color = StatusError)
                                    }
                                    StatusBadge(status = prob.status.name)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(prob.title, fontWeight = FontWeight.Bold, color = Slate900)
                                Text(prob.description, style = MaterialTheme.typography.bodySmall, color = Slate700)

                                if (!prob.adminNotes.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Resolution: ${prob.adminNotes}", style = MaterialTheme.typography.bodySmall, color = StatusInfo)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { problemToReview = prob },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Update Ticket", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Leave Action Dialog
    leaveToReview?.let { leave ->
        var responseText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { leaveToReview = null },
            title = { Text("Review Leave: ${leave.staffName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${leave.leaveType.name} (${leave.numberOfDays} Days): ${leave.reason}", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("Admin Remark / Conditions") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onUpdateLeaveStatus(leave.id, RequestStatus.REJECTED, responseText.ifEmpty { "Rejected due to operational workload." })
                            leaveToReview = null
                        }
                    ) {
                        Text("Reject", color = StatusError)
                    }
                    Button(
                        onClick = {
                            onUpdateLeaveStatus(leave.id, RequestStatus.APPROVED, responseText.ifEmpty { "Leave granted." })
                            leaveToReview = null
                        }
                    ) {
                        Text("Approve")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { leaveToReview = null }) { Text("Cancel") }
            }
        )
    }

    // Problem Action Dialog
    problemToReview?.let { prob ->
        var notesText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { problemToReview = null },
            title = { Text("Update Ticket: ${prob.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reported by ${prob.staffName}: ${prob.description}", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("IT / Admin Resolution Notes") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onUpdateProblemStatus(prob.id, RequestStatus.UNDER_REVIEW, notesText.ifEmpty { "Ticket in progress." })
                            problemToReview = null
                        }
                    ) {
                        Text("In Review")
                    }
                    Button(
                        onClick = {
                            onUpdateProblemStatus(prob.id, RequestStatus.RESOLVED, notesText.ifEmpty { "Issue resolved by IT." })
                            problemToReview = null
                        }
                    ) {
                        Text("Mark Resolved")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { problemToReview = null }) { Text("Cancel") }
            }
        )
    }
}
