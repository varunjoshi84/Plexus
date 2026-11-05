package com.example.plexus.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class MessageModel(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    @get:PropertyName("timestamp") 
    @set:PropertyName("timestamp")
    var timestampAny: Any? = null,
    
    val status: String = "SENT"
) {
    @get:Exclude
    val timestamp: Date?
        get() = when (val t = timestampAny) {
            is Long -> Date(t)
            is com.google.firebase.Timestamp -> t.toDate()
            is Date -> t
            else -> null
        }
}
