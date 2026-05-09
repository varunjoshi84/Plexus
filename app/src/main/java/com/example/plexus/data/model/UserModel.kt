package com.example.plexus.data.model

import com.google.firebase.firestore.PropertyName

data class UserModel(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val phone: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val fcmToken: String = "",
    @get:PropertyName("online") @set:PropertyName("online")
    var isOnline: Boolean = false
)
