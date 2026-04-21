package com.example.plexus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.R
import com.example.plexus.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onSplashComplete: () -> Unit = {}) {

    val logoScale = remember { Animatable(0.3f) }
    val logoAlpha = remember { Animatable(0f) }
    val brandAlpha = remember { Animatable(0f) }
    val brandOffsetY = remember { Animatable(20f) }
    val badgesAlpha = remember { Animatable(0f) }
    val loadingAlpha = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }
    val labelAlpha = remember { Animatable(1f) }
    var labelIndex by remember { mutableStateOf(0) }

    val loadingLabels = listOf(
        "Initializing network...",
        "Discovering local peers...",
        "Securing channels...",
        "Ready to connect"
    )

    val infinite = rememberInfiniteTransition(label = "splash")
    val logoFloatY by infinite.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "float"
    )

    LaunchedEffect(Unit) {
        launch {
            delay(200)
            logoAlpha.animateTo(1f, tween(400))
            logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        }
        launch {
            delay(800)
            brandAlpha.animateTo(1f, tween(600))
            brandOffsetY.animateTo(0f, tween(600, easing = EaseOut))
        }
        launch {
            delay(1400)
            badgesAlpha.animateTo(1f, tween(500))
        }
        launch {
            delay(1800)
            loadingAlpha.animateTo(1f, tween(400))
            progressAnim.animateTo(1f, tween(2500, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
            delay(500)
            onSplashComplete()
        }
        launch {
            delay(2000)
            repeat(3) {
                delay(1800)
                labelAlpha.animateTo(0f, tween(300))
                labelIndex = (labelIndex + 1) % loadingLabels.size
                labelAlpha.animateTo(1f, tween(300))
            }
        }
    }

    PlexusBackground {
        PlexusRippleRings(
            modifier = Modifier.align(Alignment.Center),
            ringCount = 4,
            ringSize = 300
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .offset(y = logoFloatY.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            PlexusGradients.ambient,
                            CircleShape
                        )
                )
                PlexusLogo(size = 120.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(brandAlpha.value)
                    .offset(y = brandOffsetY.value.dp)
            ) {
                Text(
                    text = "PLEXUS",
                    style = TextStyle(brush = PlexusGradients.cyanText),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CONNECTED. EVERYWHERE.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 4.sp,
                    color = PlexusColors.CyanPrimary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.alpha(badgesAlpha.value)
            ) {
                PlexusStatusBadge(label = "Local Network", dotColor = PlexusColors.CyanGreen)
                PlexusStatusBadge(label = "Internet", dotColor = PlexusColors.CyanLight)
            }
        }

        // Loading bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(loadingAlpha.value)
        ) {
            Text(
                text = loadingLabels[labelIndex],
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = PlexusColors.CyanPrimary.copy(alpha = 0.5f * labelAlpha.value),
                modifier = Modifier.alpha(labelAlpha.value)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PlexusColors.CyanPrimary.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressAnim.value)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PlexusGradients.cyanButton)
                )
            }
        }
    }
}