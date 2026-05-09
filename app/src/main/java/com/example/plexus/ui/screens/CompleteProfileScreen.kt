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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CompleteProfileScreen(
    onComplete: (String, String, String) -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(600))
    }

    PlexusBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PlexusLogo(size = 64.dp)
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Complete Your Profile",
                style = TextStyle(brush = PlexusGradients.cyanText),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Set up your public identity on Plexus",
                fontSize = 14.sp,
                color = PlexusColors.TextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileTextField(
                    value = username,
                    onValueChange = { if (it.length <= 20) username = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' } },
                    label = "USERNAME",
                    placeholder = "unique_username"
                )
                
                ProfileTextField(
                    value = displayName,
                    onValueChange = { if (it.length <= 30) displayName = it },
                    label = "DISPLAY NAME",
                    placeholder = "Your Name"
                )

                ProfileTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 100) bio = it },
                    label = "BIO (OPTIONAL)",
                    placeholder = "Tell us about yourself"
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            val isEnabled = username.length >= 3 && displayName.isNotEmpty() && !isLoading
            
            Button(
                onClick = { onComplete(username, displayName, bio) },
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        if (isEnabled) PlexusGradients.cyanButton else PlexusGradients.cyanButtonDisabled,
                        RoundedCornerShape(12.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = PlexusColors.BgDark, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "FINISH",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            color = if (isEnabled) PlexusColors.BgDark else PlexusColors.TextWhite.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(6.dp).background(PlexusColors.CyanGreen, CircleShape))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = PlexusColors.CyanPrimary.copy(alpha = 0.7f))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = PlexusColors.TextWhite.copy(alpha = 0.2f), fontSize = 16.sp) },
            singleLine = true,
            textStyle = TextStyle(color = PlexusColors.TextWhite, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PlexusColors.CyanPrimary,
                unfocusedBorderColor = PlexusColors.CyanPrimary.copy(alpha = 0.25f),
                cursorColor = PlexusColors.CyanLight,
                focusedContainerColor = PlexusColors.CyanPrimary.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
