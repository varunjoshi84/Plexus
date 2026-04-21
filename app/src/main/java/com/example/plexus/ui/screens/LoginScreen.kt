package com.example.plexus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onContinue: (String) -> Unit = {}) {

    var phoneNumber by remember { mutableStateOf("") }

    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(40f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch {
            titleAlpha.animateTo(1f, tween(600))
            titleOffsetY.animateTo(0f, tween(600, easing = EaseOut))
        }
        launch {
            delay(200)
            cardAlpha.animateTo(1f, tween(700))
            cardOffsetY.animateTo(0f, tween(700, easing = EaseOut))
        }
    }

    PlexusBackground {
        PlexusRippleRings(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 60.dp),
            ringCount = 3,
            ringSize = 200
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
        ) {

            // Icon mark
            Box(
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffsetY.value.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(PlexusGradients.ambient, CircleShape)
                )
                PlexusLogo(size = 64.dp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffsetY.value.dp)
            ) {
                Text(
                    text = "Welcome to Plexus",
                    style = TextStyle(brush = PlexusGradients.cyanText),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your phone number to get started",
                    fontSize = 13.sp,
                    color = PlexusColors.TextMuted,
                    letterSpacing = 0.3.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Card
            Box(
                modifier = Modifier
                    .alpha(cardAlpha.value)
                    .offset(y = cardOffsetY.value.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PlexusColors.CardBg)
                    .border(1.dp, PlexusGradients.cardBorder, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Field label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PlexusColors.CyanGreen, CircleShape)
                        )
                        Text(
                            text = "PHONE NUMBER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = PlexusColors.CyanPrimary.copy(alpha = 0.7f)
                        )
                    }

                    // Input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 15) phoneNumber = it },
                        placeholder = {
                            Text(
                                "+91 XXXXX XXXXX",
                                color = PlexusColors.TextWhite.copy(alpha = 0.2f),
                                fontSize = 16.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = TextStyle(
                            color = PlexusColors.TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlexusColors.CyanPrimary,
                            unfocusedBorderColor = PlexusColors.CyanPrimary.copy(alpha = 0.25f),
                            cursorColor = PlexusColors.CyanLight,
                            focusedContainerColor = PlexusColors.CyanPrimary.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Button
                    val isEnabled = phoneNumber.length >= 10
                    Button(
                        onClick = { onContinue(phoneNumber) },
                        enabled = isEnabled,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isEnabled) PlexusGradients.cyanButton
                                    else PlexusGradients.cyanButtonDisabled,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CONTINUE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp,
                                color = if (isEnabled) PlexusColors.BgDark
                                else PlexusColors.TextWhite.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "We'll send an OTP to verify your number",
                fontSize = 11.sp,
                color = PlexusColors.TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.alpha(cardAlpha.value)
            )
        }
    }
}