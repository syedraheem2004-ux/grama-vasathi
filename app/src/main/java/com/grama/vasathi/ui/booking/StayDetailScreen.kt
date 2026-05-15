package com.grama.vasathi.ui.booking

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.grama.vasathi.data.model.StayModel
import com.grama.vasathi.ui.home.StayViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StayDetailScreen(
    stayId: String,
    stayViewModel: StayViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel(),
    onBackClick: () -> Unit,
    onViewBookingsClick: () -> Unit
) {
    val stays by stayViewModel.stays.collectAsState()
    val stay = stays.find { it.id == stayId }
    
    val uiState by bookingViewModel.uiState.collectAsState()
    val checkIn by bookingViewModel.checkInDate.collectAsState()
    val checkOut by bookingViewModel.checkOutDate.collectAsState()
    val guests by bookingViewModel.guestCount.collectAsState()

    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    if (stay == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFC1440E))
        }
        return
    }

    Scaffold(
        bottomBar = {
            BookingBottomBar(
                price = stay.pricePerNight,
                onBookClick = { bookingViewModel.submitBooking(stay.id, stay.pricePerNight) },
                isLoading = uiState is BookingUiState.Loading
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF3E0))
                .padding(padding)
        ) {
            item { HeroSection(stay.imageUrl, onBackClick) }
            
            item { StayInfoSection(stay) }
            
            item {
                AboutSection(
                    description = "Experience the true essence of Karnataka village life. Wake up to fresh air, help with morning milking, and enjoy home-cooked meals prepared by Lakshmi aunty herself.",
                    isExpanded = isDescriptionExpanded,
                    onToggle = { isDescriptionExpanded = !isDescriptionExpanded }
                )
            }
            
            item { HostSection(stay.hostName) }
            
            item {
                BookingForm(
                    checkIn = checkIn,
                    checkOut = checkOut,
                    guests = guests,
                    totalPrice = bookingViewModel.calculateTotal(stay.pricePerNight),
                    onCheckInClick = { showCheckInPicker = true },
                    onCheckOutClick = { showCheckOutPicker = true },
                    onIncrementGuests = { bookingViewModel.incrementGuests() },
                    onDecrementGuests = { bookingViewModel.decrementGuests() }
                )
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Date Pickers
    if (showCheckInPicker) {
        StayDatePicker(
            onDateSelected = { 
                bookingViewModel.onCheckInDateSelected(it)
                showCheckInPicker = false
            },
            onDismiss = { showCheckInPicker = false }
        )
    }

    if (showCheckOutPicker) {
        StayDatePicker(
            onDateSelected = { 
                bookingViewModel.onCheckOutDateSelected(it)
                showCheckOutPicker = false
            },
            onDismiss = { showCheckOutPicker = false }
        )
    }

    // Success Bottom Sheet
    if (uiState is BookingUiState.Success) {
        val bookingId = (uiState as BookingUiState.Success).bookingId
        BookingSuccessSheet(
            bookingId = bookingId,
            onDismiss = { bookingViewModel.resetState() },
            onViewBookings = onViewBookingsClick,
            onBackToHome = onBackClick
        )
    }
}

@Composable
fun HeroSection(imageUrl: String, onBackClick: () -> Unit) {
    Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFFAF3E0).copy(alpha = 0.8f)),
                        startY = 400f
                    )
                )
        )

        // Top Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            IconButton(
                onClick = { },
                modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
            }
        }
    }
}

@Composable
fun StayInfoSection(stay: StayModel) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(stay.village, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFFC1440E), modifier = Modifier.size(16.dp))
            Text("${stay.district}, Karnataka", color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(18.dp))
            Text(" ${stay.rating} · 24 reviews · ", fontWeight = FontWeight.Bold)
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF3A6B35), modifier = Modifier.size(16.dp))
            Text(" Hygiene Score: ${stay.hygieneScore}/5", color = Color(0xFF3A6B35), fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(stay.activities) { activity ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(activity) },
                    colors = SuggestionChipDefaults.suggestionChipColors(labelColor = Color(0xFF3A6B35))
                )
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun AboutSection(description: String, isExpanded: Boolean, onToggle: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("About this Stay", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )
        Text(
            text = if (isExpanded) "Read less" else "Read more",
            color = Color(0xFFC1440E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onToggle() }.padding(vertical = 4.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun HostSection(hostName: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Your Host", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(hostName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Verified Host", color = Color(0xFF3A6B35), fontSize = 12.sp)
                    Icon(Icons.Default.Verified, null, tint = Color(0xFF3A6B35), modifier = Modifier.size(14.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val checks = listOf("Clean Bedding", "Safe Drinking Water", "Attached Bathroom")
        checks.forEach { check ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF3A6B35), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(check, color = Color.DarkGray)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun BookingForm(
    checkIn: Long?,
    checkOut: Long?,
    guests: Int,
    totalPrice: Int,
    onCheckInClick: () -> Unit,
    onCheckOutClick: () -> Unit,
    onIncrementGuests: () -> Unit,
    onDecrementGuests: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Plan Your Stay", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BookingField("Check-in", checkIn?.let { formatDate(it) } ?: "Select", Modifier.weight(1f), onCheckInClick)
            BookingField("Check-out", checkOut?.let { formatDate(it) } ?: "Select", Modifier.weight(1f), onCheckOutClick)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Number of Guests", fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrementGuests) { Icon(Icons.Default.Remove, null) }
                Text("$guests", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onIncrementGuests) { Icon(Icons.Default.Add, null) }
            }
        }
        
        if (totalPrice > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Price", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("₹$totalPrice", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFFC1440E))
            }
        }
    }
}

@Composable
fun BookingField(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clickable { onClick() }) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BookingBottomBar(price: Int, onBookClick: () -> Unit, isLoading: Boolean) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("₹$price", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("/ night", color = Color.Gray, fontSize = 12.sp)
            }
            
            Button(
                onClick = onBookClick,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1440E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp).width(200.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Request to Book", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StayDatePicker(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text("OK", color = Color(0xFFC1440E))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSuccessSheet(
    bookingId: String,
    onDismiss: () -> Unit,
    onViewBookings: () -> Unit,
    onBackToHome: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF3A6B35), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Booking Request Sent!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Unique Booking ID: $bookingId", color = Color.Gray, fontWeight = FontWeight.Medium)
            Text("Lakshmi will confirm within 24 hours", color = Color.Gray, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onViewBookings,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A6B35)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("View My Bookings")
            }
            
            TextButton(onClick = onBackToHome) {
                Text("Back to Home", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun formatDate(timeInMillis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timeInMillis))
}
