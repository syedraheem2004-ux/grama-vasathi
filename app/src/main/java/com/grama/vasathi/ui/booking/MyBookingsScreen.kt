package com.grama.vasathi.ui.booking

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.grama.vasathi.data.model.BookingModel
import com.grama.vasathi.data.model.BookingTab
import com.grama.vasathi.ui.roleselection.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    role: UserRole,
    viewModel: BookingsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onExploreStaysClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val guestBookings by viewModel.guestBookings.collectAsState()
    val hostBookings by viewModel.hostBookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showReviewSheet by remember { mutableStateOf<BookingModel?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val bookings = if (role == UserRole.GUEST) {
        guestBookings.filter { it.status == selectedTab.name }
    } else {
        hostBookings.filter { 
            when(selectedTab) {
                BookingTab.PENDING -> it.status == "PENDING"
                BookingTab.CONFIRMED -> it.status == "CONFIRMED"
                BookingTab.CANCELLED -> it.status == "CANCELLED"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) { Icon(Icons.Default.FilterList, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF3E0))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFAF3E0)).padding(padding)) {
            BookingTabs(role, selectedTab) { viewModel.onTabSelected(it) }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFC1440E))
                }
            } else if (bookings.isEmpty()) {
                EmptyBookingsState(role, onExploreStaysClick, onDashboardClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bookings) { booking ->
                        if (role == UserRole.GUEST) {
                            GuestBookingCard(
                                booking = booking,
                                onCancel = { viewModel.cancelBooking(it) },
                                onReview = { showReviewSheet = it }
                            )
                        } else {
                            HostBookingCard(
                                booking = booking,
                                onAccept = { viewModel.acceptBooking(it) },
                                onReject = { viewModel.rejectBooking(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReviewSheet != null) {
        ReviewBottomSheet(
            booking = showReviewSheet!!,
            onDismiss = { showReviewSheet = null },
            onSubmit = { bId, stayId, r, c, h ->
                viewModel.submitReview(bId, stayId, r, c, h)
                showReviewSheet = null
            }
        )
    }
}

@Composable
fun BookingTabs(role: UserRole, selectedTab: BookingTab, onTabSelected: (BookingTab) -> Unit) {
    val tabs = BookingTab.values()
    val labels = if (role == UserRole.GUEST) {
        listOf("Pending", "Confirmed", "Cancelled")
    } else {
        listOf("New Requests", "Accepted", "Rejected")
    }

    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color(0xFFFAF3E0),
        contentColor = Color(0xFFC1440E),
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = Color(0xFFC1440E)
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(labels[index], fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

@Composable
fun GuestBookingCard(booking: BookingModel, onCancel: (String) -> Unit, onReview: (BookingModel) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray)) {
                Icon(Icons.Default.Home, null, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Farm Stay · Mandya", fontWeight = FontWeight.Bold)
                Text("${booking.checkIn} → ${booking.checkOut}", fontSize = 12.sp, color = Color.Gray)
                Text("ID: ${booking.bookingId}", fontSize = 11.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(booking.status)
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (booking.status == "PENDING") {
                OutlinedButton(onClick = { onCancel(booking.bookingId) }, shape = RoundedCornerShape(8.dp)) {
                    Text("Cancel Request")
                }
            }
            if (booking.status == "CONFIRMED") {
                Button(
                    onClick = { onReview(booking) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1440E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Write a Review")
                }
            }
        }
    }
}

@Composable
fun HostBookingCard(booking: BookingModel, onAccept: (String) -> Unit, onReject: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFAF3E0))) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Guest Request · ${booking.guestCount} Guests", fontWeight = FontWeight.Bold)
                    Text("Total: ₹${booking.totalPrice}", fontWeight = FontWeight.Bold, color = Color(0xFFC1440E))
                }
                Spacer(modifier = Modifier.weight(1f))
                StatusBadge(booking.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("${booking.checkIn} → ${booking.checkOut}", fontSize = 12.sp, color = Color.Gray)
            
            if (booking.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAccept(booking.bookingId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A6B35)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Accept")
                    }
                    OutlinedButton(
                        onClick = { onReject(booking.bookingId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "PENDING" -> Color(0xFFFFB400) to "Pending"
        "CONFIRMED" -> Color(0xFF3A6B35) to "Confirmed"
        "CANCELLED" -> Color.Red to "Cancelled"
        else -> Color.Gray to status
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun EmptyBookingsState(role: UserRole, onExplore: () -> Unit, onDashboard: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(80.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (role == UserRole.GUEST) "No bookings yet — start exploring farm stays!" 
            else "No requests yet — make sure your listing is active!",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = if (role == UserRole.GUEST) onExplore else onDashboard,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1440E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (role == UserRole.GUEST) "Explore Stays" else "Go to Dashboard")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(
    booking: BookingModel,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Int, String, Int) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var hygieneRating by remember { mutableStateOf(5f) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("How was your stay?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Lakshmi Farm Stay", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Overall Rating", fontWeight = FontWeight.Bold)
            Row {
                (1..5).forEach { i ->
                    IconButton(onClick = { rating = i }) {
                        Icon(
                            if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            null,
                            tint = if (i <= rating) Color(0xFFFFB400) else Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Hygiene & Cleanliness", fontWeight = FontWeight.Bold)
            Slider(
                value = hygieneRating,
                onValueChange = { hygieneRating = it },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF3A6B35), activeTrackColor = Color(0xFF3A6B35))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Share your experience...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onSubmit(booking.bookingId, booking.stayId, rating, comment, hygieneRating.toInt()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1440E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Submit Review")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
