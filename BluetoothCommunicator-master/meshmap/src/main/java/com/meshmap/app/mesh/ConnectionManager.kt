package com.meshmap.app.mesh

import android.content.Context
import android.util.Log
import com.bluetooth.communicator.BluetoothCommunicator
import com.bluetooth.communicator.Message
import com.bluetooth.communicator.Peer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the BluetoothCommunicator lifecycle.
 * Automatically accepts incoming connections to form a mesh.
 */
class ConnectionManager(
    private val context: Context,
    private val communicator: BluetoothCommunicator,
    private val onMessageReceived: (Message) -> Unit
) {

    private val _connectedPeers = MutableStateFlow<List<Peer>>(emptyList())
    val connectedPeers: StateFlow<List<Peer>> = _connectedPeers.asStateFlow()

    private val communicatorCallback = object : BluetoothCommunicator.Callback() {
        override fun onAdvertiseStarted() {
            super.onAdvertiseStarted()
            Log.d("ConnectionManager", "Advertising started")
        }

        override fun onDiscoveryStarted() {
            super.onDiscoveryStarted()
            Log.d("ConnectionManager", "Discovery started")
        }

        override fun onPeerFound(peer: Peer) {
            super.onPeerFound(peer)
            val myId = communicator.uniqueName ?: ""
            val peerId = peer.uniqueName ?: ""
            Log.d("ConnectionManager", "Peer found: ${peer.name} ($peerId). My ID: $myId")
            
            // To prevent double connection collisions, only the device with the larger ID connects.
            // The other device will wait and accept the incoming connection.
            if (myId > peerId) {
                Log.d("ConnectionManager", "Initiating connection to $peerId")
                communicator.connect(peer)
            } else {
                Log.d("ConnectionManager", "Waiting for $peerId to initiate connection")
            }
        }

        override fun onConnectionRequest(peer: Peer) {
            super.onConnectionRequest(peer)
            Log.d("ConnectionManager", "Connection request from: ${peer.name}. Accepting...")
            // Auto-accept all incoming connection requests
            communicator.acceptConnection(peer)
        }

        override fun onConnectionSuccess(peer: Peer, source: Int) {
            super.onConnectionSuccess(peer, source)
            Log.d("ConnectionManager", "Connected to: ${peer.name}")
            updatePeersList()
        }

        override fun onConnectionLost(peer: Peer) {
            super.onConnectionLost(peer)
            Log.d("ConnectionManager", "Connection lost with: ${peer.name}")
            updatePeersList()
        }

        override fun onConnectionResumed(peer: Peer) {
            super.onConnectionResumed(peer)
            Log.d("ConnectionManager", "Connection resumed with: ${peer.name}")
            updatePeersList()
        }

        override fun onDisconnected(peer: Peer, peersLeft: Int) {
            super.onDisconnected(peer, peersLeft)
            Log.d("ConnectionManager", "Disconnected from: ${peer.name}")
            updatePeersList()
        }

        override fun onMessageReceived(message: Message, source: Int) {
            super.onMessageReceived(message, source)
            // Pass the raw Bluetooth library message up to the Relay Manager
            onMessageReceived.invoke(message)
        }
    }

    init {
        communicator.addCallback(communicatorCallback)
    }

    /**
     * Starts advertising and discovery simultaneously.
     */
    fun startMesh() {
        communicator.startAdvertising()
        communicator.startDiscovery()
    }

    /**
     * Stops advertising and discovery.
     */
    fun stopMesh() {
        communicator.stopAdvertising(true)
        communicator.stopDiscovery(true)
    }

    private fun updatePeersList() {
        val uniquePeers = communicator.connectedPeersList.distinctBy { it.uniqueName }
        _connectedPeers.value = uniquePeers
    }
}
