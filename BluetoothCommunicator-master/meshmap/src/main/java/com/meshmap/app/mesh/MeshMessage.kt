package com.meshmap.app.mesh

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents a single message packet that travels across the mesh network.
 */
data class MeshMessage(
    val id: String = UUID.randomUUID().toString(),
    val type: MeshMessageType,
    val originDeviceId: String,
    val originName: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
    var ttl: Int = 5,
    var hopCount: Int = 0,
    val hopPath: MutableList<String> = mutableListOf(),
    val urgency: Int = 0 // 0=normal, 1=urgent, 2=critical
) {
    /**
     * Serializes this message into a compact JSON string for BLE transmission.
     */
    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("type", type.name)
        json.put("originDeviceId", originDeviceId)
        json.put("originName", originName)
        json.put("payload", payload)
        json.put("timestamp", timestamp)
        json.put("ttl", ttl)
        json.put("hopCount", hopCount)
        
        val pathArray = JSONArray()
        hopPath.forEach { pathArray.put(it) }
        json.put("hopPath", pathArray)
        
        json.put("urgency", urgency)
        return json.toString()
    }

    companion object {
        /**
         * Deserializes a JSON string back into a MeshMessage.
         */
        fun fromJson(jsonString: String): MeshMessage? {
            return try {
                val json = JSONObject(jsonString)
                val pathArray = json.getJSONArray("hopPath")
                val pathList = mutableListOf<String>()
                for (i in 0 until pathArray.length()) {
                    pathList.add(pathArray.getString(i))
                }

                MeshMessage(
                    id = json.getString("id"),
                    type = MeshMessageType.valueOf(json.getString("type")),
                    originDeviceId = json.getString("originDeviceId"),
                    originName = json.getString("originName"),
                    payload = json.getString("payload"),
                    timestamp = json.getLong("timestamp"),
                    ttl = json.getInt("ttl"),
                    hopCount = json.getInt("hopCount"),
                    hopPath = pathList,
                    urgency = json.getInt("urgency")
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
