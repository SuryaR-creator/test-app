package com.example.ui.staff

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ContentCategory
import com.example.domain.model.ContentStatus
import com.example.domain.model.GenzPluseContentItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun StaffGenzPluseScreen(
    uiState: StaffUiState,
    onCreateContent: (title: String, category: ContentCategory, description: String, text: String, links: List<String>, tags: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<ContentCategory?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredContent = remember(uiState.contentList, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            uiState.contentList
        } else {
            uiState.contentList.filter { it.category == selectedCategoryFilter }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = BrandBluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Content")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Content", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Slate50,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Intro
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrandCyan.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = BrandCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "GenzPluse Studio",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Content Creation, Formatting & Multi-Category Pipeline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }
            }

            // 3 Major Divisions Category Filter Chips
            item {
                Text(
                    text = "Content Categories",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                        label = { Text("Reels (9:16)") },
                        leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == ContentCategory.NEWS_TEMPLATE,
                        onClick = { selectedCategoryFilter = ContentCategory.NEWS_TEMPLATE },
                        label = { Text("News Template") },
                        leadingIcon = { Icon(Icons.Default.Newspaper, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == ContentCategory.LANDSCAPE,
                        onClick = { selectedCategoryFilter = ContentCategory.LANDSCAPE },
                        label = { Text("Landscape (16:9)") },
                        leadingIcon = { Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Content List
            if (filteredContent.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.CloudUpload,
                        title = "No Content in this Category",
                        description = "Tap '+ New Content' below to create and organize your Reels, News Templates, or Landscape graphics."
                    )
                }
            } else {
                items(filteredContent) { item ->
                    ContentItemCard(item = item)
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Create Content Modal Dialog
    if (showCreateDialog) {
        CreateContentDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, cat, desc, text, links, tags ->
                onCreateContent(title, cat, desc, text, links, tags)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ContentItemCard(
    item: GenzPluseContentItem,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Chip
                Surface(
                    color = when (item.category) {
                        ContentCategory.REELS -> Color(0xFFEDE9FE)
                        ContentCategory.NEWS_TEMPLATE -> Color(0xFFFEF3C7)
                        ContentCategory.LANDSCAPE -> Color(0xFFE0F2FE)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = when (item.category) {
                                ContentCategory.REELS -> Icons.Default.Movie
                                ContentCategory.NEWS_TEMPLATE -> Icons.Default.Newspaper
                                ContentCategory.LANDSCAPE -> Icons.Default.Landscape
                            },
                            contentDescription = null,
                            tint = when (item.category) {
                                ContentCategory.REELS -> Color(0xFF7C3AED)
                                ContentCategory.NEWS_TEMPLATE -> Color(0xFFD97706)
                                ContentCategory.LANDSCAPE -> Color(0xFF0284C7)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.category.name.replace("_", " "),
                            color = when (item.category) {
                                ContentCategory.REELS -> Color(0xFF7C3AED)
                                ContentCategory.NEWS_TEMPLATE -> Color(0xFFD97706)
                                ContentCategory.LANDSCAPE -> Color(0xFF0284C7)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                StatusBadge(status = item.status.name)
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (item.adminReviewNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = StatusSuccessContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Admin Review: ${item.adminReviewNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Created: ${item.createdAt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )

                Text(
                    text = "By ${item.creatorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CreateContentDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: ContentCategory, description: String, text: String, links: List<String>, tags: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ContentCategory.REELS) }
    var description by remember { mutableStateOf("") }
    var mainText by remember { mutableStateOf("") }
    var linksInput by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create GenzPluse Content", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("Select Content Division", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ContentCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.name.replace("_", " "), fontSize = 11.sp) },
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
                        label = { Text("Content Title *") },
                        placeholder = { Text("e.g. AI News Breakdown Reel") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Short Description") },
                        placeholder = { Text("Hook, target audience, format...") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = mainText,
                        onValueChange = { mainText = it },
                        label = { Text("Script / Main Content Body") },
                        placeholder = { Text("Type full script, talking points or body text...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = linksInput,
                        onValueChange = { linksInput = it },
                        label = { Text("References & Asset Links") },
                        placeholder = { Text("https://drive.google.com/...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val links = linksInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onSubmit(title, selectedCategory, description, mainText, links, tags)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Submit Content")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
