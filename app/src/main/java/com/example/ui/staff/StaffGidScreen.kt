package com.example.ui.staff

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
import com.example.domain.model.AnnouncementItem
import com.example.domain.model.AnnouncementPriority
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun StaffGidScreen(
    uiState: StaffUiState,
    onAnnouncementClick: (AnnouncementItem) -> Unit,
    onNavigateToNotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // GID Title Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(18.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandCyan.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = BrandCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "GID Enterprise Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Staff Organizational Workspace & Announcements",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }
        }

        // Staff Identity Badges
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Workspace Profile",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Staff ID", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text(uiState.session?.staffId ?: "GP-STAFF-101", fontWeight = FontWeight.Bold, color = Slate900)
                        }
                        Column {
                            Text("Department", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text(uiState.session?.department ?: "Content & Media", fontWeight = FontWeight.Bold, color = Slate900)
                        }
                        Column {
                            Text("Designation", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text(uiState.session?.designation ?: "Senior Editor", fontWeight = FontWeight.Bold, color = Slate900)
                        }
                    }
                }
            }
        }

        // Announcements Header
        item {
            SectionHeader(
                title = "Official Admin Announcements",
                actionText = ""
            )
        }

        if (uiState.announcements.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Campaign,
                    title = "No Announcements Yet",
                    description = "When Admin broadcasts official updates, they will appear here in real-time."
                )
            }
        } else {
            items(uiState.announcements) { announcement ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAnnouncementClick(announcement) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = when (announcement.priority) {
                                    AnnouncementPriority.URGENT -> StatusErrorContainer
                                    AnnouncementPriority.IMPORTANT -> StatusWarningContainer
                                    AnnouncementPriority.NORMAL -> StatusInfoContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${announcement.priority.name} NOTICE",
                                    color = when (announcement.priority) {
                                        AnnouncementPriority.URGENT -> StatusError
                                        AnnouncementPriority.IMPORTANT -> Color(0xFFD97706)
                                        AnnouncementPriority.NORMAL -> StatusInfo
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = announcement.publishedAt,
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = announcement.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Published by ${announcement.authorName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }

        // Quick Notes Section Shortcut
        item {
            SectionHeader(
                title = "Personal Notes & Scratchpad",
                actionText = "Open Notes (${uiState.notes.size})",
                onActionClick = onNavigateToNotes
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToNotes() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = BrandBluePrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Private Staff Notes",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = Slate900
                        )
                        Text(
                            text = "${uiState.notes.size} private notes saved • Tap to manage",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Slate400
                    )
                }
            }
        }
    }
}
