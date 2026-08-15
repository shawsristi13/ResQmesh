package com.meshmap.app

import android.app.Application
import com.bluetooth.communicator.BluetoothCommunicator
import com.bluetooth.communicator.tools.BluetoothTools
import com.meshmap.app.db.MeshDatabase
import com.meshmap.app.repository.MeshRepository
import java.util.Random

/**
 * Application subclass that holds the singleton BluetoothCommunicator instance.
 * This ensures BLE connections persist across activity recreations.
 */
class MeshMapApplication : Application() {

    var bluetoothCommunicator: BluetoothCommunicator? = null
        private set

    var meshRelayManager: com.meshmap.app.mesh.MeshRelayManager? = null
        private set

    lateinit var database: MeshDatabase
        private set

    lateinit var repository: MeshRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = MeshDatabase.getDatabase(this)
        repository = MeshRepository(database.meshMessageDao())
    }

    /**
     * Call this ONLY after BLUETOOTH_CONNECT permission is granted.
     * The underlying library requires permissions during initialization.
     */
    fun initializeMesh() {
        android.util.Log.e("MeshApp", "initializeMesh called")
        if (bluetoothCommunicator != null) {
            android.util.Log.e("MeshApp", "bluetoothCommunicator already initialized, returning")
            return
        }

        var name = android.os.Build.MODEL
        val supportedChars = BluetoothTools.getSupportedUTFCharacters(this)
        val allCharsSupported = name.all { supportedChars.contains(it) }

        if (!allCharsSupported || name.length > 18) {
            name = "Mesh${Random().nextInt(9999)}"
        }

        if (name.length > 14) {
            name = name.substring(0, 14)
        }
        
        android.util.Log.e("MeshApp", "Creating BluetoothCommunicator with name: $name")

        val communicator = BluetoothCommunicator(
            this,
            name,
            BluetoothCommunicator.STRATEGY_P2P_WITH_RECONNECTION
        )
        bluetoothCommunicator = communicator
        android.util.Log.e("MeshApp", "Creating MeshRelayManager")
        meshRelayManager = com.meshmap.app.mesh.MeshRelayManager(this, communicator, repository)
    }
}
