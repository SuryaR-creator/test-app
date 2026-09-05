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
import com.example.domain.model.ContentCategory
import com.example.domain.model.ContentStatus
import com.example.domain.model.GenzPluseContentItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminContentReviewScreen(
    uiState: AdminUiState,
    onReviewContent: (contentId: String, status: ContentStatus, adminNotes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var contentToReview by remember { mutableStateOf<GenzPluseContentItem?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<ContentCategory?>(null) }

    val filteredContent = remember(uiState.contentList, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            uiState.contentList
        } else {
            uiState.contentList.filter { it.category == selectedCategoryFilter }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "GenzPluse Content Editorial Desk",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Text(
            text = "Review, Approve, or Request Changes on Reels, News & Landscape pieces",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null },
                label = { Text("All (${uiState.contentList.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandBluePrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedCategoryFilter == ContentCategory.REELS,
                onClick = { selectedCategoryFilter = ContentCategory.REELS },
                label = { Text("Reels") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandBluePrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedCategoryFilter == ContentCategory.NEWS_TEMPLATE,
                onClick = { selectedCategoryFilter = ContentCategory.NEWS_TEMPLATE },
                label = { Text("News Template") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandBluePrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedCategoryFilter == ContentCategory.LANDSCAPE,
                onClick = { selectedCategoryFilter = ContentCategory.LANDSCAPE },
                label = { Text("Landscape") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandBluePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredContent.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.DoneAll,
                title = "No Content to Display",
                description = "Content submitted by creators across all divisions will appear here for editorial review."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredContent) { item ->
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
                                    color = when (item.category) {
                                        ContentCategory.REELS -> Color(0xFFEDE9FE)
                                        ContentCategory.NEWS_TEMPLATE -> Color(0xFFFEF3C7)
                                        ContentCategory.LANDSCAPE -> Color(0xFFE0F2FE)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = item.category.name.replace("_", " "),
                                        color = when (item.category) {
                                            ContentCategory.REELS -> Color(0xFF7C3AED)
                                            ContentCategory.NEWS_TEMPLATE -> Color(0xFFD97706)
                                            ContentCategory.LANDSCAPE -> Color(0xFF0284C7)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                StatusBadge(status = item.status.name)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            if (item.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }

                            if (item.mainText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.mainText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate800,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            if (item.adminReviewNotes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Review Note: ${item.adminReviewNotes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "By ${item.creatorName} • ${item.createdAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )

                                Button(
                                    onClick = { contentToReview = item },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Review", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Editorial Review Modal Dialog
    contentToReview?.let { item ->
        var adminNotes by remember { mutableStateOf(item.adminReviewNotes) }

        AlertDialog(
            onDismissRequest = { contentToReview = null },
            title = { Text("Editorial Decision: ${item.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Creator: ${item.creatorName} (${item.category.name})", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    OutlinedTextField(
                        value = adminNotes,
                        onValueChange = { adminNotes = it },
                        label = { Text("Admin Feedback / Review Notes") },
                        placeholder = { Text("e.g. Excellent hook, approved for publishing...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onReviewContent(item.id, ContentStatus.REJECTED, adminNotes.ifEmpty { "Needs revisions." })
                            contentToReview = null
                        }
                    ) {
                        Text("Reject / Changes", color = StatusError)
                    }
                    Button(
                        onClick = {
                            onReviewContent(item.id, ContentStatus.APPROVED, adminNotes.ifEmpty { "Approved by Editorial desk." })
                            contentToReview = null
                        }
                    ) {
                        Text("Approve & Publish")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { contentToReview = null }) { Text("Cancel") }
            }
        )
    }
}
