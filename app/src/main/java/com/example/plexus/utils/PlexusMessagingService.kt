package com.example.plexus.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.plexus.MainActivity
import com.example.plexus.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PlexusMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "plexus_messages"
        const val CHANNEL_NAME = "Messages"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["senderName"] ?: "New Message"
        val body = message.data["text"] ?: ""
        val chatId = message.data["chatId"] ?: ""

        showNotification(title, body, chatId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save token to Firestore so others can send notifications to you
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = com.google.firebase.auth.FirebaseAuth
            .getInstance().currentUser?.uid ?: return

        com.google.firebase.firestore.FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }

    private fun showNotification(title: String, body: String, chatId: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Create channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Plexus chat messages"
        }
        manager.createNotificationChannel(channel)

        // Tap notification → open app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("chatId", chatId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}