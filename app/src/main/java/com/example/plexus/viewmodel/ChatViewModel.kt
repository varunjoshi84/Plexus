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

    // ─── Create Group Chat ────────────────────────────
    fun createGroup(name: String, participantIds: List<String>, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val groupId = db.collection("chats").document().id
                val groupParticipants = (participantIds + currentUserId).distinct()
                
                val chat = ChatModel(
                    chatId = groupId,
                    participants = groupParticipants,
                    lastMessage = "Group created",
                    lastTime = System.currentTimeMillis(),
                    isGroup = true,
                    groupName = name,
                    adminId = currentUserId
                )

                db.collection("chats")
                    .document(groupId)
                    .set(chat)
                    .await()

                onCreated(groupId)
            } catch (e: Exception) {
                _error.value = "Failed to create group: ${e.message}"
            }
        }
    }

    // ─── Search user by username or phone ─────────────────────
    fun searchUsers(query: String) {
        if (query.length < 3) return
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            _searchResults.value = emptyList()
            try {
                var normalized = query.trim()
                val isNumeric = normalized.all { it.isDigit() || it == '+' }
                
                // Hardcode +91 for 10-digit Indian numbers
                if (isNumeric && normalized.length == 10 && !normalized.startsWith("+")) {
                    normalized = "+91$normalized"
                }

                Log.d("PlexusSearch", "Searching for: $normalized (isNumeric: $isNumeric)")

                val snapshot = if (isNumeric) {
                    // Search by phone number - Fix: Field name is 'phone', not 'phoneNumber'
                    db.collection("users")
                        .whereGreaterThanOrEqualTo("phone", normalized)
                        .whereLessThanOrEqualTo("phone", normalized + "\uf8ff")
                        .get()
                        .await()
                } else {
                    // Search by username
                    db.collection("users")
                        .whereGreaterThanOrEqualTo("username", normalized)
                        .whereLessThanOrEqualTo("username", normalized + "\uf8ff")
                        .get()
                        .await()
                }

                val results = snapshot.documents
                    .mapNotNull { it.toObject(UserModel::class.java) }
                    .filter { it.uid != currentUserId }
                
                Log.d("PlexusSearch", "Found ${results.size} users")
                _searchResults.value = results
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

    // ─── Delete chat ──────────────────────────────────
    fun deleteChat(chatId: String) {
        Log.d("PlexusDelete", "Starting deletion for chatId: $chatId")
        Log.d("PlexusDelete", "Current User ID: $currentUserId")
        viewModelScope.launch {
            try {
                // Pre-check: Fetch the chat document to see participants
                val chatDoc = db.collection("chats").document(chatId).get().await()
                if (chatDoc.exists()) {
                    val participants = chatDoc.get("participants") as? List<*>
                    Log.d("PlexusDelete", "Chat participants: $participants")
                    if (participants == null || !participants.contains(currentUserId)) {
                        Log.w("PlexusDelete", "Warning: Current user $currentUserId not found in participants list!")
                    }
                } else {
                    Log.e("PlexusDelete", "Chat document does not exist!")
                }

                // 1. Delete all messages in the subcollection
                Log.d("PlexusDelete", "Step 1: Fetching messages for chatId: $chatId")
                val msgsSnapshot = db.collection("messages")
                    .document(chatId)
                    .collection("msgs")
                    .get()
                    .await()

                Log.d("PlexusDelete", "Found ${msgsSnapshot.size()} messages to delete")

                if (!msgsSnapshot.isEmpty) {
                    val batch = db.batch()
                    msgsSnapshot.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                    Log.d("PlexusDelete", "Messages batch deletion successful")
                }

                // 2. Delete the chat document itself
                Log.d("PlexusDelete", "Step 2: Deleting chat document: $chatId")
                db.collection("chats")
                    .document(chatId)
                    .delete()
                    .await()
                
                Log.d("PlexusDelete", "Chat document deleted successfully")

            } catch (e: Exception) {
                Log.e("PlexusDelete", "Error during deletion: ${e.message}", e)
                _error.value = "Failed to delete chat: ${e.message}"
            }
        }
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
