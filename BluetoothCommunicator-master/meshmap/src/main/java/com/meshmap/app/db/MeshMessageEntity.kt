package com.meshmap.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meshmap.app.mesh.MeshMessage
import com.meshmap.app.mesh.MeshMessageType

@Entity(tableName = "messages")
data class MeshMessageEntity(
    @PrimaryKey val id: String,
    val type: String,
    val originDeviceId: String,
    val originName: String,
    val payload: String,
    val timestamp: Long,
    val urgency: Int
) {
    fun toMeshMessage(): MeshMessage {
        return MeshMessage(
            id = id,
            type = MeshMessageType.valueOf(type),
            originDeviceId = originDeviceId,
            originName = originName,
            payload = payload,
            timestamp = timestamp,
            urgency = urgency
        )
    }

    companion object {
        fun fromMeshMessage(message: MeshMessage): MeshMessageEntity {
            return MeshMessageEntity(
                id = message.id,
                type = message.type.name,
                originDeviceId = message.originDeviceId,
                originName = message.originName,
                payload = message.payload,
                timestamp = message.timestamp,
                urgency = message.urgency
            )
        }
    }
}
