package com.example.plexus.data.model

data class LocalMessageModel(
    val id: String,
    val text: String,
    val senderIp: String,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean = false
)