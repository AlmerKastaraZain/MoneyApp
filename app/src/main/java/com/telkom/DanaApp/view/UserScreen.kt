package com.telkom.DanaApp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await



@Composable
fun UserScreen(
    onNavigateToLogin: () -> Unit = {}
) {
    val orangeColor = Color(0xFFE5A547)
    val tealColor = Color(0xFF4A7C59)

    // Firebase Auth instance
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // States untuk menyimpan data user
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }
    var userPhone by remember { mutableStateOf("Loading...") }
    var userLocation by remember { mutableStateOf("Loading...") }
    var userId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // State untuk Edit Profile Dialog
    var showEditDialog by remember { mutableStateOf(false) }

    // State untuk Logout Confirmation
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Load user data dari Firebase
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            userEmail = user.email ?: "No email"
            userId = user.uid

            // Load additional data dari Firestore
            val db = FirebaseFirestore.getInstance()
            try {
                val document = db.collection("users").document(user.uid).get().await()
                if (document.exists()) {
                    userName = document.getString("username") ?: user.displayName ?: "User"
                    userPhone = document.getString("phone") ?: "No phone number"
                    userLocation = document.getString("location") ?: "No location"
                } else {
                    userName = user.displayName ?: "User"
                    userPhone = user.phoneNumber ?: "No phone number"
                    userLocation = "No location"
                }
            } catch (e: Exception) {
                userName = user.displayName ?: "User"
                userPhone = user.phoneNumber ?: "No phone number"
                userLocation = "No location"
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    orangeColor,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Back button and Edit Profile button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Account",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            // Profile section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        tint = orangeColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Hello! $userName",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "ID: ${userId.take(9)}...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Information Cards
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                ProfileInfoCard(
                    icon = Icons.Default.Person,
                    label = "Username",
                    value = if (isLoading) "Loading..." else userName
                )
                Spacer(modifier = Modifier.height(4.dp))

                ProfileInfoCard(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = if (isLoading) "Loading..." else userEmail
                )
                Spacer(modifier = Modifier.height(4.dp))

                ProfileInfoCard(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    value = if (isLoading) "Loading..." else userLocation
                )

                Spacer(modifier = Modifier.height(4.dp))

                ProfileInfoCard(
                    icon = Icons.Default.Phone,
                    label = "Number",
                    value = if (isLoading) "Loading..." else userPhone
                )

                // Tombol Logout
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

        }




    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            currentUsername = userName,
            currentLocation = userLocation,
            currentPhone = userPhone,
            onDismiss = { showEditDialog = false },
            onSave = { newUsername, newLocation, newPhone ->
                // Update data ke Firestore
                currentUser?.let { user ->
                    val db = FirebaseFirestore.getInstance()
                    val userData = hashMapOf(
                        "username" to newUsername,
                        "location" to newLocation,
                        "phone" to newPhone,
                        "email" to user.email
                    )

                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            // Update local state
                            userName = newUsername
                            userLocation = newLocation
                            userPhone = newPhone
                            showEditDialog = false
                        }
                        .addOnFailureListener {
                            // Handle error
                        }
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                FirebaseAuth.getInstance().signOut()
                showLogoutDialog = false
                onNavigateToLogin()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun EditProfileDialog(
    currentUsername: String,
    currentLocation: String,
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }
    var location by remember { mutableStateOf(currentLocation) }
    var phone by remember { mutableStateOf(currentPhone) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A7C59)
                )

                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Location Field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Phone Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(username, location, phone) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A7C59)
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Logout",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Are you sure you want to logout?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    val orangeColor = Color(0xFFE5A547)
    val tealColor = Color(0xFF4A7C59)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tealColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        orangeColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}