package com.meshmap.app.mesh

/**
 * Defines the type of payload the MeshMessage carries.
 */
enum class MeshMessageType {
    SOS,            // High-urgency alert
    CHAT,           // Standard text message
    ACK,            // Acknowledgment of an SOS
    PEER_ANNOUNCE   // System message to announce presence
}
