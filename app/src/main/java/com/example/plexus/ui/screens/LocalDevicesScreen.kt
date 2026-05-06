package com.example.plexus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.data.local.DiscoveredDevice
import com.example.plexus.data.local.LocalChatClient.ConnectionState
import com.example.plexus.ui.theme.*

@Composable
fun LocalDevicesScreen(
    discoveredDevices: List<DiscoveredDevice> = emptyList(),
    connectionState: ConnectionState = ConnectionState.Disconnected,
    onDeviceClick: (DiscoveredDevice) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Rotating animation for scan indicator
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "rotate"
    )

    PlexusBackground {

        Column(modifier = Modifier.fillMaxSize()) {

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
                    text = "Local Network",
                    style = TextStyle(brush = PlexusGradients.cyanText),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Scan indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring rotating
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(rotation)
                        .border(
                            2.dp,
                            PlexusGradients.cyanButton,
                            CircleShape
                        )
                )
                // Inner static ring
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(
                            1.dp,
                            PlexusColors.PurplePrimary.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
                // Center logo
                PlexusLogo(size = 56.dp)

                // Connection status text
                Text(
                    text = when (connectionState) {
                        is ConnectionState.Connected -> "Connected"
                        is ConnectionState.Connecting -> "Connecting..."
                        is ConnectionState.Error -> "Error"
                        else -> "Scanning..."
                    },
                    fontSize = 11.sp,
                    color = when (connectionState) {
                        is ConnectionState.Connected -> PlexusColors.CyanGreen
                        is ConnectionState.Error -> Color.Red
                        else -> PlexusColors.TextMuted
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 20.dp)
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(1.dp)
                    .background(PlexusColors.PurplePrimary.copy(alpha = 0.15f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section label
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(PlexusColors.CyanGreen, CircleShape)
                )
                Text(
                    text = "NEARBY DEVICES (${discoveredDevices.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = PlexusColors.CyanPrimary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (discoveredDevices.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📡", fontSize = 40.sp)
                        Text(
                            text = "Searching for nearby devices...",
                            fontSize = 13.sp,
                            color = PlexusColors.TextMuted
                        )
                        Text(
                            text = "Make sure others are on the same WiFi",
                            fontSize = 11.sp,
                            color = PlexusColors.TextMuted.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(discoveredDevices) { device ->
                        DeviceCard(
                            device = device,
                            onClick = { onDeviceClick(device) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: DiscoveredDevice,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PlexusColors.CardBg)
            .border(1.dp, PlexusGradients.cardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PlexusGradients.cyanButton),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = device.name.first().uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PlexusColors.BgDark
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name.removePrefix("PlexusChat-"),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = PlexusColors.TextWhite
            )
            Text(
                text = "${device.host}:${device.port}",
                fontSize = 11.sp,
                color = PlexusColors.TextMuted
            )
        }

        // LOCAL tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PlexusColors.CyanGreen.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "LOCAL",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = PlexusColors.CyanGreen
            )
        }

        Text("›", fontSize = 20.sp, color = PlexusColors.TextMuted)
    }
}