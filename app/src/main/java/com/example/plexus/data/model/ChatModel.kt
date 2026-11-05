package com.example.plexus.data.model

data class ChatModel(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTime: Long = 0L,

    val isGroup: Boolean = false,
    val groupName: String = "",
    val adminId: String = ""
)