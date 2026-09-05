package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AppNotification
import com.example.domain.model.UserRole
import com.example.domain.model.UserSession
import com.example.ui.theme.*

@Composable
fun AppHeader(
    session: UserSession?,
    unreadNotificationsCount: Int = 2,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Left: Logo & Welcome
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Organization Logo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(BrandBluePrimary, BrandCyan)
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "GenzPluse Logo",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GenzPluse",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandBluePrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (session?.role == UserRole.ADMIN) Color(0xFFFEF3C7) else Color(0xFFDBEAFE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (session?.role == UserRole.ADMIN) "ADMIN" else "STAFF",
                                color = if (session?.role == UserRole.ADMIN) Color(0xFFD97706) else BrandBluePrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = "Welcome, ${session?.name?.split(" ")?.firstOrNull() ?: "Staff"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Notifications, Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Notification Bell with Badge
                Box {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (unreadNotificationsCount > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(StatusError)
                        ) {
                            Text(
                                text = unreadNotificationsCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Profile Avatar Icon
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BrandBluePrimary.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = (session?.name?.firstOrNull() ?: 'U').uppercase(),
                            color = BrandBluePrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
