package com.antigravity.btremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.antigravity.btremote.hid.BluetoothHidManager
import com.antigravity.btremote.theme.BluetoothRemoteTheme
import com.antigravity.btremote.ui.MainScreen

class MainActivity : ComponentActivity() {
    private lateinit var hidManager: BluetoothHidManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hidManager = BluetoothHidManager(applicationContext)

        enableEdgeToEdge()
        setContent {
            BluetoothRemoteTheme {
                MainScreen(hidManager = hidManager)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        hidManager.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        hidManager.cleanup()
    }
}
