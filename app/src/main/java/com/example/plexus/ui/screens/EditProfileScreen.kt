package com.example.plexus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.ui.theme.*

@Composable
fun EditProfileScreen(
    currentName: String,
    currentUsername: String,
    onUpdate: (String, String) -> Unit,
    onBack: () -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false
) {
    var username by remember { mutableStateOf(currentUsername) }
    var displayName by remember { mutableStateOf(currentName) }

    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(500))
    }

    PlexusBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha.value)
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
                    text = "Edit Profile",
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    ProfileTextField(
                        value = displayName,
                        onValueChange = { if (it.length <= 30) displayName = it },
                        label = "DISPLAY NAME",
                        placeholder = "Your Name"
                    )

                    ProfileTextField(
                        value = username,
                        onValueChange = { if (it.length <= 20) username = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' } },
                        label = "USERNAME",
                        placeholder = "unique_username"
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

                Spacer(modifier = Modifier.height(48.dp))

                val isChanged = (username != currentUsername || displayName != currentName) && username.length >= 3 && displayName.isNotEmpty()
                
                Button(
                    onClick = { onUpdate(username, displayName) },
                    enabled = isChanged && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            if (isChanged && !isLoading) PlexusGradients.cyanButton else PlexusGradients.cyanButtonDisabled,
                            RoundedCornerShape(12.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = PlexusColors.BgDark, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "SAVE CHANGES",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = if (isChanged && !isLoading) PlexusColors.BgDark else PlexusColors.TextWhite.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}
