package com.meshmap.app.mesh

data class MeshPeer(
    val uniqueName: String,
    val name: String,
    val isConnected: Boolean,
    val isHardwareConnected: Boolean
)
