package com.grama.vasathi.ui.host

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grama.vasathi.data.model.StayModel

private val Terracotta = Color(0xFFC1440E)
private val ForestGreen = Color(0xFF3A6B35)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddListingBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (StayModel) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // Step 1 fields
    var stayName by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Step 2 fields
    var roomCount by remember { mutableIntStateOf(1) }
    var maxGuests by remember { mutableIntStateOf(2) }
    var price by remember { mutableStateOf("") }
    var bathroomAttached by remember { mutableStateOf(true) }

    // Step 3 fields
    val presetActivities = listOf("🐄 Cow Milking", "🌾 Organic Farming", "🐦 Birdwatching", "🍳 Cooking Class", "🚜 Tractor Ride", "🎣 Fishing", "🌅 Sunrise Walk", "🌿 Herb Picking", "🏕️ Bonfire Night")
    val selectedActivities = remember { mutableStateListOf<String>() }
    var showCustomActivityDialog by remember { mutableStateOf(false) }
    var customActivity by remember { mutableStateOf("") }

    // Step 4 — photos (placeholder)
    val photoCount by remember { mutableIntStateOf(0) }

    var showSuccess by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            // Step Progress
            Text("Step $step of $totalSteps", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
            LinearProgressIndicator(
                progress = { step / totalSteps.toFloat() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = Terracotta
            )

            when (step) {
                1 -> StepBasicInfo(stayName, village, district, description,
                    onStayName = { stayName = it }, onVillage = { village = it },
                    onDistrict = { district = it }, onDescription = { description = it })
                2 -> StepRoomDetails(roomCount, maxGuests, price, bathroomAttached,
                    onRoomCount = { roomCount = it }, onMaxGuests = { maxGuests = it },
                    onPrice = { price = it }, onBathroom = { bathroomAttached = it })
                3 -> StepActivities(presetActivities, selectedActivities, onAddCustom = { showCustomActivityDialog = true })
                4 -> StepPhotos(photoCount)
                5 -> StepReview(stayName, village, price, selectedActivities, photoCount)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (step > 1) {
                    OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("← Back")
                    }
                }
                Button(
                    onClick = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            val listing = StayModel(
                                id = "", hostName = "", village = village, district = district,
                                activities = selectedActivities.toList(),
                                pricePerNight = price.toIntOrNull() ?: 0
                            )
                            onSubmit(listing)
                            showSuccess = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (step == totalSteps) ForestGreen else Terracotta),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(if (step == totalSteps) "Submit Listing" else "Next →") }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showCustomActivityDialog) {
        AlertDialog(
            onDismissRequest = { showCustomActivityDialog = false },
            title = { Text("Add Custom Activity") },
            text = {
                OutlinedTextField(value = customActivity, onValueChange = { customActivity = it }, label = { Text("Activity name") })
            },
            confirmButton = {
                Button(onClick = {
                    if (customActivity.isNotBlank()) selectedActivities.add(customActivity)
                    customActivity = ""
                    showCustomActivityDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showCustomActivityDialog = false }) { Text("Cancel") } }
        )
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("🎉 Listing Created!") },
            text = { Text("Now complete your checklist to go live.") },
            confirmButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)) { Text("Go to Checklist") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Do it Later") } }
        )
    }
}

@Composable
fun StepBasicInfo(stayName: String, village: String, district: String, description: String,
                  onStayName: (String) -> Unit, onVillage: (String) -> Unit,
                  onDistrict: (String) -> Unit, onDescription: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🏡 Basic Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(value = stayName, onValueChange = onStayName, label = { Text("Stay Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = village, onValueChange = onVillage, label = { Text("📍 Village Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = district, onValueChange = onDistrict, label = { Text("🗺️ District / Taluk") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = description, onValueChange = { if (it.length <= 200) onDescription(it) }, label = { Text("📝 Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
        Text("${description.length}/200", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.End))
    }
}

@Composable
fun StepRoomDetails(roomCount: Int, maxGuests: Int, price: String, bathroomAttached: Boolean,
                    onRoomCount: (Int) -> Unit, onMaxGuests: (Int) -> Unit,
                    onPrice: (String) -> Unit, onBathroom: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("🛏️ Room Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        CounterRow("Number of Rooms", roomCount, onDecrease = { if (roomCount > 1) onRoomCount(roomCount - 1) }, onIncrease = { onRoomCount(roomCount + 1) })
        CounterRow("Max Guests per Room", maxGuests, onDecrease = { if (maxGuests > 1) onMaxGuests(maxGuests - 1) }, onIncrease = { onMaxGuests(maxGuests + 1) })
        OutlinedTextField(value = price, onValueChange = onPrice, label = { Text("💰 Price per Night (₹)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Text("🛁 Bathroom Type", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(true to "Attached", false to "Shared").forEach { (value, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = bathroomAttached == value, onClick = { onBathroom(value) }, colors = RadioButtonDefaults.colors(selectedColor = Terracotta))
                    Text(label)
                }
            }
        }
    }
}

@Composable
fun CounterRow(label: String, value: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = onDecrease) { Icon(Icons.Default.Remove, null) }
            Text("$value", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = onIncrease) { Icon(Icons.Default.Add, null) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepActivities(presetActivities: List<String>, selected: MutableList<String>, onAddCustom: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🌾 Farm Activities", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("What can guests do at your farm?", color = Color.Gray)
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            presetActivities.forEach { activity ->
                FilterChip(
                    selected = selected.contains(activity),
                    onClick = { if (selected.contains(activity)) selected.remove(activity) else selected.add(activity) },
                    label = { Text(activity) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Terracotta, selectedLabelColor = Color.White)
                )
            }
        }
        TextButton(onClick = onAddCustom) { Text("+ Add Custom Activity", color = Terracotta) }
    }
}

@Composable
fun StepPhotos(photoCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("📷 Add Photos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Add photos of your home (minimum 2)", color = Color.Gray)
        Text("Photo upload via gallery/camera will be available after listing creation.", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun StepReview(stayName: String, village: String, price: String, activities: List<String>, photoCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("✅ Review & Submit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReviewRow("Stay Name", stayName.ifBlank { "Not set" })
                ReviewRow("Village", village.ifBlank { "Not set" })
                ReviewRow("Price/Night", if (price.isBlank()) "Not set" else "₹$price")
                ReviewRow("Activities", "${activities.size} selected")
            }
        }
        Text("💡 Complete your readiness checklist after submission to go fully live.", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ActivityPickerSheet(
    currentActivities: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf("🐄 Cow Milking", "🌾 Organic Farming", "🐦 Birdwatching", "🍳 Cooking Class", "🚜 Tractor Ride", "🎣 Fishing", "🌅 Sunrise Walk", "🌿 Herb Picking", "🏕️ Bonfire Night")
    val selected = remember { mutableStateListOf<String>().also { it.addAll(currentActivities) } }
    var customActivity by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Pick Activities", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { activity ->
                    FilterChip(
                        selected = selected.contains(activity),
                        onClick = { if (selected.contains(activity)) selected.remove(activity) else selected.add(activity) },
                        label = { Text(activity) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Terracotta, selectedLabelColor = Color.White)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = customActivity, onValueChange = { customActivity = it }, label = { Text("+ Custom Activity") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (customActivity.isNotBlank()) IconButton(onClick = { selected.add(customActivity); customActivity = "" }) { Icon(Icons.Default.Add, null) }
                }, shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onSave(selected.toList()) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ForestGreen), shape = RoundedCornerShape(12.dp)) {
                Text("Save Activities")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
