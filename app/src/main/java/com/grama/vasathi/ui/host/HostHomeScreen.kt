package com.grama.vasathi.ui.host

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.vasathi.data.model.BookingModel
import com.grama.vasathi.data.model.StayModel

private val Terracotta = Color(0xFFC1440E)
private val ForestGreen = Color(0xFF3A6B35)
private val Cream = Color(0xFFFAF3E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostHomeScreen(
    viewModel: HostHomeViewModel = viewModel(),
    onChecklistClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val listingStatus by viewModel.listingStatus.collectAsState()
    val checklistProgress by viewModel.checklistProgress.collectAsState()
    val pendingCount by viewModel.pendingBookingsCount.collectAsState()
    val recentBookings by viewModel.recentBookings.collectAsState()
    val earnings by viewModel.earningsSummary.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val hostName by viewModel.hostName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddListingSheet by remember { mutableStateOf(false) }
    var showActivitySheet by remember { mutableStateOf(false) }
    var isEditingActivities by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            HostHomeBottomBar(
                onHomeClick = {},
                onBookingsClick = onBookingsClick,
                onChecklistClick = onChecklistClick,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Terracotta),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(hostName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Namaskara, $hostName 🙏", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Your farm is waiting for guests", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    BadgedBox(badge = { Badge { Text("3") } }) {
                        IconButton(onClick = {}) { Icon(Icons.Default.Notifications, null) }
                    }
                }
            }

            // Listing Status Banner
            item { ListingStatusBanner(listingStatus, checklistProgress, onChecklistClick) { showAddListingSheet = true } }

            // Quick Actions Grid
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))
                QuickActionsGrid(
                    checklistProgress = checklistProgress,
                    pendingCount = pendingCount,
                    onAddListing = { showAddListingSheet = true },
                    onChecklist = onChecklistClick,
                    onBookings = onBookingsClick
                )
            }

            // Add Listing Card (only if no listing)
            if (listingStatus == ListingStatus.NO_LISTING) {
                item {
                    AddListingPromptCard { showAddListingSheet = true }
                }
            }

            // Checklist Quick Access
            item {
                ChecklistQuickCard(progress = checklistProgress, onContinue = onChecklistClick)
            }

            // Farm Activities
            item {
                ActivitiesSection(
                    activities = activities,
                    isEditing = isEditingActivities,
                    onEditToggle = { isEditingActivities = !isEditingActivities },
                    onRemove = { viewModel.removeActivity(it) },
                    onAddClick = { showActivitySheet = true }
                )
            }

            // Recent Bookings
            item {
                RecentBookingsSection(bookings = recentBookings, onSeeAll = onBookingsClick)
            }

            // Earnings
            item {
                EarningsSummaryCard(earnings.monthlyEarning, earnings.weeklyBreakdown)
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Add Listing Bottom Sheet
    if (showAddListingSheet) {
        AddListingBottomSheet(
            onDismiss = { showAddListingSheet = false },
            onSubmit = { listing ->
                viewModel.submitListing(listing)
                showAddListingSheet = false
            }
        )
    }

    // Add Activity Sheet
    if (showActivitySheet) {
        ActivityPickerSheet(
            currentActivities = activities,
            onSave = { selected ->
                selected.forEach { viewModel.addActivity(it) }
                showActivitySheet = false
            },
            onDismiss = { showActivitySheet = false }
        )
    }
}

@Composable
fun ListingStatusBanner(
    status: ListingStatus,
    progress: Float,
    onChecklist: () -> Unit,
    onCreateListing: () -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    val (bgColor, icon, title, subtitle) = when (status) {
        ListingStatus.NO_LISTING -> listOf(Color(0xFFFFF3E0), "🟠", "Your listing is not live yet!", "Add your first stay to start earning")
        ListingStatus.CHECKLIST_INCOMPLETE -> listOf(Color(0xFFFFFDE7), "🟡", "Complete your checklist to go live!", "Almost there!")
        ListingStatus.LIVE -> listOf(Color(0xFFE8F5E9), "🟢", "Your listing is LIVE! 🎉", "Guests can now discover your farm stay")
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor as Color)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("$icon $title", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle as String, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            if (status == ListingStatus.CHECKLIST_INCOMPLETE) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = ForestGreen, trackColor = Color.LightGray
                )
                Text("${(progress * 100).toInt()}% complete", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onChecklist, colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(12.dp)) {
                    Text("Finish Checklist →")
                }
            } else if (status == ListingStatus.NO_LISTING) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onCreateListing, colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(12.dp)) {
                    Text("Create Listing Now →")
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    checklistProgress: Float,
    pendingCount: Int,
    onAddListing: () -> Unit,
    onChecklist: () -> Unit,
    onBookings: () -> Unit
) {
    val actions = listOf(
        Triple("🏡", "Add/Edit Listing", Terracotta),
        Triple("✅", "Checklist", ForestGreen),
        Triple("📋", "Bookings", Color(0xFFFFB400)),
        Triple("🌾", "Farm Activities", Color(0xFF4CAF50)),
        Triple("💰", "Earnings", Color(0xFF7B1FA2)),
        Triple("⭐", "Reviews", Color(0xFFFF6F00))
    )
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        actions.chunked(2).forEachIndexed { rowIdx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { colIdx, (icon, label, color) ->
                    val actionIdx = rowIdx * 2 + colIdx
                    Card(
                        modifier = Modifier.weight(1f).height(100.dp).clickable {
                            when (actionIdx) {
                                0 -> onAddListing()
                                1 -> onChecklist()
                                2 -> onBookings()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            Column {
                                Text(icon, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            // Badges
                            when (actionIdx) {
                                1 -> Badge(modifier = Modifier.align(Alignment.TopEnd), containerColor = ForestGreen) {
                                    Text("${(checklistProgress * 17).toInt()}/17 ✅", fontSize = 9.sp)
                                }
                                2 -> if (pendingCount > 0) Badge(modifier = Modifier.align(Alignment.TopEnd), containerColor = Color.Red) {
                                    Text("$pendingCount Pending", fontSize = 9.sp)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.align(Alignment.BottomEnd).size(16.dp), tint = Color.LightGray)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun AddListingPromptCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, Terracotta, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, null, tint = Terracotta, modifier = Modifier.size(32.dp))
            Text("Tap to add your first farm stay listing", color = Terracotta, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ChecklistQuickCard(progress: Float, onContinue: () -> Unit) {
    val animProg by animateFloatAsState(targetValue = progress, label = "ring")
    val sections = listOf("🛏️" to "Room", "🚿" to "Hygiene", "💧" to "Water", "🔒" to "Safety", "📶" to "Connectivity")
    val sectionProgress = listOf(1f, 0.75f, 0.5f, 1f, 0.25f)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                    Canvas(modifier = Modifier.size(56.dp)) {
                        drawArc(color = Color(0xFFEEEEEE), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 8.dp.toPx()))
                        drawArc(color = Terracotta, startAngle = -90f, sweepAngle = 360f * animProg, useCenter = false, style = Stroke(width = 8.dp.toPx()))
                    }
                    Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Terracotta)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Readiness Checklist", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        sections.forEachIndexed { i, (icon, _) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(icon, fontSize = 16.sp)
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(
                                    if (sectionProgress[i] >= 1f) ForestGreen else Color.LightGray
                                ))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Continue Checklist →") }
        }
    }
}

@Composable
fun ActivitiesSection(
    activities: List<String>,
    isEditing: Boolean,
    onEditToggle: () -> Unit,
    onRemove: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Your Farm Activities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onEditToggle) { Text(if (isEditing) "Done" else "Edit", color = Terracotta) }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activities) { activity ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(activity) },
                    trailingIcon = if (isEditing) {{ IconButton(onClick = { onRemove(activity) }, modifier = Modifier.size(18.dp)) { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) } }} else null
                )
            }
            item {
                OutlinedButton(onClick = onAddClick, shape = RoundedCornerShape(50), border = ButtonDefaults.outlinedButtonBorder) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Text(" Add Activity")
                }
            }
        }
    }
}

@Composable
fun RecentBookingsSection(bookings: List<BookingModel>, onSeeAll: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Booking Requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onSeeAll) { Text("See All", color = Terracotta) }
        }
        if (bookings.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No bookings yet", color = Color.Gray)
                    Text("Your listing needs to go live first!", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            bookings.forEach { booking ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Cream), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Guest · ${booking.guestCount} people", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${booking.checkIn} → ${booking.checkOut}", fontSize = 12.sp, color = Color.Gray)
                        }
                        val (statusColor, statusText) = when (booking.status) {
                            "PENDING" -> Color(0xFFFFB400) to "Pending"
                            "ACCEPTED" -> ForestGreen to "Accepted"
                            else -> Color.Red to "Rejected"
                        }
                        Box(modifier = Modifier.background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EarningsSummaryCard(monthlyEarning: Int, weeklyData: List<Int>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("This Month", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("₹$monthlyEarning earned", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = ForestGreen)
                }
                TextButton(onClick = {}) { Text("View Full Report →", color = Terracotta) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (weeklyData.isNotEmpty()) {
                Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    val max = weeklyData.maxOrNull()?.toFloat() ?: 1f
                    val spacing = size.width / (weeklyData.size - 1)
                    val points = weeklyData.mapIndexed { i, v ->
                        Offset(i * spacing, size.height - (v / max) * size.height)
                    }
                    for (i in 0 until points.size - 1) {
                        drawLine(color = Terracotta, start = points[i], end = points[i + 1], strokeWidth = 3.dp.toPx())
                    }
                    points.forEach { drawCircle(color = Terracotta, radius = 4.dp.toPx(), center = it) }
                }
            }
        }
    }
}

@Composable
fun HostHomeBottomBar(onHomeClick: () -> Unit, onBookingsClick: () -> Unit, onChecklistClick: () -> Unit, onProfileClick: () -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = true, onClick = onHomeClick, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = onBookingsClick, icon = { Icon(Icons.Default.ListAlt, null) }, label = { Text("Bookings") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.House, null) }, label = { Text("My Listing") })
        NavigationBarItem(selected = false, onClick = onChecklistClick, icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Checklist") })
        NavigationBarItem(selected = false, onClick = onProfileClick, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
    }
}
