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
            Log.e("ConnectionManager", "Advertising started")
        }

        override fun onDiscoveryStarted() {
            super.onDiscoveryStarted()
            Log.e("ConnectionManager", "Discovery started")
        }

        override fun onPeerFound(peer: Peer) {
            super.onPeerFound(peer)
            val myId = communicator.uniqueName ?: ""
            val peerId = peer.uniqueName ?: ""
            Log.e("ConnectionManager", "Peer found: ${peer.name} ($peerId). My ID: $myId")
            
            // To prevent double connection collisions, only the device with the larger ID connects.
            // The other device will wait and accept the incoming connection.
            if (myId > peerId) {
                Log.e("ConnectionManager", "Initiating connection to $peerId")
                communicator.connect(peer)
            } else {
                Log.e("ConnectionManager", "Waiting for $peerId to initiate connection")
            }
        }

        override fun onConnectionRequest(peer: Peer) {
            super.onConnectionRequest(peer)
            Log.e("ConnectionManager", "Connection request from: ${peer.name}. Accepting...")
            // Auto-accept all incoming connection requests
            communicator.acceptConnection(peer)
        }

        override fun onConnectionSuccess(peer: Peer, source: Int) {
            super.onConnectionSuccess(peer, source)
            Log.e("ConnectionManager", "Connected to: ${peer.name}")
            updatePeersList()
        }

        override fun onConnectionLost(peer: Peer) {
            super.onConnectionLost(peer)
            Log.e("ConnectionManager", "Connection lost with: ${peer.name}")
            updatePeersList()
        }

        override fun onConnectionResumed(peer: Peer) {
            super.onConnectionResumed(peer)
            Log.e("ConnectionManager", "Connection resumed with: ${peer.name}")
            updatePeersList()
        }

        override fun onDisconnected(peer: Peer, peersLeft: Int) {
            super.onDisconnected(peer, peersLeft)
            Log.e("ConnectionManager", "Disconnected from: ${peer.name}")
            updatePeersList()
        }

        override fun onConnectionFailed(peer: Peer, errorCode: Int) {
            super.onConnectionFailed(peer, errorCode)
            Log.e("ConnectionManager", "Connection failed with: ${peer.name}, errorCode: $errorCode")
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
        android.util.Log.e("ConnectionManager", "startMesh called, starting advertising and discovery")
        val advStatus = communicator.startAdvertising()
        val discStatus = communicator.startDiscovery()
        android.util.Log.e("ConnectionManager", "startMesh results -> Advertising: $advStatus, Discovery: $discStatus")
    }

    /**
     * Stops advertising and discovery.
     */
    fun stopMesh() {
        communicator.stopAdvertising(true)
        communicator.stopDiscovery(true)
    }

    private fun updatePeersList() {
        val allPeers = communicator.connectedPeersList
        val uniquePeers = allPeers.distinctBy { it.uniqueName }
        Log.e("ConnectionManager", "updatePeersList: total=${allPeers.size}, unique=${uniquePeers.size}")
        allPeers.forEach { 
            Log.e("ConnectionManager", "Peer in list: name=${it.name}, uniqueName=${it.uniqueName}, isConnected=${it.isConnected}")
        }
        _connectedPeers.value = uniquePeers
    }
}
