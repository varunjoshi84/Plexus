package com.example.plexus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.ui.theme.*

@Composable
fun ProfileScreen(
    userName: String = "Varun",
    phoneNumber: String = "+91 XXXXX XXXXX",
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onLocalNetworkClick: () -> Unit = {},  // ← ADD
    onInternetModeClick: () -> Unit = {},  // ← ADD
    onEditProfileClick: () -> Unit = {},   // ← ADD
    onNotificationsClick: () -> Unit = {}  // ← ADD
) {
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(600))
        contentOffsetY.animateTo(0f, tween(600, easing = EaseOut))
    }

    PlexusBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .alpha(contentAlpha.value)
                .offset(y = contentOffsetY.value.dp)
        ) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PlexusColors.PurplePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 18.sp, color = PlexusColors.CyanPrimary)
                    }
                }
                Text(
                    text = "Profile",
                    style = TextStyle(brush = PlexusGradients.cyanText),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PlexusColors.PurplePrimary.copy(alpha = 0.15f))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Avatar section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        PlexusColors.PurplePrimary,
                                        PlexusColors.CyanPrimary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = PlexusColors.BgDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PlexusColors.CardBg)
                            .border(1.dp, PlexusColors.CyanPrimary.copy(alpha = 0.4f), CircleShape)
                            .clickable { onEditProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✎", fontSize = 12.sp, color = PlexusColors.CyanPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PlexusColors.TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phoneNumber,
                    fontSize = 14.sp,
                    color = PlexusColors.TextMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlexusStatusBadge(label = "Local", dotColor = PlexusColors.CyanGreen)
                    PlexusStatusBadge(label = "Internet", dotColor = PlexusColors.CyanPrimary)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Settings list
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileSectionLabel("ACCOUNT")
                ProfileItem(
                    icon = "👤",
                    title = "Edit Profile",
                    subtitle = "Name, avatar",
                    onClick = onEditProfileClick
                )
                ProfileItem(
                    icon = "🔔",
                    title = "Notifications",
                    subtitle = "Sounds, alerts",
                    onClick = onNotificationsClick
                )

                Spacer(modifier = Modifier.height(4.dp))
                ProfileSectionLabel("NETWORK")
                ProfileItem(
                    icon = "📡",
                    title = "Local Network",
                    subtitle = "Discover nearby devices",
                    onClick = onLocalNetworkClick  // ← clean callback, no navController here
                )
                ProfileItem(
                    icon = "🌐",
                    title = "Internet Mode",
                    subtitle = "Connect via server",
                    onClick = onInternetModeClick
                )

                Spacer(modifier = Modifier.height(4.dp))
                ProfileSectionLabel("APP")
                ProfileItem(icon = "🔒", title = "Privacy", subtitle = "End-to-end encryption")
                ProfileItem(icon = "🎨", title = "Appearance", subtitle = "Theme settings")
                ProfileItem(icon = "ℹ️", title = "About Plexus", subtitle = "Version 1.0.0")

                Spacer(modifier = Modifier.height(16.dp))

                // Logout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFF4444).copy(alpha = 0.08f))
                        .border(1.dp, Color(0xFFFF4444).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { onLogout() }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log Out",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF6666),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun ProfileSectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        color = PlexusColors.CyanPrimary.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun ProfileItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}  // ← ADD default empty lambda
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PlexusColors.CardBg)
            .border(1.dp, PlexusColors.PurplePrimary.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable { onClick() }  // ← now uses the parameter
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PlexusColors.TextWhite)
            Text(text = subtitle, fontSize = 11.sp, color = PlexusColors.TextMuted)
        }
        Text("›", fontSize = 18.sp, color = PlexusColors.TextMuted)
    }
}