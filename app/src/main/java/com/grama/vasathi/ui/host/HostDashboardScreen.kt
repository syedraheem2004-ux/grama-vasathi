package com.grama.vasathi.ui.host

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.grama.vasathi.data.model.BookingModel
import com.grama.vasathi.data.model.StayModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostDashboardScreen(
    viewModel: HostDashboardViewModel = viewModel(),
    onAddListingClick: () -> Unit,
    onViewChecklistClick: () -> Unit
) {
    val requests by viewModel.bookingRequests.collectAsState()
    val earnings by viewModel.earnings.collectAsState()
    val checklistProgress by viewModel.checklistProgress.collectAsState()
    val listings by viewModel.listings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        bottomBar = { HostBottomBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF3E0))
                .padding(padding)
        ) {
            item { HostHeader("Lakshmi") }
            
            item { StatsRow(earnings) }
            
            item { 
                BookingRequestsSection(
                    requests = requests,
                    onAccept = { viewModel.acceptBooking(it) },
                    onReject = { viewModel.rejectBooking(it) }
                ) 
            }
            
            item { 
                ReadinessChecklistSection(
                    progress = checklistProgress,
                    onViewFull = onViewChecklistClick
                ) 
            }
            
            item { EarningsChartSection(earnings.weeklyBreakdown) }
            
            item { 
                MyListingsSection(
                    listings = listings,
                    onAddClick = onAddListingClick
                ) 
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun HostHeader(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Namaskara, $name 🙏",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Here's what's happening at your farm today",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
        }
    }
}

@Composable
fun StatsRow(stats: com.grama.vasathi.data.model.EarningStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Total Bookings", "${stats.totalBookings}", Icons.Default.ListAlt, Modifier.weight(1f))
        StatCard(
            label = "Pending",
            value = "${stats.pendingCount}",
            icon = Icons.Default.Timer,
            modifier = Modifier.weight(1f),
            isHighlighted = stats.pendingCount > 0
        )
        StatCard("Earnings", "₹${stats.monthlyEarning}", Icons.Default.Payments, Modifier.weight(1.2f))
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier, isHighlighted: Boolean = false) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Color(0xFFC1440E) else Color.White,
            contentColor = if (isHighlighted) Color.White else Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, fontSize = 11.sp, color = if (isHighlighted) Color.White.copy(alpha = 0.8f) else Color.Gray)
        }
    }
}

@Composable
fun BookingRequestsSection(requests: List<BookingModel>, onAccept: (String) -> Unit, onReject: (String) -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Booking Requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("See All", color = Color(0xFFC1440E), fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (requests.isEmpty()) {
            EmptyRequestsState()
        } else {
            requests.take(3).forEach { request ->
                BookingRequestCard(request, onAccept, onReject)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun BookingRequestCard(request: BookingModel, onAccept: (String) -> Unit, onReject: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFAF3E0))) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Guest Request · ${request.guestCount} Guests", fontWeight = FontWeight.Bold)
                    Text("${request.checkIn} → ${request.checkOut}", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                StatusBadge(request.status)
            }
            
            if (request.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAccept(request.bookingId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A6B35)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Accept")
                    }
                    OutlinedButton(
                        onClick = { onReject(request.bookingId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}

@Composable
fun ReadinessChecklistSection(progress: Int, onViewFull: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Your Readiness Checklist", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress / 10f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFF3A6B35),
                trackColor = Color(0xFFFAF3E0)
            )
            Text("$progress/10 items complete", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ChecklistItem("Clean bed linen provided", true)
            ChecklistItem("Safe drinking water available", true)
            ChecklistItem("Mosquito net installed", false)
            
            TextButton(onClick = onViewFull, modifier = Modifier.align(Alignment.End)) {
                Text("View Full Checklist", color = Color(0xFFC1440E))
            }
        }
    }
}

@Composable
fun ChecklistItem(label: String, isChecked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(
            if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            null,
            tint = if (isChecked) Color(0xFF3A6B35) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = if (isChecked) Color.Black else Color.Gray)
    }
}

@Composable
fun EarningsChartSection(data: List<Int>) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Earnings This Month", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val max = data.maxOrNull()?.toFloat() ?: 1f
                    val spacing = size.width / (data.size + 1)
                    val barWidth = 30.dp.toPx()
                    
                    data.forEachIndexed { index, value ->
                        val barHeight = (value / max) * size.height
                        drawRect(
                            color = Color(0xFFC1440E),
                            topLeft = Offset(
                                x = (index + 1) * spacing - barWidth / 2,
                                y = size.height - barHeight
                            ),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyListingsSection(listings: List<StayModel>, onAddClick: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("My Listings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1440E)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Text(" Add New", fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(listings) { stay ->
                Card(
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        AsyncImage(
                            model = stay.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stay.village, fontWeight = FontWeight.Bold, maxLines = 1)
                            StatusBadge("ACTIVE", isSmall = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, isSmall: Boolean = false) {
    val (color, label) = when (status) {
        "PENDING" -> Color(0xFFFFB400) to "Pending"
        "ACCEPTED" -> Color(0xFF3A6B35) to "Accepted"
        "REJECTED" -> Color.Red to "Rejected"
        "ACTIVE" -> Color(0xFF3A6B35) to "Active"
        else -> Color.Gray to status
    }
    
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = if (isSmall) 6.dp else 12.dp, vertical = if (isSmall) 2.dp else 4.dp)
    ) {
        Text(label, color = color, fontSize = if (isSmall) 10.sp else 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyRequestsState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
        Text("No new requests yet", color = Color.Gray)
        Text("Share your listing to get more!", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun HostBottomBar() {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Dashboard, null) },
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.ListAlt, null) },
            label = { Text("Bookings") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Listings") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile") }
        )
    }
}
