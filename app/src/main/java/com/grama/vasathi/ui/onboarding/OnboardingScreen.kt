package com.grama.vasathi.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onGetStarted: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            title = "Welcome to Grama-Vasathi",
            description = "Discover authentic rural stays and farm experiences across Karnataka's villages",
            illustration = "🏠",
            color = MaterialTheme.colorScheme.primaryContainer
        ),
        OnboardingPageData(
            title = "List Your Home, Earn More",
            description = "Complete a simple readiness checklist and start welcoming city guests to your farm",
            illustration = "👩‍🌾",
            color = MaterialTheme.colorScheme.secondaryContainer
        ),
        OnboardingPageData(
            title = "Explore. Book. Experience.",
            description = "Find verified stays filtered by activities like cow milking, organic farming, and birdwatching",
            illustration = "🚜",
            color = MaterialTheme.colorScheme.tertiaryContainer
        )
    )

    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val currentPage by viewModel.currentPage.collectAsState()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                OnboardingPagerItem(page = pages[index])
            }

            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dot Indicators
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(pages.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (currentPage == index) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Next / Get Started Button
                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        } else {
                            onGetStarted()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (currentPage < pages.size - 1) "Next" else "Get Started",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Skip Button (Top Right)
        if (currentPage < pages.size - 1) {
            TextButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pages.size - 1)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OnboardingPagerItem(page: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(32.dp)),
            color = page.color.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = page.illustration,
                    fontSize = 100.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val illustration: String,
    val color: Color
)
