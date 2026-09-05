package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.domain.model.ContentStatus
import com.example.domain.model.RequestStatus
import com.example.domain.model.TaskStatus
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    uiState: AdminUiState,
    onNavigateToStaff: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToRequests: () -> Unit,
    onOpenAddStaffDialog: () -> Unit,
    onOpenAssignTaskDialog: () -> Unit,
    onOpenBroadcastDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalStaff = uiState.staffList.size
    val activeStaff = uiState.staffList.count { it.isActive }
    val completedTasks = uiState.allTasks.count { it.status == TaskStatus.COMPLETED }
    val pendingTasks = uiState.allTasks.count { it.status != TaskStatus.COMPLETED }
    val pendingContentReviews = uiState.contentList.count { it.status == ContentStatus.SUBMITTED }
    val pendingLeaves = uiState.leaveRequests.count { it.status == RequestStatus.PENDING }
    val openProblems = uiState.problemReports.count { it.status == RequestStatus.PENDING || it.status == RequestStatus.UNDER_REVIEW }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // ADMIN HERO BANNER
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Admin Control Suite",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Organization Management & Real-Time Monitoring",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }

                        Surface(
                            color = StatusSuccessContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ONLINE",
                                color = StatusSuccess,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick Actions Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onOpenAddStaffDialog,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Staff", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onOpenAssignTaskDialog,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onOpenBroadcastDialog,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Broadcast", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // METRIC STAT CARDS
        item {
            Text(
                text = "Key Operations Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Active Staff",
                    value = "$activeStaff",
                    subtitle = "$totalStaff Enrolled",
                    icon = Icons.Default.Groups,
                    iconColor = BrandBluePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToStaff
                )

                StatCard(
                    title = "Pending Tasks",
                    value = "$pendingTasks",
                    subtitle = "$completedTasks Done",
                    icon = Icons.Default.AssignmentLate,
                    iconColor = StatusWarning,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTasks
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Content Pending",
                    value = "$pendingContentReviews",
                    subtitle = "Reels & News",
                    icon = Icons.Default.VideoLibrary,
                    iconColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToContent
                )

                StatCard(
                    title = "Open Helpdesk",
                    value = "${pendingLeaves + openProblems}",
                    subtitle = "$pendingLeaves Leaves, $openProblems Issues",
                    icon = Icons.Default.ReportProblem,
                    iconColor = StatusError,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToRequests
                )
            }
        }

        // LIVE ATTENDANCE SNAPSHOT
        item {
            SectionHeader(
                title = "Live Shift Attendance (Today)",
                actionText = "Full Roster",
                onActionClick = onNavigateToStaff
            )
        }

        if (uiState.todayAttendance.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text("No check-in logs for today yet.", color = Slate500)
                    }
                }
            }
        } else {
            items(uiState.todayAttendance) { att ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccessContainer)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = att.staffName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Slate900
                                )
                                Text(
                                    text = "In at ${att.checkInTime ?: "09:00 AM"} • ${att.remarks}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }
                        }

                        StatusBadge(status = att.status.name)
                    }
                }
            }
        }

        // PENDING CONTENT REVIEWS PREVIEW
        item {
            SectionHeader(
                title = "Content Pending Approval (${pendingContentReviews})",
                actionText = "Review Desk",
                onActionClick = onNavigateToContent
            )
        }

        val pendingList = uiState.contentList.filter { it.status == ContentStatus.SUBMITTED }
        if (pendingList.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text("All content submissions reviewed! No pending items in queue.", color = Slate500)
                    }
                }
            }
        } else {
            items(pendingList) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToContent() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFEDE9FE),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.category.name.replace("_", " "),
                                        color = Color(0xFF7C3AED),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By ${item.creatorName} • ${item.createdAt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
