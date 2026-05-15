package com.grama.vasathi.ui.roleselection

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RoleSelectionScreen(
    viewModel: RoleSelectionViewModel = viewModel(),
    onContinue: (UserRole) -> Unit
) {
    val selectedRole by viewModel.selectedRole.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // App Logo / Name
        Text(
            text = "Grama-Vasathi",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFC1440E),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Who are you today?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Role Cards
        RoleCard(
            title = "I'm a Host",
            subtitle = "List my home and welcome guests",
            icon = "🏡",
            isSelected = selectedRole == UserRole.HOST,
            selectedColor = Color(0xFF3A6B35),
            onClick = { viewModel.selectRole(UserRole.HOST) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoleCard(
            title = "I'm a Guest",
            subtitle = "Explore farm stays and book experiences",
            icon = "🎒",
            isSelected = selectedRole == UserRole.GUEST,
            selectedColor = Color(0xFFC1440E),
            onClick = { viewModel.selectRole(UserRole.GUEST) }
        )

        Spacer(modifier = Modifier.weight(1.2f))

        // Continue Button
        Button(
            onClick = { selectedRole?.let { onContinue(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedRole != null,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC1440E),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFC1440E).copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")
    
    val borderColor by animateColorAsState(
        if (isSelected) selectedColor else Color.Transparent,
        label = "border"
    )
    
    val backgroundColor by animateColorAsState(
        if (isSelected) selectedColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        label = "background"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) selectedColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
