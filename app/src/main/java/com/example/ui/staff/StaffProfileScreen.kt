package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun StaffProfileScreen(
    uiState: StaffUiState,
    onUpdateSelfProfile: (bio: String, emergencyContact: String, bloodGroup: String, address: String, phone: String) -> Unit,
    onLogout: () -> Unit,
    onOpenHelpdesk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.profile ?: StaffProfile(
        uid = "staff_001",
        staffId = uiState.session?.staffId ?: "GP-STAFF-101",
        name = uiState.session?.name ?: "Kavitha Raman",
        username = uiState.session?.username ?: "kavitha",
        email = uiState.session?.email ?: "kavitha.raman@genzpluse.org",
        phoneNumber = uiState.session?.phoneNumber ?: "+91 98401 23456",
        department = uiState.session?.department ?: "Content & Media",
        designation = uiState.session?.designation ?: "Senior Reels Editor",
        joiningDate = "2024-03-15",
        bio = "Creative multimedia editor specializing in viral Genz short-form storytelling and brand reels.",
        emergencyContact = "+91 98401 99999 (Spouse)",
        bloodGroup = "O+",
        address = "42 Silicon Avenue, Chennai",
        isActive = true,
        assignedTarget = 80,
        completedTarget = 60
    )

    var showEditModal by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // PROFILE HEADER HERO
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(BrandBluePrimary, BrandCyan)
                                )
                            )
                    ) {
                        Text(
                            text = profile.name.take(1).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Text(
                        text = "${profile.designation} • ${profile.department}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Staff ID: ${profile.staffId}",
                            color = BrandBluePrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // SEPARATION: ADMIN-LOCKED FIELDS
        item {
            SectionHeader(
                title = "Organization & Role Data",
                actionText = "Locked by Admin"
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LockedInfoRow("Official Email", profile.email)
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))
                    LockedInfoRow("Department", profile.department)
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))
                    LockedInfoRow("Designation", profile.designation)
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))
                    LockedInfoRow("Joining Date", profile.joiningDate)
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))
                    LockedInfoRow("Assigned Monthly Target", "${profile.assignedTarget} Units")
                }
            }
        }

        // SEPARATION: STAFF-EDITABLE FIELDS
        item {
            SectionHeader(
                title = "Personal & Contact Details",
                actionText = "Edit",
                onActionClick = { showEditModal = true }
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EditableInfoRow("Phone Number", profile.phoneNumber.ifEmpty { "Not specified" })
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))
                    EditableInfoRow("Emergency Contact", profile.emergencyContact.ifEmpty { "Not specified" })
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))
                    EditableInfoRow("Blood Group", profile.bloodGroup.ifEmpty { "Not specified" })
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))
                    EditableInfoRow("Residential Address", profile.address.ifEmpty { "Not specified" })
                    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))
                    EditableInfoRow("Professional Bio", profile.bio.ifEmpty { "No bio added" })
                }
            }
        }

        // HELPDESK & SUPPORT SECTION
        item {
            SectionHeader(
                title = "Helpdesk & IT Support",
                actionText = ""
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("GenzPluse Internal Helpdesk", fontWeight = FontWeight.Bold, color = Slate900)
                            Text("Fast escalation for IT, payroll, or workstation issues", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenHelpdesk,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Requests & IT Desk")
                    }
                }
            }
        }

        // CONTACT US SECTION
        item {
            SectionHeader(
                title = "Contact Organization",
                actionText = ""
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Headquarters: GenzPluse Media Hub, Chennai, India", style = MaterialTheme.typography.bodySmall, color = Slate800)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("support@genzpluse.org • hr@genzpluse.org", style = MaterialTheme.typography.bodySmall, color = Slate800)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Helpline: +91 98400 11223 (Ext 401)", style = MaterialTheme.typography.bodySmall, color = Slate800)
                    }
                }
            }
        }

        // LOGOUT BUTTON
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Workspace", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Edit Profile Modal Dialog
    if (showEditModal) {
        var editPhone by remember { mutableStateOf(profile.phoneNumber) }
        var editEmergency by remember { mutableStateOf(profile.emergencyContact) }
        var editBlood by remember { mutableStateOf(profile.bloodGroup) }
        var editAddress by remember { mutableStateOf(profile.address) }
        var editBio by remember { mutableStateOf(profile.bio) }

        AlertDialog(
            onDismissRequest = { showEditModal = false },
            title = { Text("Edit Personal Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Personal Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmergency,
                        onValueChange = { editEmergency = it },
                        label = { Text("Emergency Contact") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBlood,
                        onValueChange = { editBlood = it },
                        label = { Text("Blood Group") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Residential Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Professional Bio") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSelfProfile(editBio, editEmergency, editBlood, editAddress, editPhone)
                        showEditModal = false
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LockedInfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Slate900)
        }
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Admin Locked",
            tint = Slate400,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun EditableInfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Slate800)
        }
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Editable",
            tint = BrandBluePrimary.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}
