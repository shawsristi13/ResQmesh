package com.meshmap.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meshmap.app.MeshMapApplication
import com.meshmap.app.classifier.UrgencyClassifier
import com.meshmap.app.mesh.MeshMessage
import com.meshmap.app.mesh.MeshMessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeshViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MeshMapApplication
    private val relayManager = app.meshRelayManager

    val connectedPeers = relayManager?.connectedPeers ?: MutableStateFlow(emptyList())

    private val _messages = MutableStateFlow<List<MeshMessage>>(emptyList())
    val messages: StateFlow<List<MeshMessage>> = _messages.asStateFlow()
    
    private val _sosAlerts = MutableStateFlow<List<MeshMessage>>(emptyList())
    val sosAlerts: StateFlow<List<MeshMessage>> = _sosAlerts.asStateFlow()

    init {
        // Collect incoming messages from the relay manager
        viewModelScope.launch {
            relayManager?.incomingMessages?.collect { msg ->
                // Add to messages list
                val updatedMessages = _messages.value.toMutableList().apply {
                    add(msg)
                    // Keep last 100 messages in memory for now
                    if (size > 100) removeAt(0)
                }
                _messages.value = updatedMessages
                
                // Add to SOS alerts if urgency > 0 or type is SOS
                if (msg.urgency > 0 || msg.type == MeshMessageType.SOS) {
                    val updatedSos = _sosAlerts.value.toMutableList().apply {
                        add(msg)
                        if (size > 50) removeAt(0)
                    }
                    _sosAlerts.value = updatedSos
                }
            }
        }
    }

    fun sendChatMessage(payload: String) {
        val urgency = UrgencyClassifier.classify(payload)
        relayManager?.sendLocalMessage(
            type = MeshMessageType.CHAT,
            payload = payload,
            urgency = urgency
        )
    }

    fun sendSosAlert(payload: String = "SOS! I need help!") {
        relayManager?.sendLocalMessage(
            type = MeshMessageType.SOS,
            payload = payload,
            urgency = 2 // Always critical
        )
    }
}
