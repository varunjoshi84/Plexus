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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plexus.ui.theme.*
import kotlinx.coroutines.launch

// Pure UI data class — no backend
data class ChatPreviewUiModel(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isLocal: Boolean = false  // local network or internet
)

@Composable
fun HomeScreen(
    chats: List<ChatPreviewUiModel> = emptyList(),
    onChatClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNewChat: () -> Unit = {}
) {
    val titleAlpha = remember { Animatable(0f) }
    val listAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, tween(600)) }
        launch {
            kotlinx.coroutines.delay(300)
            listAlpha.animateTo(1f, tween(700))
        }
    }

    PlexusBackground {

        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .alpha(titleAlpha.value),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PLEXUS",
                        style = TextStyle(brush = PlexusGradients.cyanText),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Messages",
                        fontSize = 12.sp,
                        color = PlexusColors.TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Network status badges
                    PlexusStatusBadge(label = "Local", dotColor = PlexusColors.CyanGreen)

                    // Profile avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PlexusGradients.cyanButton)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Y",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PlexusColors.BgDark
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PlexusColors.PurplePrimary.copy(alpha = 0.15f))
            )

            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .alpha(titleAlpha.value)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PlexusColors.CardBg)
                    .border(
                        1.dp,
                        PlexusColors.PurplePrimary.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Search conversations...",
                    fontSize = 13.sp,
                    color = PlexusColors.TextMuted.copy(alpha = 0.5f),
                    letterSpacing = 0.3.sp
                )
            }

            // Chat list
            if (chats.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(listAlpha.value),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlexusLogo(size = 72.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No conversations yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PlexusColors.TextWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Start chatting on local network\nor over the internet",
                            fontSize = 13.sp,
                            color = PlexusColors.TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(listAlpha.value),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chats) { chat ->
                        ChatPreviewCard(
                            chat = chat,
                            onClick = { onChatClick(chat.id) }
                        )
                    }
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(PlexusGradients.cyanButton)
                .clickable { onNewChat() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", fontSize = 28.sp, color = PlexusColors.BgDark, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun ChatPreviewCard(
    chat: ChatPreviewUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PlexusColors.CardBg)
            .border(1.dp, PlexusGradients.cardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                PlexusColors.PurplePrimary.copy(alpha = 0.4f),
                                PlexusColors.CyanPrimary.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .border(1.dp, PlexusGradients.cardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.name.first().uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PlexusColors.TextWhite
                )
            }
            // Online dot
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(PlexusColors.BgDark)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PlexusColors.CyanGreen, CircleShape)
                    )
                }
            }
        }

        // Message info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PlexusColors.TextWhite
                )
                Text(
                    text = chat.time,
                    fontSize = 11.sp,
                    color = PlexusColors.TextMuted
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage,
                    fontSize = 12.sp,
                    color = PlexusColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Local/Internet tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (chat.isLocal) PlexusColors.CyanGreen.copy(alpha = 0.15f)
                                else PlexusColors.PurplePrimary.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (chat.isLocal) "LOCAL" else "NET",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = if (chat.isLocal) PlexusColors.CyanGreen
                            else PlexusColors.PurplePrimary
                        )
                    }
                    // Unread badge
                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(PlexusGradients.cyanButton, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlexusColors.BgDark
                            )
                        }
                    }
                }
            }
        }
    }
}