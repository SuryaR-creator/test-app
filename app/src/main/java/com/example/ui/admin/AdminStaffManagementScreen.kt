package com.example.ui.admin

import androidx.compose.animation.AnimatedVisibility
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
import com.example.domain.model.StaffProfile
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun AdminStaffManagementScreen(
    uiState: AdminUiState,
    onAddStaff: (staffId: String, name: String, username: String, email: String, phone: String, dept: String, desig: String, target: Int) -> Unit,
    onToggleActive: (staffId: String, currentActive: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddStaffDialog by remember { mutableStateOf(false) }

    val filteredStaff = remember(uiState.staffList, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.staffList
        } else {
            uiState.staffList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.staffId.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true) ||
                it.designation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStaffDialog = true },
                containerColor = BrandBluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Staff", fontWeight = FontWeight.Bold)
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, Staff ID, or department...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats summary row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enrolled Staff Directory (${filteredStaff.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "${uiState.staffList.count { it.isActive }} Active",
                    style = MaterialTheme.typography.labelMedium,
                    color = StatusSuccess,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredStaff.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PersonOff,
                    title = "No Staff Members Found",
                    description = "Try searching with a different keyword or tap '+ Add Staff' to onboard a new employee."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredStaff) { staff ->
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (staff.isActive) BrandBluePrimary.copy(alpha = 0.15f)
                                                    else Slate200
                                                )
                                        ) {
                                            Text(
                                                text = staff.name.take(1).uppercase(),
                                                color = if (staff.isActive) BrandBluePrimary else Slate500,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = staff.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                            Text(
                                                text = "${staff.designation} • ${staff.department}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate600
                                            )
                                        }
                                    }

                                    // Active / Deactivate switch
                                    Switch(
                                        checked = staff.isActive,
                                        onCheckedChange = { onToggleActive(staff.staffId, staff.isActive) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = StatusSuccess,
                                            uncheckedThumbColor = Slate400,
                                            uncheckedTrackColor = Slate200
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Staff ID", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                        Text(staff.staffId, fontWeight = FontWeight.SemiBold, color = Slate800)
                                    }
                                    Column {
                                        Text("Phone", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                        Text(staff.phoneNumber, fontWeight = FontWeight.SemiBold, color = Slate800)
                                    }
                                    Column {
                                        Text("Monthly Target", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                        Text("${staff.assignedTarget} Units", fontWeight = FontWeight.Bold, color = BrandBluePrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Staff Modal Dialog
    if (showAddStaffDialog) {
        var staffId by remember { mutableStateOf("GP-STAFF-104") }
        var name by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+91 ") }
        var department by remember { mutableStateOf("Content & Media") }
        var designation by remember { mutableStateOf("Reels Creator") }
        var targetValue by remember { mutableIntStateOf(80) }

        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = { Text("Onboard New Staff Member", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = staffId,
                            onValueChange = { staffId = it },
                            label = { Text("Staff ID *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (username.isEmpty()) username = it.lowercase().replace(" ", "")
                                if (email.isEmpty()) email = "${it.lowercase().replace(" ", ".")}@genzpluse.org"
                            },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. Ramesh Kumar") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Login Username *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Official Email *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text("Department") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = designation,
                            onValueChange = { designation = it },
                            label = { Text("Designation") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (staffId.isNotBlank() && name.isNotBlank() && email.isNotBlank()) {
                            onAddStaff(staffId, name, username, email, phone, department, designation, targetValue)
                            showAddStaffDialog = false
                        }
                    },
                    enabled = staffId.isNotBlank() && name.isNotBlank()
                ) {
                    Text("Complete Onboarding")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) { Text("Cancel") }
            }
        )
    }
}
