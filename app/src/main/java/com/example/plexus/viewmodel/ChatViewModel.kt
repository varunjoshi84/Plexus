package com.example.plexus.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plexus.data.model.ChatModel
import com.example.plexus.data.model.MessageModel
import com.example.plexus.data.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId get() = auth.currentUser?.uid ?: ""

    private val _messages = MutableStateFlow<List<MessageModel>>(emptyList())
    val messages: StateFlow<List<MessageModel>> = _messages

    private val _chats = MutableStateFlow<List<ChatModel>>(emptyList())
    val chats: StateFlow<List<ChatModel>> = _chats

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchResults = MutableStateFlow<List<UserModel>>(emptyList())
    val searchResults: StateFlow<List<UserModel>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError

    private val _participantProfiles = MutableStateFlow<Map<String, UserModel>>(emptyMap())
    val participantProfiles: StateFlow<Map<String, UserModel>> = _participantProfiles

    // ─── Listen to chats ──────────────────────────────
    fun listenToChats() {
        if (currentUserId.isEmpty()) return
        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }
                val chatsList = snapshot?.documents?.mapNotNull {
                    it.toObject(ChatModel::class.java)
                } ?: emptyList()
                
                _chats.value = chatsList
                
                // Fetch profiles for all participants we don't have yet
                val allParticipantIds = chatsList.flatMap { it.participants }.distinct()
                fetchMissingProfiles(allParticipantIds)
            }
    }

    private fun fetchMissingProfiles(ids: List<String>) {
        val current = _participantProfiles.value
        val missing = ids.filter { it !in current && it.isNotEmpty() }
        if (missing.isEmpty()) return

        viewModelScope.launch {
            try {
                // Firestore "in" query limit is 30, but for now we'll do them one by one or in batches
                missing.forEach { uid ->
                    val doc = db.collection("users").document(uid).get().await()
                    val profile = doc.toObject(UserModel::class.java)
                    if (profile != null) {
                        _participantProfiles.value = _participantProfiles.value + (uid to profile)
                    }
                }
            } catch (e: Exception) {
                // ignore profile fetch errors
            }
        }
    }

    // ─── Listen to messages ───────────────────────────
    fun listenToMessages(chatId: String) {
        db.collection("messages")
            .document(chatId)
            .collection("msgs")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }
                _messages.value = snapshot?.documents?.mapNotNull {
                    it.toObject(MessageModel::class.java)
                } ?: emptyList()
            }
    }

    // ─── Send message ─────────────────────────────────
    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            try {
                val msgRef = db.collection("messages")
                    .document(chatId)
                    .collection("msgs")
                    .document()

                val message = MessageModel(
                    id = msgRef.id,
                    text = text,
                    senderId = currentUserId,
                    timestamp = System.currentTimeMillis(),
                    status = "SENT"
                )

                msgRef.set(message).await()

                db.collection("chats")
                    .document(chatId)
                    .update(
                        mapOf(
                            "lastMessage" to text,
                            "lastTime" to System.currentTimeMillis()
                        )
                    ).await()

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── Create chat ──────────────────────────────────
    fun createChat(otherUserId: String, onChatCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val chatId = if (currentUserId < otherUserId)
                    "${currentUserId}_${otherUserId}"
                else
                    "${otherUserId}_${currentUserId}"

                val chat = ChatModel(
                    chatId = chatId,
                    participants = listOf(currentUserId, otherUserId),
                    lastMessage = "",
                    lastTime = System.currentTimeMillis()
                )

                db.collection("chats")
                    .document(chatId)
                    .set(chat)
                    .await()

                onChatCreated(chatId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── Search user by username ─────────────────────
    fun searchUserByUsername(query: String) {
        if (query.length < 3) return
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            _searchResults.value = emptyList()
            try {
                val normalized = query.lowercase().trim()
                val snapshot = db.collection("users")
                    .whereGreaterThanOrEqualTo("username", normalized)
                    .whereLessThanOrEqualTo("username", normalized + "\uf8ff")
                    .get()
                    .await()

                _searchResults.value = snapshot.documents
                    .mapNotNull { it.toObject(UserModel::class.java) }
                    .filter { it.uid != currentUserId }
            } catch (e: Exception) {
                _searchError.value = "Search failed: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    // ─── Clear search ─────────────────────────────────
    fun clearSearch() {
        _searchResults.value = emptyList()
        _searchError.value = null
    }

    fun saveFcmToken() {
        com.google.firebase.messaging.FirebaseMessaging
            .getInstance().token
            .addOnSuccessListener { token ->
                Log.d("PlexusFCM", "FCM Token: $token")
                val uid = currentUserId
                if (uid.isEmpty()) {
                    Log.w("PlexusFCM", "No UID found, cannot save token")
                    return@addOnSuccessListener
                }
                db.collection("users")
                    .document(uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("PlexusFCM", "Token updated successfully in Firestore")
                    }
                    .addOnFailureListener {
                        Log.e("PlexusFCM", "Failed to update token in Firestore", it)
                    }
            }
            .addOnFailureListener {
                Log.e("PlexusFCM", "Failed to get FCM Token", it)
            }
    }
}
