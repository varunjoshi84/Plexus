package com.example.plexus.data.model

data class UserModel(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val phone: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val isOnline: Boolean = false
)