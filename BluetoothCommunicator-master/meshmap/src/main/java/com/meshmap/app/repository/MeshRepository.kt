package com.meshmap.app.repository

import com.meshmap.app.db.MeshMessageDao
import com.meshmap.app.db.MeshMessageEntity
import com.meshmap.app.mesh.MeshMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeshRepository(private val messageDao: MeshMessageDao) {

    val allMessages: Flow<List<MeshMessage>> = messageDao.getAllMessages().map { entities ->
        entities.map { it.toMeshMessage() }
    }

    suspend fun insertMessage(message: MeshMessage) {
        messageDao.insertMessage(MeshMessageEntity.fromMeshMessage(message))
    }

    suspend fun clearMessages() {
        messageDao.clearMessages()
    }
}
