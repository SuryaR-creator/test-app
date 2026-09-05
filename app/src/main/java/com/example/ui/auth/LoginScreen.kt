package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserRole
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToStaff: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val currentSession by viewModel.currentSession.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Staff, 1: Admin
    var staffEmail by remember { mutableStateOf("") }
    var staffPassword by remember { mutableStateOf("") }
    var isStaffPasswordVisible by remember { mutableStateOf(false) }

    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var isAdminPasswordVisible by remember { mutableStateOf(false) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var resetFeedbackMsg by remember { mutableStateOf<String?>(null) }
    var isResetSuccess by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(currentSession) {
        if (currentSession != null) {
            if (currentSession?.role == UserRole.ADMIN) {
                onNavigateToAdmin()
            } else {
                onNavigateToStaff()
            }
        }
    }

    Scaffold(
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Header Hero
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(BrandBluePrimary, BrandCyan)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "GenzPluse",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Slate900,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Staff Productivity & Enterprise Management Suite",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Role Selector Tab
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Slate200,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp)
                ) {
                    // Staff Login Tab
                    Button(
                        onClick = {
                            selectedTab = 0
                            viewModel.clearError()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) Color.White else Color.Transparent,
                            contentColor = if (selectedTab == 0) BrandBluePrimary else Slate600
                        ),
                        elevation = if (selectedTab == 0) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Staff Login",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Admin Login Tab
                    Button(
                        onClick = {
                            selectedTab = 1
                            viewModel.clearError()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) Color.White else Color.Transparent,
                            contentColor = if (selectedTab == 1) BrandBluePrimary else Slate600
                        ),
                        elevation = if (selectedTab == 1) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Admin Portal",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Container for Forms
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    if (selectedTab == 0) {
                        // STAFF LOGIN FORM
                        Text(
                            text = "Staff Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Enter your registered staff email and password",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = staffEmail,
                            onValueChange = {
                                staffEmail = it
                                viewModel.clearError()
                            },
                            label = { Text("Staff Email") },
                            placeholder = { Text("kavitha.raman@genzpluse.org") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = BrandBluePrimary)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = staffPassword,
                            onValueChange = {
                                staffPassword = it
                                viewModel.clearError()
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBluePrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isStaffPasswordVisible = !isStaffPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isStaffPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (isStaffPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.loginStaff(staffEmail, staffPassword)
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = {
                                resetFeedbackMsg = null
                                showForgotDialog = true
                            }) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BrandBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.loginStaff(staffEmail, staffPassword)
                            },
                            enabled = uiState !is AuthUiState.Loading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (uiState is AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign In as Staff",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                    } else {
                        // ADMIN LOGIN FORM
                        Text(
                            text = "Admin Secure Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Administrator credentials required for console access",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = {
                                adminEmail = it
                                viewModel.clearError()
                            },
                            label = { Text("Admin Email") },
                            placeholder = { Text("admin.director@genzpluse.org") },
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandBluePrimary)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = {
                                adminPassword = it
                                viewModel.clearError()
                            },
                            label = { Text("Admin Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBluePrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isAdminPasswordVisible = !isAdminPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isAdminPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (isAdminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.loginAdmin(adminEmail, adminPassword)
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.loginAdmin(adminEmail, adminPassword)
                            },
                            enabled = uiState !is AuthUiState.Loading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (uiState is AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Shield, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Authenticate Admin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Error Message display
                    AnimatedVisibility(visible = uiState is AuthUiState.Error) {
                        if (uiState is AuthUiState.Error) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = StatusErrorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = StatusError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = (uiState as AuthUiState.Error).message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusError,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
                resetFeedbackMsg = null
            },
            title = {
                Text("Reset Account Password", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Enter your registered account email. A secure password reset link will be dispatched to your inbox.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Account Email") },
                        placeholder = { Text("user@genzpluse.org") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetFeedbackMsg != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = if (isResetSuccess) Color(0xFFDCFCE7) else StatusErrorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resetFeedbackMsg ?: "",
                                color = if (isResetSuccess) Color(0xFF166534) else StatusError,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPassword(forgotEmail) { success, msg ->
                            isResetSuccess = success
                            resetFeedbackMsg = msg
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    resetFeedbackMsg = null
                }) {
                    Text("Close")
                }
            }
        )
    }
}
