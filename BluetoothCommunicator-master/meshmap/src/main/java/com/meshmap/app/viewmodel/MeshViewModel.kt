package com.meshmap.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meshmap.app.MeshMapApplication
import com.meshmap.app.classifier.UrgencyClassifier
import com.meshmap.app.mesh.MeshMessage
import com.meshmap.app.mesh.MeshMessageType
import com.bluetooth.communicator.Peer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeshViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MeshMapApplication
    private val repository = app.repository

    private val _connectedPeers = MutableStateFlow<List<Peer>>(emptyList())
    val connectedPeers: StateFlow<List<Peer>> = _connectedPeers.asStateFlow()

    init {
        // Collect peers from the MeshRelayManager once it's initialized
        viewModelScope.launch {
            while (app.meshRelayManager == null) {
                delay(100)
            }
            app.meshRelayManager!!.connectedPeers.collect { peers ->
                _connectedPeers.value = peers
            }
        }
    }

    private val _messages = MutableStateFlow<List<MeshMessage>>(emptyList())
    val messages: StateFlow<List<MeshMessage>> = _messages.asStateFlow()
    
    private val _sosAlerts = MutableStateFlow<List<MeshMessage>>(emptyList())
    val sosAlerts: StateFlow<List<MeshMessage>> = _sosAlerts.asStateFlow()

    init {
        // Collect messages from the Room Database for persistence
        viewModelScope.launch {
            repository.allMessages.collect { msgs ->
                _messages.value = msgs
                _sosAlerts.value = msgs.filter { it.urgency > 0 || it.type == MeshMessageType.SOS }
            }
        }
    }

    fun sendChatMessage(payload: String) {
        val urgency = UrgencyClassifier.classify(payload)
        app.meshRelayManager?.sendLocalMessage(
            type = MeshMessageType.CHAT,
            payload = payload,
            urgency = urgency
        )
    }

    fun sendSosAlert(payload: String = "SOS! I need help!") {
        app.meshRelayManager?.sendLocalMessage(
            type = MeshMessageType.SOS,
            payload = payload,
            urgency = 2 // Always critical
        )
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearMessages()
        }
    }

    fun stopMesh() {
        app.meshRelayManager?.stop()
    }

    fun restartMesh() {
        app.meshRelayManager?.restart()
    }
}
