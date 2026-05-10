package com.example.plexus.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

data class MessageUiModel(
    val id: String,
    val text: String,
    val isMine: Boolean,
    val time: String,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus { SENDING, SENT, DELIVERED, READ }

@Composable
fun ChatScreen(
    contactName: String = "Contact",
    isOnline: Boolean = false,
    isLocal: Boolean = false,
    messages: List<MessageUiModel> = emptyList(),
    onSendMessage: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val headerAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        headerAlpha.animateTo(1f, tween(500))
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    PlexusBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(PlexusColors.CardBg.copy(alpha = 0.9f))
                    .border(
                        width = 1.dp,
                        color = PlexusColors.PurplePrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .alpha(headerAlpha.value),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Back button
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

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PlexusGradients.cyanButton),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contactName.first().uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PlexusColors.BgDark
                    )
                }

                // Name & status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contactName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PlexusColors.TextWhite
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isOnline) PlexusColors.CyanGreen
                                    else PlexusColors.TextMuted,
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (isOnline) {
                                if (isLocal) "Online · Local Network" else "Online · Internet"
                            } else "Offline",
                            fontSize = 11.sp,
                            color = PlexusColors.TextMuted
                        )
                    }
                }

                // Network tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isLocal) PlexusColors.CyanGreen.copy(alpha = 0.1f)
                            else PlexusColors.PurplePrimary.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isLocal) "LOCAL" else "NET",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isLocal) PlexusColors.CyanGreen else PlexusColors.PurplePrimary
                    )
                }
            }

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PlexusLogo(size = 56.dp)
                                Text(
                                    text = "Say hello to $contactName",
                                    fontSize = 14.sp,
                                    color = PlexusColors.TextMuted
                                )
                            }
                        }
                    }
                } else {
                    items(messages) { msg ->
                        MessageBubble(message = msg)
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PlexusColors.CardBg.copy(alpha = 0.95f))
                    .border(
                        width = 1.dp,
                        color = PlexusColors.PurplePrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Message...",
                            color = PlexusColors.TextMuted.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    textStyle = TextStyle(
                        color = PlexusColors.TextWhite,
                        fontSize = 14.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlexusColors.PurplePrimary.copy(alpha = 0.5f),
                        unfocusedBorderColor = PlexusColors.PurplePrimary.copy(alpha = 0.2f),
                        cursorColor = PlexusColors.CyanLight,
                        focusedContainerColor = PlexusColors.BgDark.copy(alpha = 0.5f),
                        unfocusedContainerColor = PlexusColors.BgDark.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 4,
                    modifier = Modifier.weight(1f)
                )

                // ✅ Fixed: no .then() — use if/else on the whole modifier
                val sendModifier = if (inputText.isNotBlank()) {
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PlexusGradients.cyanButton)
                        .clickable {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        }
                } else {
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PlexusGradients.cyanButtonDisabled)
                }

                Box(
                    modifier = sendModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "↑",
                        fontSize = 20.sp,
                        color = PlexusColors.BgDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start
    ) {
        //Fixed: no .then() — use if/else on the whole modifier
        val bubbleModifier = if (message.isMine) {
            Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = 16.dp, bottomEnd = 4.dp
                    )
                )
                .background(PlexusGradients.cyanButton)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        } else {
            Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = 4.dp, bottomEnd = 16.dp
                    )
                )
                .background(PlexusColors.CardBg)
                .border(
                    1.dp,
                    PlexusColors.PurplePrimary.copy(alpha = 0.2f),
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = 4.dp, bottomEnd = 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        }

        Box(modifier = bubbleModifier) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = if (message.isMine) PlexusColors.BgDark else PlexusColors.TextWhite,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message.time,
                fontSize = 10.sp,
                color = PlexusColors.TextMuted.copy(alpha = 0.6f)
            )
            if (message.isMine) {
                Text(
                    text = when (message.status) {
                        MessageStatus.SENDING  -> "○"
                        MessageStatus.SENT     -> "✓"
                        MessageStatus.DELIVERED -> "✓✓"
                        MessageStatus.READ     -> "✓✓"
                    },
                    fontSize = 10.sp,
                    color = when (message.status) {
                        MessageStatus.READ -> PlexusColors.CyanGreen
                        else -> PlexusColors.TextMuted.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}