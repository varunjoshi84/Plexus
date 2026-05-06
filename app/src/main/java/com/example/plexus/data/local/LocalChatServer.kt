package com.example.plexus.data.local

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException

class LocalChatServer(port: Int) : NanoWSD(port) {

    companion object {
        const val TAG = "LocalChatServer"
    }

    private val _receivedMessages = MutableStateFlow<String?>(null)
    val receivedMessages: StateFlow<String?> = _receivedMessages

    private val connectedSockets = mutableListOf<WebSocket>()

    override fun openWebSocket(handshake: NanoHTTPD.IHTTPSession): WebSocket {
        return PlexusWebSocket(handshake)
    }

    inner class PlexusWebSocket(handshake: NanoHTTPD.IHTTPSession) : WebSocket(handshake) {

        override fun onOpen() {
            Log.d(TAG, "Client connected")
            synchronized(connectedSockets) {
                connectedSockets.add(this)
            }
        }

        override fun onClose(
            code: WebSocketFrame.CloseCode?,
            reason: String?,
            initiatedByRemote: Boolean
        ) {
            Log.d(TAG, "Client disconnected: $reason")
            synchronized(connectedSockets) {
                connectedSockets.remove(this)
            }
        }

        override fun onMessage(message: WebSocketFrame) {
            val text = message.textPayload
            Log.d(TAG, "Message received: $text")
            _receivedMessages.value = text
            broadcastToOthers(text, this)
        }

        override fun onPong(pong: WebSocketFrame) {}

        override fun onException(exception: IOException) {
            Log.e(TAG, "WebSocket error: ${exception.message}")
            synchronized(connectedSockets) {
                connectedSockets.remove(this)
            }
        }
    }

    fun broadcastToOthers(message: String, exclude: WebSocket? = null) {
        synchronized(connectedSockets) {
            connectedSockets
                .filter { it != exclude }
                .forEach {
                    try {
                        it.send(message)
                    } catch (e: Exception) {
                        Log.e(TAG, "Broadcast error: ${e.message}")
                    }
                }
        }
    }
}