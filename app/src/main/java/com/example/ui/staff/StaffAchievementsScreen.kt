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
import com.example.ui.components.DonutProgressChart
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun StaffAchievementsScreen(
    uiState: StaffUiState,
    modifier: Modifier = Modifier
) {
    val ach = uiState.achievements
    val progress = uiState.targetProgress
    val completedPct = progress.completionPercentage
    val remainingPct = (100f - completedPct).coerceAtLeast(0f)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // DONUT PROGRESS CHART HERO CARD
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Target Completion Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Current Active Cycle: September 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Donut Chart
                    DonutProgressChart(
                        completedPercentage = completedPct,
                        completedValue = "${progress.completedTarget}",
                        remainingValue = "${progress.remainingTarget}",
                        size = 190.dp,
                        strokeWidth = 22.dp,
                        primaryColor = BrandBluePrimary,
                        secondaryColor = Slate200
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Legend Row: Completed % vs Remaining %
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Completed indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(BrandBluePrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Completed: ${completedPct.toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "${progress.completedTarget} Units",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }

                        // Remaining indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Slate300)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Remaining: ${remainingPct.toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "${progress.remainingTarget} Units",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }
                    }
                }
            }
        }

        // STATS SUMMARY ROW
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Total Assigned",
                    value = "${progress.totalTarget}",
                    subtitle = "Monthly Goal",
                    icon = Icons.Default.Flag,
                    iconColor = Slate700,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completed",
                    value = "${progress.completedTarget}",
                    subtitle = "Verified",
                    icon = Icons.Default.CheckCircle,
                    iconColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Remaining",
                    value = "${progress.remainingTarget}",
                    subtitle = "To Go",
                    icon = Icons.Default.HourglassTop,
                    iconColor = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // MONTHLY ACHIEVEMENTS & EXCELLENCE AWARDS
        item {
            SectionHeader(
                title = "Monthly Achievement History",
                actionText = "${ach.monthlyAchievements.size} Records"
            )
        }

        items(ach.monthlyAchievements) { month ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(StatusSuccessContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = month.monthYear,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Surface(
                                color = StatusSuccessContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = month.rating,
                                    color = StatusSuccess,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val percentage = if (month.targetAssigned > 0) (month.targetCompleted.toFloat() / month.targetAssigned.toFloat()) * 100f else 100f

                        Text(
                            text = "${month.targetCompleted}/${month.targetAssigned} Units Delivered • ${month.tasksFinished} Tasks (${percentage.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { percentage / 100f },
                            trackColor = Slate200,
                            color = StatusSuccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // PERFORMANCE MILESTONES
        item {
            SectionHeader(
                title = "Performance Milestones & Badges",
                actionText = ""
            )
        }

        items(ach.milestones) { milestone ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrandBluePrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = BrandBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = milestone.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = milestone.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }

                    Text(
                        text = milestone.dateAchieved,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
