package com.example.plexus.ui.screens
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OtpScreen(
    phoneNumber: String = "",
    onVerified: (String) -> Unit = {},  // ← now passes OTP code
    onResend: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(40f) }
    val titleAlpha = remember { Animatable(0f) }

    var timerValue by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, tween(600)) }
        launch {
            kotlinx.coroutines.delay(200)
            cardAlpha.animateTo(1f, tween(700))
            cardOffsetY.animateTo(0f, tween(700, easing = EaseOut))
            focusRequesters[0].requestFocus()
        }
        launch {
            while (timerValue > 0) {
                kotlinx.coroutines.delay(1000)
                timerValue--
            }
            canResend = true
        }
    }

    PlexusBackground {
        PlexusRippleRings(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 60.dp),
            ringCount = 3,
            ringSize = 200
        )

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .alpha(titleAlpha.value)
        ) {
            Text("←", fontSize = 24.sp, color = PlexusColors.CyanPrimary)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
        ) {

            // Logo
            PlexusLogo(
                size = 64.dp,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(titleAlpha.value)
            ) {
                Text(
                    text = "Verify Number",
                    style = TextStyle(brush = PlexusGradients.cyanText),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter the 6-digit OTP sent to",
                    fontSize = 13.sp,
                    color = PlexusColors.TextMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = phoneNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PlexusColors.CyanPrimary,
                    letterSpacing = 1.sp
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
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // Label
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
                            text = "ONE TIME PASSWORD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = PlexusColors.CyanPrimary.copy(alpha = 0.7f)
                        )
                    }

                    // OTP boxes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        otpValues.forEachIndexed { index, value ->
                            OtpBox(
                                value = value,
                                focusRequester = focusRequesters[index],
                                onValueChange = { newVal ->
                                    val digit = newVal.filter { it.isDigit() }
                                    when {
                                        // typed a digit
                                        digit.isNotEmpty() -> {
                                            otpValues[index] = digit.last().toString()
                                            if (index < 5) focusRequesters[index + 1].requestFocus()
                                        }
                                        // deleted
                                        newVal.isEmpty() -> {
                                            otpValues[index] = ""
                                            if (index > 0) focusRequesters[index - 1].requestFocus()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Verify button
                    val isComplete = otpValues.all { it.isNotEmpty() }
                    Button(
                        onClick = { onVerified(otpValues.joinToString("")) }, // ← passes full OTP
                        enabled = isComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
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
                                    if (isComplete) PlexusGradients.cyanButton
                                    else PlexusGradients.cyanButtonDisabled,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VERIFY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp,
                                color = if (isComplete) PlexusColors.BgDark
                                else PlexusColors.TextWhite.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(cardAlpha.value)
            ) {
                Text(
                    text = "Didn't receive OTP? ",
                    fontSize = 12.sp,
                    color = PlexusColors.TextMuted
                )
                if (canResend) {
                    TextButton(onClick = {
                        canResend = false
                        timerValue = 60
                        onResend()
                    }) {
                        Text(
                            text = "Resend",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PlexusColors.CyanPrimary
                        )
                    }
                } else {
                    Text(
                        text = "Resend in ${timerValue}s",
                        fontSize = 12.sp,
                        color = PlexusColors.CyanPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpBox(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(                          // ← key fix: BasicTextField not OutlinedTextField
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(
            color = PlexusColors.TextWhite,  // ← always visible white
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(PlexusColors.CyanLight),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isFocused) PlexusColors.CyanPrimary.copy(alpha = 0.1f)
                        else PlexusColors.BgDark
                    )
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) PlexusColors.CyanPrimary
                        else PlexusColors.PurplePrimary.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
    )
}