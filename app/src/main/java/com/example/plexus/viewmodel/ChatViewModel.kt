package com.example.plexus.viewmodel

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

    // ─── Save user after login ─────────────────────────
    fun saveUser(phone: String, name: String = "") {
        val uid = currentUserId
        if (uid.isEmpty()) return
        val user = UserModel(uid = uid, name = name, phone = phone, isOnline = true)
        db.collection("users").document(uid).set(user)
    }

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
                _chats.value = snapshot?.documents?.mapNotNull {
                    it.toObject(ChatModel::class.java)
                } ?: emptyList()
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

    // ─── Search user by phone ─────────────────────────
    fun searchUserByPhone(phone: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            _searchResults.value = emptyList()
            try {
                val normalized = phone.replace(" ", "").trim()
                val snapshot = db.collection("users")
                    .whereEqualTo("phone", normalized)
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
}