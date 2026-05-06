package com.example.plexus.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plexus.data.local.DiscoveredDevice
import com.example.plexus.data.local.LocalChatClient
import com.example.plexus.data.local.LocalChatServer
import com.example.plexus.data.local.NsdDiscoveryManager
import com.example.plexus.data.model.LocalMessageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.ServerSocket
import java.util.UUID

class LocalChatViewModel : ViewModel() {

    private var nsdManager: NsdDiscoveryManager? = null
    private val chatClient = LocalChatClient()
    private var chatServer: LocalChatServer? = null

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices

    val connectionState: StateFlow<LocalChatClient.ConnectionState>
        get() = chatClient.connectionState

    private val _messages = MutableStateFlow<List<LocalMessageModel>>(emptyList())
    val messages: StateFlow<List<LocalMessageModel>> = _messages

    private var myName: String = "Me"
    private var myIp: String = ""
    private var isInitialized = false

    fun initialize(context: Context, deviceName: String) {
        if (isInitialized) return
        isInitialized = true
        myName = deviceName

        val port = getFreePort()
        Log.d("LocalChat", "Starting on port: $port")

        // ✅ start NanoWSD WebSocket server
        chatServer = LocalChatServer(port)
        chatServer?.start()
        Log.d("LocalChat", "WebSocket server started on port $port")

        nsdManager = NsdDiscoveryManager(context)
        nsdManager?.registerService(port, deviceName)
        nsdManager?.startDiscovery()

        viewModelScope.launch {
            nsdManager?.discoveredDevices?.collect { devices ->
                _discoveredDevices.value = devices
            }
        }

        // listen for messages from server
        viewModelScope.launch {
            chatServer?.receivedMessages?.collect { raw ->
                raw?.let { parseAndAddMessage(it, isMine = false) }
            }
        }

        // listen for messages from client
        viewModelScope.launch {
            chatClient.receivedMessages.collect { raw ->
                raw?.let { parseAndAddMessage(it, isMine = false) }
            }
        }
    }

    fun connectToDevice(device: DiscoveredDevice) {
        Log.d("LocalChat", "Connecting to ${device.host}:${device.port}")
        chatClient.connect(device.host, device.port)
    }

    fun sendMessage(text: String) {
        val json = JSONObject().apply {
            put("id", UUID.randomUUID().toString())
            put("text", text)
            put("senderName", myName)
            put("senderIp", myIp)
            put("timestamp", System.currentTimeMillis())
        }.toString()

        val sent = chatClient.sendMessage(json)
        if (sent) {
            val msg = LocalMessageModel(
                id = UUID.randomUUID().toString(),
                text = text,
                senderIp = myIp,
                senderName = myName,
                isMine = true
            )
            _messages.value = _messages.value + msg
        } else {
            Log.e("LocalChat", "Failed to send message — not connected")
        }
    }

    private fun parseAndAddMessage(raw: String, isMine: Boolean) {
        try {
            val json = JSONObject(raw)
            val msg = LocalMessageModel(
                id = json.getString("id"),
                text = json.getString("text"),
                senderIp = json.optString("senderIp", ""),
                senderName = json.getString("senderName"),
                timestamp = json.getLong("timestamp"),
                isMine = isMine
            )
            _messages.value = _messages.value + msg
        } catch (e: Exception) {
            Log.e("LocalChat", "Parse error: ${e.message}")
        }
    }

    private fun getFreePort(): Int {
        return try {
            val socket = ServerSocket(0)
            val port = socket.localPort
            socket.close()
            port
        } catch (e: Exception) {
            (49152..65535).random()
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatServer?.stop()
        nsdManager?.stopDiscovery()
        nsdManager?.unregisterService()
        chatClient.disconnect()
    }
}