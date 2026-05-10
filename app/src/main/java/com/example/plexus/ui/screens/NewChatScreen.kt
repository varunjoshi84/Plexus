package com.example.plexus.ui.screens
import androidx.compose.ui.focus.onFocusChanged

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.data.model.UserModel
import com.example.plexus.ui.theme.*

@Composable
fun NewChatScreen(
    searchResults: List<UserModel> = emptyList(),
    isSearching: Boolean = false,
    errorMessage: String? = null,
    onSearch: (String) -> Unit = {},
    onUserClick: (UserModel) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

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
                    text = "New Chat",
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

            Spacer(modifier = Modifier.height(20.dp))

            // Search box
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
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
                        text = "SEARCH BY USERNAME OR PHONE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = PlexusColors.CyanPrimary.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PlexusColors.CardBg)
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = if (isFocused) PlexusColors.CyanPrimary
                            else PlexusColors.PurplePrimary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🔍", fontSize = 16.sp)

                    BasicTextField(
                        value = searchText,
                        onValueChange = { input ->
                            if (input.length <= 20) searchText = input.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '+' }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        textStyle = TextStyle(
                            color = PlexusColors.TextWhite,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        ),
                        cursorBrush = SolidColor(PlexusColors.CyanLight),
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchText.isEmpty()) {
                                    Text(
                                        text = "username_or_phone",
                                        color = PlexusColors.TextMuted.copy(alpha = 0.4f),
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged {
                                isFocused = it.isFocused
                            }
                    )

                    // Clear button
                    if (searchText.isNotEmpty()) {
                        Text(
                            text = "✕",
                            fontSize = 14.sp,
                            color = PlexusColors.TextMuted,
                            modifier = Modifier.clickable { searchText = "" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (searchText.length >= 3) PlexusGradients.cyanButton
                            else PlexusGradients.cyanButtonDisabled
                        )
                        .clickable(enabled = searchText.length >= 3) {
                            onSearch(searchText.trim())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            color = PlexusColors.BgDark,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "SEARCH",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            color = if (searchText.length >= 3) PlexusColors.BgDark
                            else PlexusColors.TextWhite.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            errorMessage?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF4444).copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            Color(0xFFFF4444).copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = Color(0xFFFF6666)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Results section
            if (searchResults.isNotEmpty()) {
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
                        text = "RESULTS (${searchResults.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = PlexusColors.CyanPrimary.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { user ->
                        UserResultCard(
                            user = user,
                            onClick = { onUserClick(user) }
                        )
                    }
                }
            } else if (!isSearching && errorMessage == null && searchText.isEmpty()) {
                // Idle state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlexusLogo(size = 64.dp)
                        Text(
                            text = "Find people on Plexus",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PlexusColors.TextWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Search by username to\nstart a conversation",
                            fontSize = 12.sp,
                            color = PlexusColors.TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else if (!isSearching && searchResults.isEmpty() && searchText.isNotEmpty()) {
                // No results
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🔍", fontSize = 40.sp)
                        Text(
                            text = "No user found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PlexusColors.TextWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Make sure you have the correct username",
                            fontSize = 12.sp,
                            color = PlexusColors.TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserResultCard(
    user: UserModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PlexusColors.CardBg)
            .border(1.dp, PlexusGradients.cardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PlexusColors.PurplePrimary.copy(alpha = 0.6f),
                            PlexusColors.CyanPrimary.copy(alpha = 0.6f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (user.displayName.isNotEmpty()) user.displayName.first().uppercase()
                else if (user.username.isNotEmpty()) user.username.first().uppercase()
                else "?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PlexusColors.TextWhite
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (user.displayName.isNotEmpty()) user.displayName else user.username,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = PlexusColors.TextWhite
            )
            Text(
                text = "@${user.username}",
                fontSize = 12.sp,
                color = PlexusColors.TextMuted
            )
        }

        // Start chat button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(PlexusGradients.cyanButton)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Chat",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PlexusColors.BgDark
            )
        }
    }
}