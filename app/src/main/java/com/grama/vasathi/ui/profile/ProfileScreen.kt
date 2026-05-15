package com.grama.vasathi.ui.profile

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.vasathi.data.model.UserProfile
import com.grama.vasathi.ui.roleselection.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onLogOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var editedName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var editedPhone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var editedLocation by remember(profile.location) { mutableStateOf(profile.location) }
    var editedBio by remember(profile.bio) { mutableStateOf(profile.bio) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(if (isEditMode) Icons.Default.Close else Icons.Default.Edit, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF3E0))
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFC1440E))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAF3E0))
                    .padding(padding)
            ) {
                item { ProfileHeader(profile) }
                
                item { StatsRow(profile.role) }
                
                item { 
                    PersonalInformationSection(
                        isEditMode = isEditMode,
                        name = editedName,
                        onNameChange = { editedName = it },
                        email = profile.email,
                        phone = editedPhone,
                        onPhoneChange = { editedPhone = it },
                        location = editedLocation,
                        onLocationChange = { editedLocation = it },
                        bio = editedBio,
                        onBioChange = { editedBio = it }
                    ) 
                }
                
                if (isEditMode) {
                    item {
                        Button(
                            onClick = { 
                                viewModel.updateProfile(profile.copy(
                                    fullName = editedName,
                                    phone = editedPhone,
                                    location = editedLocation,
                                    bio = editedBio
                                ))
                            },
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1440E)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
                
                item { AppSettingsSection(settings, onSettingChange = { dark, notif -> viewModel.updateSetting(dark, notif) }) }
                
                item { 
                    AccountActionsSection(
                        onLogoutClick = { showLogoutDialog = true },
                        onDeleteClick = { showDeleteDialog = true }
                    ) 
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = { 
                viewModel.logOut()
                onLogOut()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            onConfirm = { viewModel.deleteAccount("DELETE", onDeleteAccount) },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun ProfileHeader(profile: UserProfile) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(96.dp)) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.fullName.take(1).uppercase() + (profile.fullName.split(" ").getOrNull(1)?.take(1)?.uppercase() ?: ""),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC1440E)
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFFC1440E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(profile.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        val badgeColor = if (profile.role == "HOST") Color(0xFF3A6B35) else Color(0xFFC1440E)
        val badgeText = if (profile.role == "HOST") "Verified Host 🏡" else "Guest Traveler 🎒"
        
        Surface(
            color = badgeColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(badgeText, color = badgeColor, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Text(profile.location.ifEmpty { "Mandya Village, Karnataka" }, color = Color.Gray, fontSize = 14.sp)
        }
        Text("Member since ${profile.memberSince.ifEmpty { "April 2024" }}", color = Color.LightGray, fontSize = 12.sp)
    }
}

@Composable
fun StatsRow(role: String) {
    val stats = if (role == "HOST") {
        listOf("Listings" to "2", "Bookings" to "24", "Rating" to "⭐ 4.8", "Earnings" to "₹14.4k")
    } else {
        listOf("Completed" to "5", "Wishlist" to "8", "Reviews" to "4", "Cities" to "3")
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stats) { (label, value) ->
            Card(
                modifier = Modifier.width(100.dp).height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(label, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PersonalInformationSection(
    isEditMode: Boolean,
    name: String, onNameChange: (String) -> Unit,
    email: String,
    phone: String, onPhoneChange: (String) -> Unit,
    location: String, onLocationChange: (String) -> Unit,
    bio: String, onBioChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Personal Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        ProfileField("Full Name", name, isEditMode, onNameChange)
        ProfileField("Email", email, false, {}) // Email non-editable
        ProfileField("Phone Number", phone, isEditMode, onPhoneChange)
        ProfileField("Village / City", location, isEditMode, onLocationChange)
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("About Me", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        if (isEditMode) {
            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 150) onBioChange(it) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("${bio.length}/150") }
            )
        } else {
            Text(bio.ifEmpty { "Experience seeker and nature lover." }, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, isEditMode: Boolean, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        if (isEditMode) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun AppSettingsSection(settings: com.grama.vasathi.data.model.AppSettings, onSettingChange: (Boolean?, Boolean?) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingRow("Dark Mode", Icons.Default.DarkMode, settings.isDarkMode) { onSettingChange(it, null) }
        SettingRow("Booking Notifications", Icons.Default.Notifications, settings.notificationsEnabled) { onSettingChange(null, it) }
        
        ActionRow("Language", Icons.Default.Language, settings.language)
        ActionRow("Location Access", Icons.Default.MyLocation, "Enabled")
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingRow(label: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFFC1440E), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFC1440E), checkedTrackColor = Color(0xFFC1440E).copy(alpha = 0.5f))
        )
    }
}

@Composable
fun ActionRow(label: String, icon: ImageVector, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFFC1440E), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Color.Gray, fontSize = 14.sp)
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun AccountActionsSection(onLogoutClick: () -> Unit, onDeleteClick: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth().clickable { onLogoutClick() }.padding(vertical = 12.dp)) {
            Icon(Icons.Default.Logout, null, tint = Color(0xFFC1440E))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Log Out", color = Color(0xFFC1440E), fontWeight = FontWeight.Bold)
        }
        
        Row(modifier = Modifier.fillMaxWidth().clickable { onDeleteClick() }.padding(vertical = 12.dp)) {
            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Delete Account", color = Color.Red.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1440E))) {
                Text("Log Out")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Log Out?") },
        text = { Text("Are you sure you want to log out of Grama-Vasathi?") },
        containerColor = Color.White
    )
}

@Composable
fun DeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var confirmText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmText == "DELETE",
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete Permanently")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Delete Account?") },
        text = {
            Column {
                Text("This will permanently delete your account and all your data. This action cannot be undone.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    placeholder = { Text("Type DELETE to confirm") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = Color.White
    )
}
