package com.meshmap.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.meshmap.app.ui.navigation.MeshNavigation
import com.meshmap.app.ui.theme.DeepNavy
import com.meshmap.app.ui.theme.MeshMapTheme

class MeshMapActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "BLE permissions granted — mesh ready", Toast.LENGTH_SHORT).show()
            startMesh()
        } else {
            Toast.makeText(this, "BLE permissions required for mesh communication", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.e("MeshApp", "MeshMapActivity onCreate")

        // Request permissions on launch
        if (!PermissionHelper.hasAllPermissions(this)) {
            android.util.Log.e("MeshApp", "Permissions missing, requesting...")
            permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
        } else {
            android.util.Log.e("MeshApp", "Permissions granted, starting mesh")
            startMesh()
        }

        setContent {
            MeshMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepNavy
                ) {
                    MeshNavigation()
                }
            }
        }
    }

    private fun startMesh() {
        val app = application as MeshMapApplication
        app.initializeMesh()
        app.meshRelayManager?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.e("MeshApp", "MeshMapActivity onDestroy, stopping mesh...")
        val app = application as MeshMapApplication
        app.meshRelayManager?.stop()
    }
}
