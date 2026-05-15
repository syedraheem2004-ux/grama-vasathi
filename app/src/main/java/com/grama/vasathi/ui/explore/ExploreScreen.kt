package com.grama.vasathi.ui.explore

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = viewModel(),
    onStayClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val filteredStays by viewModel.filteredStays.collectAsState(initial = emptyList())
    val wishlistedStays by viewModel.wishlistedStays.collectAsState(initial = emptyList())
    val recommendedStays by viewModel.recommendedStays.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val activeFilters by viewModel.activeFilters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var peekStay by remember { mutableStateOf<StayModel?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Explore Stays", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSortSheet = true }) { Icon(Icons.Default.Sort, null) }
                    IconButton(onClick = { viewModel.setViewMode(if (viewMode == ViewMode.GRID) ViewMode.MAP else ViewMode.GRID) }) {
                        Icon(if (viewMode == ViewMode.GRID) Icons.Default.Map else Icons.Default.GridView, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(false, onHomeClick, { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(true, {}, { Icon(Icons.Default.Explore, null) }, label = { Text("Explore") })
                NavigationBarItem(false, onBookingsClick, { Icon(Icons.Default.ListAlt, null) }, label = { Text("Bookings") })
                NavigationBarItem(false, onProfileClick, { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Cream).padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search village, district, activity...") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (searchQuery.isNotBlank()) IconButton(onClick = { viewModel.onSearchQueryChanged("") }) { Icon(Icons.Default.Clear, null) } },
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Terracotta, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                singleLine = true
            )

            // Quick filter chips + Advanced Filters
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                val quickFilters = listOf("📍 Near Me", "💰 Under ₹1,000", "⭐ Top Rated", "🆕 Newly Added", "🐄 Cow Milking", "🌾 Farming")
                LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickFilters) { chip ->
                        val isSelected = when (chip) {
                            "💰 Under ₹1,000" -> activeFilters.maxPrice <= 1000
                            "⭐ Top Rated" -> activeFilters.minRating >= 4f
                            else -> false
                        }
                        FilterChip(selected = isSelected, onClick = {
                            when (chip) {
                                "💰 Under ₹1,000" -> viewModel.applyFilters(activeFilters.copy(maxPrice = 1000))
                                "⭐ Top Rated" -> viewModel.applyFilters(activeFilters.copy(minRating = 4f))
                            }
                        }, label = { Text(chip, fontSize = 12.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Terracotta, selectedLabelColor = Color.White))
                    }
                }
                TextButton(onClick = { showFilterSheet = true }) { Text("Filters", color = Terracotta, fontSize = 12.sp) }
            }

            // Explore / Wishlist segmented control
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.Center) {
                listOf(ExploreTab.EXPLORE to "Explore", ExploreTab.WISHLIST to "Wishlist ❤️").forEach { (tab, label) ->
                    val selected = activeTab == tab
                    Button(
                        onClick = { viewModel.setActiveTab(tab) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (selected) Terracotta else Color.White, contentColor = if (selected) Color.White else Color.Gray),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) { Text(label, fontSize = 13.sp) }
                }
            }

            if (activeTab == ExploreTab.EXPLORE) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Terracotta)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // AI Recommendations
                        if (recommendedStays.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                AIRecommendationBanner(recommendedStays, onStayClick)
                            }
                        }

                        items(filteredStays) { stay ->
                            ExploreStayCard(
                                stay = stay,
                                isWishlisted = viewModel.isWishlisted(stay.id),
                                onStayClick = { onStayClick(stay.id) },
                                onWishlistToggle = { viewModel.toggleWishlist(stay.id) }
                            )
                        }

                        if (filteredStays.isEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🌾", fontSize = 48.sp)
                                    Text("No stays match your filters", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                                    TextButton(onClick = { viewModel.applyFilters(ExploreFilter()) }) { Text("Reset Filters", color = Terracotta) }
                                }
                            }
                        }
                    }
                }
            } else {
                WishlistView(wishlistedStays, onStayClick, onRemove = { viewModel.toggleWishlist(it) }, onExplore = { viewModel.setActiveTab(ExploreTab.EXPLORE) })
            }
        }
    }

    if (showFilterSheet) {
        AdvancedFilterSheet(currentFilter = activeFilters, onApply = { viewModel.applyFilters(it); showFilterSheet = false }, onDismiss = { showFilterSheet = false })
    }

    if (showSortSheet) {
        SortSheet(current = activeFilters.sortOrder, onApply = { viewModel.applyFilters(activeFilters.copy(sortOrder = it)); showSortSheet = false }, onDismiss = { showSortSheet = false })
    }

    peekStay?.let { stay ->
        QuickPeekSheet(stay = stay, isWishlisted = viewModel.isWishlisted(stay.id), onViewDetails = { onStayClick(stay.id); peekStay = null }, onWishlistToggle = { viewModel.toggleWishlist(stay.id) }, onDismiss = { peekStay = null })
    }
}

@Composable
fun ExploreStayCard(stay: StayModel, isWishlisted: Boolean, onStayClick: () -> Unit, onWishlistToggle: () -> Unit) {
    var heartScale by remember { mutableStateOf(1f) }
    val animScale by animateFloatAsState(heartScale, spring(), finishedListener = { heartScale = 1f }, label = "heart")

    Card(modifier = Modifier.fillMaxWidth().clickable { onStayClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Box {
                AsyncImage(model = stay.imageUrl.ifBlank { null }, contentDescription = null, modifier = Modifier.fillMaxWidth().height(130.dp), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxWidth().height(130.dp).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)))))
                IconButton(onClick = { heartScale = 1.3f; onWishlistToggle() }, modifier = Modifier.align(Alignment.TopEnd).scale(animScale)) {
                    Icon(if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isWishlisted) Color.Red else Color.White)
                }
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).background(ForestGreen.copy(alpha = 0.9f), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("${(stay.rating * 20).toInt()}% Match", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text("${stay.hostName}'s Farm", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stay.village, fontSize = 11.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(12.dp))
                    Text(" ${stay.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(" · Hygiene ${stay.hygieneScore}/5", fontSize = 10.sp, color = Color.Gray)
                }
                if (stay.activities.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stay.activities.take(1).joinToString(), fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                        if (stay.activities.size > 1) Text("+${stay.activities.size - 1} more", fontSize = 9.sp, color = Terracotta)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${stay.pricePerNight}/night", fontWeight = FontWeight.ExtraBold, color = Terracotta, fontSize = 12.sp)
                    Button(onClick = onStayClick, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(8.dp)) {
                        Text("Book", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AIRecommendationBanner(stays: List<StayModel>, onStayClick: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(ForestGreen, Terracotta))).padding(16.dp)) {
            Column {
                Text("✨ Recommended for You", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Based on your interest in farming and birdwatching", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(stays.take(3)) { stay ->
                        Card(modifier = Modifier.width(140.dp).clickable { onStayClick(stay.id) }, shape = RoundedCornerShape(12.dp)) {
                            Column {
                                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color.LightGray))
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(stay.village, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                    Text("₹${stay.pricePerNight}/night", color = Terracotta, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistView(stays: List<StayModel>, onStayClick: (String) -> Unit, onRemove: (String) -> Unit, onExplore: () -> Unit) {
    if (stays.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("🤍", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No saved stays yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Tap the ❤️ on any stay to save it here", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onExplore, colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(12.dp)) { Text("Start Exploring") }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Text("Your Saved Stays ❤️", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
            Text("${stays.size} stays saved", color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(stays, key = { it.id }) { stay ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${stay.hostName}'s Farm", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${stay.village}, ${stay.district}", fontSize = 12.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(12.dp))
                                    Text(" ${stay.rating} · ₹${stay.pricePerNight}/night", fontSize = 11.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Button(onClick = { onStayClick(stay.id) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(8.dp)) { Text("Book Now", fontSize = 11.sp) }
                                    IconButton(onClick = { onRemove(stay.id) }) { Icon(Icons.Default.Favorite, null, tint = Color.Red) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterSheet(currentFilter: ExploreFilter, onApply: (ExploreFilter) -> Unit, onDismiss: () -> Unit) {
    var minPrice by remember { mutableFloatStateOf(currentFilter.minPrice.toFloat()) }
    var maxPrice by remember { mutableFloatStateOf(currentFilter.maxPrice.toFloat()) }
    var minRating by remember { mutableFloatStateOf(currentFilter.minRating) }
    var district by remember { mutableStateOf(currentFilter.district) }
    var guestCount by remember { mutableIntStateOf(currentFilter.guestCount) }
    val districts = listOf("All", "Mandya", "Mysuru", "Hassan", "Coorg", "Chikkamagaluru")

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Advanced Filters", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text("💰 Price Range: ₹${minPrice.toInt()} - ₹${maxPrice.toInt()}", fontWeight = FontWeight.Medium)
            RangeSlider(value = minPrice..maxPrice, onValueChange = { minPrice = it.start; maxPrice = it.endInclusive }, valueRange = 500f..5000f, colors = SliderDefaults.colors(thumbColor = Terracotta, activeTrackColor = Terracotta))

            Spacer(modifier = Modifier.height(12.dp))
            Text("⭐ Minimum Rating: ${minRating.toInt()}+", fontWeight = FontWeight.Medium)
            Slider(value = minRating, onValueChange = { minRating = it }, valueRange = 0f..5f, steps = 4, colors = SliderDefaults.colors(thumbColor = Terracotta, activeTrackColor = Terracotta))

            Spacer(modifier = Modifier.height(12.dp))
            Text("🏘️ District", fontWeight = FontWeight.Medium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                items(districts) { d ->
                    FilterChip(selected = district == d, onClick = { district = d }, label = { Text(d) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Terracotta, selectedLabelColor = Color.White))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("👥 Guest Count: $guestCount", fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (guestCount > 1) guestCount-- }) { Icon(Icons.Default.Remove, null) }
                    Text("$guestCount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { if (guestCount < 10) guestCount++ }) { Icon(Icons.Default.Add, null) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onApply(currentFilter.copy(minPrice = minPrice.toInt(), maxPrice = maxPrice.toInt(), minRating = minRating, district = district, guestCount = guestCount)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(12.dp)) { Text("Apply Filters") }
            TextButton(onClick = { onApply(ExploreFilter()) }, modifier = Modifier.fillMaxWidth()) { Text("Reset All", color = Color.Gray) }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(current: ExploreSortOrder, onApply: (ExploreSortOrder) -> Unit, onDismiss: () -> Unit) {
    var selected by remember { mutableStateOf(current) }
    val options = listOf(ExploreSortOrder.NEWEST_FIRST to "🆕 Newest First", ExploreSortOrder.HIGHEST_RATED to "⭐ Highest Rated", ExploreSortOrder.PRICE_LOW_TO_HIGH to "💰 Price: Low to High", ExploreSortOrder.PRICE_HIGH_TO_LOW to "💰 Price: High to Low", ExploreSortOrder.NEAREST to "📍 Nearest to Me")
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Sort By", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            options.forEach { (order, label) ->
                Row(modifier = Modifier.fillMaxWidth().clickable { selected = order }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == order, onClick = { selected = order }, colors = RadioButtonDefaults.colors(selectedColor = Terracotta))
                    Text(label, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onApply(selected) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(12.dp)) { Text("Apply Sort") }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPeekSheet(stay: StayModel, isWishlisted: Boolean, onViewDetails: () -> Unit, onWishlistToggle: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray))
            Spacer(modifier = Modifier.height(16.dp))
            Text("${stay.hostName}'s Farm · ${stay.village}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                Text(" ${stay.rating} · Hygiene ${stay.hygieneScore}/5", fontSize = 13.sp, color = Color.Gray)
            }
            if (stay.activities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stay.activities) { Text(it, modifier = Modifier.background(Cream, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 12.sp) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(progress = { stay.hygieneScore / 5f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = ForestGreen)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onWishlistToggle, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Icon(if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isWishlisted) Color.Red else Color.Gray)
                    Text(if (isWishlisted) " Remove" else " Wishlist")
                }
                Button(onClick = onViewDetails, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Terracotta), shape = RoundedCornerShape(12.dp)) {
                    Text("View Details · ₹${stay.pricePerNight}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
