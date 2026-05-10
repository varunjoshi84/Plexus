package com.example.plexus.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.focus.onFocusChanged
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
fun NewGroupScreen(
    searchResults: List<UserModel> = emptyList(),
    isSearching: Boolean = false,
    onSearch: (String) -> Unit = {},
    onCreateGroup: (String, List<String>) -> Unit = { _, _ -> },
    onBack: () -> Unit = {}
) {
    var groupName by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var selectedUsers by remember { mutableStateOf(setOf<UserModel>()) }
    var isSearchFocused by remember { mutableStateOf(false) }

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
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 18.sp, color = PlexusColors.CyanPrimary)
                }
                Text(
                    text = "New Group",
                    style = TextStyle(brush = PlexusGradients.cyanText),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Group Name Input
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "GROUP NAME",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = PlexusColors.CyanPrimary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PlexusColors.CardBg)
                        .border(1.dp, PlexusColors.PurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    decorationBox = { innerTextField ->
                        if (groupName.isEmpty()) Text("Enter group name...", color = Color.Gray)
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search and Selection
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "ADD BY USERNAME OR PHONE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = PlexusColors.CyanPrimary.copy(alpha = 0.7f)
                )
                
                // Selected Users Horizontal List
                if (selectedUsers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedUsers.toList()) { user ->
                            SelectedUserChip(user = user) {
                                selectedUsers = selectedUsers - user
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PlexusColors.CardBg)
                        .border(
                            1.dp,
                            if (isSearchFocused) PlexusColors.CyanPrimary else PlexusColors.PurplePrimary.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = searchText,
                        onValueChange = { 
                            searchText = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '+' }
                            if (it.length >= 3) onSearch(it)
                        },
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.weight(1f).onFocusChanged { isSearchFocused = it.isFocused },
                        decorationBox = { innerTextField ->
                            if (searchText.isEmpty()) Text("Username or phone...", color = Color.Gray, fontSize = 14.sp)
                            innerTextField()
                        }
                    )
                    if (isSearching) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }

            // Search Results
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults.filter { it !in selectedUsers }) { user ->
                    ParticipantResultRow(user = user) {
                        selectedUsers = selectedUsers + user
                        searchText = ""
                    }
                }
            }

            // Create Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (groupName.isNotBlank() && selectedUsers.isNotEmpty()) PlexusGradients.cyanButton
                        else PlexusGradients.cyanButtonDisabled
                    )
                    .clickable(enabled = groupName.isNotBlank() && selectedUsers.isNotEmpty()) {
                        onCreateGroup(groupName, selectedUsers.map { it.uid })
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CREATE GROUP",
                    fontWeight = FontWeight.Bold,
                    color = if (groupName.isNotBlank() && selectedUsers.isNotEmpty()) PlexusColors.BgDark else Color.Gray
                )
            }
        }
    }
}

@Composable
fun SelectedUserChip(user: UserModel, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PlexusColors.PurplePrimary.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = user.username, color = Color.White, fontSize = 14.sp)
        Text(
            text = "✕",
            color = Color.Red,
            modifier = Modifier.clickable { onRemove() }
        )
    }
}

@Composable
fun ParticipantResultRow(user: UserModel, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdd() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(user.username.first().uppercase())
        }
        Text(user.username, color = Color.White)
        Spacer(modifier = Modifier.weight(1f))
        Text("+ Add", color = PlexusColors.CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
