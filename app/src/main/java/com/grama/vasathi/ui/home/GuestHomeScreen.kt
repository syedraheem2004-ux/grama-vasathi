package com.grama.vasathi.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.grama.vasathi.data.model.*

private val Terracotta = Color(0xFFC1440E)
private val ForestGreen = Color(0xFF3A6B35)
private val Cream = Color(0xFFFAF3E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestHomeScreen(
    viewModel: GuestHomeViewModel = viewModel(),
    onStayClick: (String) -> Unit,
    onViewBookingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onExploreClick: () -> Unit
) {
    val profile by viewModel.guestProfile.collectAsState()
    val recommendedStays by viewModel.recommendedStays.collectAsState()
    val nearbyStays by viewModel.nearbyStays.collectAsState()
    val topRatedStays by viewModel.topRatedStays.collectAsState()
    val recentlyViewed by viewModel.recentlyViewed.collectAsState()
    val upcomingBooking by viewModel.upcomingBooking.collectAsState()
    val stats by viewModel.guestStats.collectAsState()
    val offers by viewModel.seasonalOffers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Good Morning, ${profile.fullName.ifBlank { "Guest" }} 👋", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Where do you want to escape today?", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        IconButton(onClick = { }) { Icon(Icons.Default.Notifications, null) }
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp).size(10.dp).background(Color.Red, CircleShape))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        bottomBar = {
            GuestBottomBar(onHomeClick = { }, onBookingsClick = onViewBookingsClick, onProfileClick = onProfileClick, onExploreClick = onExploreClick)
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().background(Cream).padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            // Search Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onExploreClick() },
                    shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Cream, shape = RoundedCornerShape(16.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Terracotta, modifier = Modifier.size(16.dp))
                                Text(" Bengaluru", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Terracotta)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Search villages, activities...", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Search, null, tint = Terracotta, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            }

            // Quick Stats
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatPill("🏡", "${stats.staysCompleted} Stays")
                    StatPill("❤️", "${stats.savedStays} Saved")
                    StatPill("⭐", "${stats.reviewsGiven} Reviews")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Quick Actions
            item {
                val actions = listOf("Explore" to Icons.Default.Search, "Bookings" to Icons.Default.ListAlt, "Wishlist" to Icons.Default.FavoriteBorder, "Activities" to Icons.Default.Agriculture, "Reviews" to Icons.Default.StarBorder, "Support" to Icons.Default.HelpOutline)
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    items(actions) { (label, icon) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                            when(label) {
                                "Explore", "Activities", "Wishlist" -> onExploreClick()
                                "Bookings" -> onViewBookingsClick()
                                "Reviews", "Support" -> onProfileClick()
                            }
                        }) {
                            Box(modifier = Modifier.size(56.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(icon, null, tint = Terracotta)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Personalized Banner
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp)) {
                    Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(Terracotta, ForestGreen))).padding(20.dp)) {
                        Column {
                            Text("✨ Start your first farm adventure!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onExploreClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Terracotta), shape = RoundedCornerShape(12.dp)) {
                                Text("Explore Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Upcoming Booking
            upcomingBooking?.let { booking ->
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = ForestGreen), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🗓️ Your next stay is soon!", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Check-in: ${booking.checkIn}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                            OutlinedButton(onClick = onViewBookingsClick, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(8.dp)) {
                                Text("View Details")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // AI Recommendations
            if (recommendedStays.isNotEmpty()) {
                item {
                    SectionHeader("✨ Recommended for You", "Based on your love for farming")
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(recommendedStays) { stay -> RecommendationCard(stay, viewModel.isWishlisted(stay.id), onStayClick) { viewModel.toggleWishlist(stay.id) } }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Browse by Activity
            item {
                SectionHeader("🌾 Browse by Activity")
                val activities = listOf("Cow Milking" to "🐄", "Farming" to "🌾", "Birdwatching" to "🐦", "Cooking" to "🍳", "Tractor" to "🚜", "Fishing" to "🎣", "Sunrise" to "🌅", "Bonfire" to "🔥")
                Column(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { activities.take(4).forEach { ActivityCard(it.first, it.second, onExploreClick) } }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { activities.drop(4).forEach { ActivityCard(it.first, it.second, onExploreClick) } }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Nearby Stays
            if (nearbyStays.isNotEmpty()) {
                item {
                    SectionHeader("📍 Stays Near Bengaluru", showSeeAll = true, onSeeAll = onExploreClick)
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(nearbyStays) { stay -> StayThumbnailCard(stay, viewModel.isWishlisted(stay.id), onStayClick) { viewModel.toggleWishlist(stay.id) } }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Seasonal Offer
            if (offers.isNotEmpty()) {
                item {
                    val offer = offers.first()
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🌿 ${offer.title}", fontWeight = FontWeight.Bold, color = Terracotta)
                                Text(offer.description, fontSize = 12.sp, color = Color.DarkGray)
                            }
                            Button(onClick = onExploreClick, colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text("Explore", fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Top Rated Stays
            if (topRatedStays.isNotEmpty()) {
                item {
                    SectionHeader("⭐ Top Rated Farm Stays", showSeeAll = true, onSeeAll = onExploreClick)
                }
                items(topRatedStays) { stay ->
                    TopRatedStayCard(stay, onStayClick)
                }
            }

            // Recently Viewed
            if (recentlyViewed.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("🕐 Recently Viewed", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Clear", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.clickable { viewModel.clearRecentlyViewed() })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(recentlyViewed) { recent -> RecentlyViewedCard(recent, onStayClick) }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(icon: String, text: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 1.dp) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Terracotta)
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, showSeeAll: Boolean = false, onSeeAll: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        if (showSeeAll) Text("See All", color = Terracotta, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onSeeAll() })
    }
}

@Composable
fun RecommendationCard(stay: StayModel, isWishlisted: Boolean, onClick: (String) -> Unit, onWishlist: () -> Unit) {
    var heartScale by remember { mutableStateOf(1f) }
    val animScale by animateFloatAsState(heartScale, spring(), finishedListener = { heartScale = 1f }, label = "heart")

    Card(modifier = Modifier.width(260.dp).height(180.dp).clickable { onClick(stay.id) }, shape = RoundedCornerShape(16.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(model = stay.imageUrl.ifBlank { null }, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
            
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).background(Terracotta, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("95% Match", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(stay.village, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(14.dp))
                    Text(" ${stay.rating} · ₹${stay.pricePerNight}/night", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                }
            }

            IconButton(onClick = { heartScale = 1.3f; onWishlist() }, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).scale(animScale)) {
                Icon(if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isWishlisted) Color.Red else Color.White)
            }
        }
    }
}

@Composable
fun StayThumbnailCard(stay: StayModel, isWishlisted: Boolean, onClick: (String) -> Unit, onWishlist: () -> Unit) {
    Card(modifier = Modifier.width(160.dp).clickable { onClick(stay.id) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            Box {
                AsyncImage(model = stay.imageUrl.ifBlank { null }, contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                IconButton(onClick = onWishlist, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    Icon(if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isWishlisted) Color.Red else Color.White)
                }
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(6.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("~120 km", color = Color.White, fontSize = 9.sp)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text("${stay.hostName}'s Farm", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stay.village, fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(10.dp))
                    Text(" ${stay.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("₹${stay.pricePerNight}", color = Terracotta, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ActivityCard(name: String, icon: String, onClick: () -> Unit) {
    Card(modifier = Modifier.width(120.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("12 stays", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun TopRatedStayCard(stay: StayModel, onClick: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onClick(stay.id) }, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${stay.hostName}'s Farm", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${stay.village}, ${stay.district}", fontSize = 11.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(12.dp))
                    Text(" ${stay.rating} (120)", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.background(ForestGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                        Text("✅ Clean", color = ForestGreen, fontSize = 9.sp)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${stay.pricePerNight}/night", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedButton(onClick = { onClick(stay.id) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp), shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Terracotta), colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta)) {
                        Text("View", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentlyViewedCard(recent: RecentlyViewedModel, onClick: (String) -> Unit) {
    Card(modifier = Modifier.width(140.dp).clickable { onClick(recent.stayId) }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(recent.stayName, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("₹${recent.pricePerNight}", color = Terracotta, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun GuestBottomBar(onHomeClick: () -> Unit, onBookingsClick: () -> Unit, onProfileClick: () -> Unit, onExploreClick: () -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = true, onClick = onHomeClick, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = onExploreClick, icon = { Icon(Icons.Default.Explore, null) }, label = { Text("Explore") })
        NavigationBarItem(selected = false, onClick = onBookingsClick, icon = { Icon(Icons.Default.ListAlt, null) }, label = { Text("Bookings") })
        NavigationBarItem(selected = false, onClick = onProfileClick, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
    }
}
