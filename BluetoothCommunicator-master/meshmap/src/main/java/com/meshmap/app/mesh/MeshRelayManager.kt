package com.meshmap.app.mesh

import android.content.Context
import android.util.Log
import com.bluetooth.communicator.BluetoothCommunicator
import com.bluetooth.communicator.Message as BtMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles the core logic of the mesh network:
 * - Sending messages
 * - Receiving messages
 * - Deduplicating messages
 * - Decrementing TTL and forwarding to other peers
 */
class MeshRelayManager(
    private val context: Context,
    private val communicator: BluetoothCommunicator
) {
    // Unique identifier for THIS device on the mesh
    private val myDeviceId: String = communicator.uniqueName

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
        broadcastToMesh(message)
        
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
            Log.d("MeshRelay", "Dropping duplicate message: ${meshMessage.id}")
            return
        }
        
        // 2. Mark as seen
        seenMessages.add(meshMessage.id)
        
        Log.d("MeshRelay", "Received new message: ${meshMessage.id} (hop: ${meshMessage.hopCount})")

        // 3. Emit to local UI so user sees it
        _incomingMessages.tryEmit(meshMessage)

        // 4. Relay logic (TTL Gate)
        if (meshMessage.ttl > 0) {
            meshMessage.ttl -= 1
            meshMessage.hopCount += 1
            meshMessage.hopPath.add(myDeviceId)
            
            // Re-serialize and broadcast (excluding the peer that just sent it to us)
            // Note: BluetoothCommunicator's null receiver broadcasts to everyone. 
            // In a strict mesh, we would avoid sending it back to `rawBtMessage.sender`.
            // But since our deduplication handles echoes, broadcasting to everyone is safe and robust.
            broadcastToMesh(meshMessage)
        } else {
            Log.d("MeshRelay", "Message ${meshMessage.id} reached TTL 0. Dropping.")
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
