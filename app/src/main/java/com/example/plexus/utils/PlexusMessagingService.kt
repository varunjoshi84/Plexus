package com.example.plexus.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.example.plexus.MainActivity
import com.example.plexus.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PlexusMessagingService : FirebaseMessagingService() {

    companion object {
        const val TAG = "PlexusMessagingService"
        const val CHANNEL_ID = "plexus_messages"
        const val CHANNEL_NAME = "Messages"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        val senderName = message.data["senderName"] ?: "Plexus User"
        val senderId = message.data["senderId"] ?: ""
        val body = message.data["text"] ?: ""
        val chatId = message.data["chatId"] ?: ""

        Log.d(TAG, "Message Data: senderName=$senderName, body=$body, chatId=$chatId")

        if (body.isNotEmpty() && chatId.isNotEmpty()) {
            showMessagingStyleNotification(senderName, senderId, body, chatId)
        } else {
            Log.w(TAG, "Empty body or chatId, not showing notification")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Token generated: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = com.google.firebase.auth.FirebaseAuth
            .getInstance().currentUser?.uid ?: run {
                Log.w(TAG, "No user logged in, cannot save new token")
                return
            }

        com.google.firebase.firestore.FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
            .addOnSuccessListener { Log.d(TAG, "Token updated in Firestore") }
            .addOnFailureListener { Log.e(TAG, "Failed to update token", it) }
    }

    private fun showMessagingStyleNotification(
        senderName: String,
        senderId: String,
        body: String,
        chatId: String
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android 8.0+
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Direct messages from Plexus contacts"
            enableVibration(true)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)

        // 1. Create the Person (Sender)
        val sender = Person.Builder()
            .setName(senderName)
            .setKey(senderId)
            .build()

        // 2. Build MessagingStyle
        // For a single message, we create the style. In a production app,
        // you'd typically retrieve the existing notification for this chatId
        // to append multiple messages (grouping).
        val messagingStyle = NotificationCompat.MessagingStyle(Person.Builder().setName("Me").build())
            .setConversationTitle(null) // null for 1-to-1 chats
            .addMessage(body, System.currentTimeMillis(), sender)

        // 3. Main Intent (Open Chat)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("chatId", chatId)
            putExtra("contactName", senderName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            chatId.hashCode(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Quick Reply Action (Optional but very WhatsApp-like)
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Reply...")
            .build()

        val replyIntent = Intent(this, MainActivity::class.java).apply {
            action = "com.example.plexus.ACTION_REPLY"
            putExtra("chatId", chatId)
        }
        
        val replyPendingIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode() + 1,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE // Mutable for RemoteInput
        )

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground, // Replace with a reply icon if you have one
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // 5. Build Final Notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setStyle(messagingStyle)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(replyAction)
            .setShortcutId(chatId) // Useful for Android 11+ conversation grouping
            .build()

        // Use chatId.hashCode() as ID to group notifications by conversation
        manager.notify(chatId.hashCode(), notification)
    }
}
