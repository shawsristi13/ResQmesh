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
    private val repository = app.repository

    val connectedPeers = relayManager?.connectedPeers ?: MutableStateFlow(emptyList())

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
