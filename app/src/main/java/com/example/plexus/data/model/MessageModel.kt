package com.example.plexus.data.model


data class MessageModel(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L,
    val status: String = "SENT" // SENDING, SENT, DELIVERED, READ
)