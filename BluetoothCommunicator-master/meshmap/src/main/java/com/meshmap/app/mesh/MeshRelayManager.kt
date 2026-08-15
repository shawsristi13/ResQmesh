package com.meshmap.app.mesh

import android.content.Context
import android.util.Log
import com.bluetooth.communicator.BluetoothCommunicator
import com.bluetooth.communicator.Message as BtMessage
import com.meshmap.app.repository.MeshRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles the core logic of the mesh network:
 * - Sending messages
 * - Receiving messages
 * - Deduplicating messages
 * - Decrementing TTL and forwarding to other peers
 * - Persisting messages to database
 */
class MeshRelayManager(
    private val context: Context,
    private val communicator: BluetoothCommunicator,
    private val repository: MeshRepository
) {
    // Unique identifier for THIS device on the mesh
    private val myDeviceId: String = communicator.uniqueName

    // Scope for background database operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Cache of seen message IDs to prevent infinite relay loops (thread-safe set)
    private val seenMessages = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    private val _incomingMessages = MutableSharedFlow<MeshMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<MeshMessage> = _incomingMessages.asSharedFlow()

    private val connectionManager = ConnectionManager(context, communicator) { rawBtMessage ->
        handleIncomingRawMessage(rawBtMessage)
    }

    /**
     * Start the mesh networking layer.
     */
    fun start() {
        connectionManager.startMesh()
    }

    /**
     * Stop the mesh networking layer.
     */
    fun stop() {
        connectionManager.stopMesh()
    }

    /**
     * Restart the mesh networking layer.
     */
    fun restart() {
        connectionManager.stopMesh()
        // Adding a slight delay might be safer for the Bluetooth stack
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            connectionManager.startMesh()
        }, 500)
    }

    /**
     * Expose the live list of connected peers.
     */
    val connectedPeers = connectionManager.connectedPeers

    /**
     * Sends a new local message into the mesh network.
     */
    fun sendLocalMessage(type: MeshMessageType, payload: String, urgency: Int = 0) {
        val message = MeshMessage(
            type = type,
            originDeviceId = myDeviceId,
            // Strip the 4-char unique suffix added by library for human display
            originName = myDeviceId.dropLast(4),
            payload = payload,
            urgency = urgency
        )
        
        // Add ourselves to the start of the path
        message.hopPath.add(myDeviceId)
        
        // Mark as seen so we don't process our own broadcast if someone bounces it back
        seenMessages.add(message.id)

        // Broadcast to all connected peers
        Log.e("MeshRelay", "sendLocalMessage: Broadcasting new message ${message.id} to mesh")
        broadcastToMesh(message)
        
        // Save to Database
        scope.launch {
            repository.insertMessage(message)
        }
        
        // Emit to local UI
        _incomingMessages.tryEmit(message)
    }

    /**
     * Handles raw incoming messages from the BluetoothCommunicator library.
     */
    private fun handleIncomingRawMessage(rawBtMessage: BtMessage) {
        // We only care about text messages (the JSON payload)
        val jsonPayload = rawBtMessage.text ?: return
        
        val meshMessage = MeshMessage.fromJson(jsonPayload)
        if (meshMessage == null) {
            Log.e("MeshRelay", "Failed to parse incoming message: $jsonPayload")
            return
        }

        // 1. Deduplication Gate
        if (seenMessages.contains(meshMessage.id)) {
            Log.e("MeshRelay", "Dropping duplicate message: ${meshMessage.id} from ${meshMessage.originName}")
            return
        }
        
        // 2. Mark as seen
        seenMessages.add(meshMessage.id)
        
        Log.e("MeshRelay", "Received new message: ${meshMessage.id} (hop: ${meshMessage.hopCount}, origin: ${meshMessage.originName})")

        // 3. Save to Database
        scope.launch {
            repository.insertMessage(meshMessage)
        }

        // 4. Emit to local UI so user sees it
        _incomingMessages.tryEmit(meshMessage)

        // 5. Relay logic (TTL Gate)
        if (meshMessage.ttl > 0) {
            meshMessage.ttl -= 1
            meshMessage.hopCount += 1
            meshMessage.hopPath.add(myDeviceId)
            
            Log.e("MeshRelay", "Relaying message ${meshMessage.id} (new TTL: ${meshMessage.ttl}, new hop: ${meshMessage.hopCount})")
            broadcastToMesh(meshMessage)
        } else {
            Log.e("MeshRelay", "Message ${meshMessage.id} reached TTL 0. Dropping relay.")
        }
    }

    /**
     * Serializes a MeshMessage and sends it to all connected peers.
     */
    private fun broadcastToMesh(meshMessage: MeshMessage) {
        val jsonStr = meshMessage.toJson()
        // header is arbitrary for this app since we encapsulate type in JSON
        val btMessage = BtMessage(context, "M", jsonStr, null) 
        communicator.sendMessage(btMessage)
    }
}
