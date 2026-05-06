package com.example.plexus.data.local

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class LocalChatClient {

    companion object {
        const val TAG = "LocalChatClient"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _receivedMessages = MutableStateFlow<String?>(null)
    val receivedMessages: StateFlow<String?> = _receivedMessages

    private var webSocket: WebSocket? = null

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    // ─── Connect to a device ──────────────────────────
    fun connect(host: String, port: Int) {
        _connectionState.value = ConnectionState.Connecting

        val request = Request.Builder()
            .url("ws://$host:$port")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "Connected to $host:$port")
                _connectionState.value = ConnectionState.Connected
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                _receivedMessages.value = text
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                onMessage(ws, bytes.utf8())
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
                _connectionState.value = ConnectionState.Disconnected
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Connection failed: ${t.message}")
                _connectionState.value = ConnectionState.Error(t.message ?: "Connection failed")
            }
        })
    }

    // ─── Send a message ───────────────────────────────
    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    // ─── Disconnect ───────────────────────────────────
    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        _connectionState.value = ConnectionState.Disconnected
    }
}