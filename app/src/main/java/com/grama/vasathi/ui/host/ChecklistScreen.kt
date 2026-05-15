package com.grama.vasathi.ui.host

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.vasathi.data.model.ChecklistItem
import com.grama.vasathi.data.model.ChecklistSection
import com.grama.vasathi.data.model.ReadinessStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel = viewModel(),
    onBackClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    val sections by viewModel.sections.collectAsState()
    val progress by viewModel.overallProgress.collectAsState()
    val status by viewModel.readinessStatus.collectAsState()
    
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Host Readiness Checklist", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Complete all sections to unlock your listing", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF3E0))
            )
        },
        bottomBar = {
            ChecklistBottomBar(
                isComplete = status == ReadinessStatus.GUEST_READY,
                onSubmit = { showSuccessDialog = true }
            )
        }
    ) { padding ->
        if (sections.isEmpty()) {
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
                item { ProgressSection(progress, status) }
                
                items(sections) { section ->
                    ExpandableChecklistSection(section) { itemId, isChecked ->
                        viewModel.toggleItem(itemId, isChecked)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(onDashboardClick)
    }
}

@Composable
fun ProgressSection(progress: Float, status: ReadinessStatus) {
    val animatedProgress by animateFloatAsState(targetValue = progress)
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(140.dp)) {
                drawArc(
                    color = Color(0xFFFAF3E0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFFC1440E),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFC1440E)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("${(progress * 17).toInt()} of 17 items completed", color = Color.Gray)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        StatusBadge(status)
    }
}

@Composable
fun StatusBadge(status: ReadinessStatus) {
    val (color, label) = when (status) {
        ReadinessStatus.NOT_READY -> Color.Red to "Not Ready"
        ReadinessStatus.ALMOST_READY -> Color(0xFFFFB400) to "Almost Ready"
        ReadinessStatus.GUEST_READY -> Color(0xFF3A6B35) to "Guest Ready!"
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
fun ExpandableChecklistSection(section: ChecklistSection, onToggle: (String, Boolean) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val allChecked = section.items.all { it.isChecked }
    
    Card(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allChecked) Color(0xFF3A6B35).copy(alpha = 0.05f) else Color.White
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(section.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(section.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                
                val checkedCount = section.items.count { it.isChecked }
                Surface(
                    color = if (allChecked) Color(0xFF3A6B35) else Color(0xFFFAF3E0),
                    shape = CircleShape
                ) {
                    Text(
                        "$checkedCount/${section.items.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allChecked) Color.White else Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = Color.Gray
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    section.items.forEach { item ->
                        ChecklistItemRow(item, onToggle)
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistItemRow(item: ChecklistItem, onToggle: (String, Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (item.isChecked) Color(0xFF3A6B35).copy(alpha = 0.05f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle(item.itemId, it) },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFC1440E))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(item.label, modifier = Modifier.weight(1f), fontSize = 14.sp)
        IconButton(onClick = { }) {
            Icon(Icons.Default.PhotoCamera, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ChecklistBottomBar(isComplete: Boolean, onSubmit: () -> Unit) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Last saved: Today, 10:32 AM", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isComplete) Color(0xFF3A6B35) else Color(0xFFC1440E)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isComplete) "Submit & Activate Listing" else "Save Progress")
            }
        }
    }
}

@Composable
fun SuccessDialog(onDashboardClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(
                onClick = onDashboardClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A6B35))
            ) {
                Text("Go to Dashboard")
            }
        },
        title = { Text("Your home is Guest Ready! 🎉") },
        text = { Text("Your listing is now visible to travelers across Karnataka.") },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
