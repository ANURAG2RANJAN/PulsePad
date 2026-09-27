package com.antigravity.btremote.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.btremote.audio.SoundFeedbackEngine
import com.antigravity.btremote.hid.BluetoothHidManager
import com.antigravity.btremote.hid.HidConstants
import com.antigravity.btremote.model.ConnectionStatus
import com.antigravity.btremote.model.RemoteSettings
import com.antigravity.btremote.sensor.GyroAirMouse
import com.antigravity.btremote.theme.BgDark
import com.antigravity.btremote.theme.RoseError
import com.antigravity.btremote.ui.components.ModeSelectorBar
import com.antigravity.btremote.ui.components.PairingDialog
import com.antigravity.btremote.ui.components.RemoteMode
import com.antigravity.btremote.ui.components.SettingsDialog
import com.antigravity.btremote.ui.components.TopStatusBar
import com.antigravity.btremote.ui.screens.KeyboardScreen
import com.antigravity.btremote.ui.screens.MacroScreen
import com.antigravity.btremote.ui.screens.MediaRemoteScreen
import com.antigravity.btremote.ui.screens.NumpadScreen
import com.antigravity.btremote.ui.screens.TrackpadScreen

@Composable
fun MainScreen(
    hidManager: BluetoothHidManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status by hidManager.status.collectAsState()
    val pairedDevices by hidManager.pairedDevices.collectAsState()
    val reportRateHz by hidManager.telemetryReportHz.collectAsState()
    val packetsSent by hidManager.telemetryPacketsSent.collectAsState()

    var currentMode by remember { mutableStateOf(RemoteMode.TRACKPAD) }
    var settings by remember { mutableStateOf(RemoteSettings()) }
    var showPairingDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Gyroscope 6-DOF Air Mouse
    val airMouse = remember {
        GyroAirMouse(context) { dx, dy ->
            hidManager.sendMouseMove(dx, dy)
        }
    }
    val isAirMouseActive by airMouse.isActive.collectAsState()

    // Acoustic Sound Engine
    val soundEngine = remember(context) { SoundFeedbackEngine(context) }

    DisposableEffect(Unit) {
        onDispose {
            airMouse.stop()
            soundEngine.release()
        }
    }

    val triggerKeyPressSound: (Boolean) -> Unit = { isSpace ->
        if (settings.soundFeedback) {
            soundEngine.playKeyPress(settings.soundProfile, settings.soundVolume, isSpace)
        }
    }

    val triggerMouseSound: () -> Unit = {
        if (settings.soundFeedback && settings.mouseClickSound) {
            soundEngine.playMouseClick(settings.soundVolume)
        }
    }

    // Vibrator Setup
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val triggerHaptic: () -> Unit = {
        if (settings.hapticFeedback) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(14, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(14)
                }
            } catch (_: Exception) {}
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            hidManager.init()
        }
    }

    // Discoverable Launcher
    val discoverableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hidManager.refreshPairedDevices()
    }

    // Check & request permissions on start
    LaunchedEffect(Unit) {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(perms)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Top App Bar & Status
            TopStatusBar(
                status = status,
                targetPlatform = settings.targetPlatform,
                onPlatformChanged = { settings = settings.copy(targetPlatform = it) },
                onConnectClick = {
                    hidManager.refreshPairedDevices()
                    showPairingDialog = true
                },
                onSettingsClick = { showSettingsDialog = true }
            )

            // Mode Selector Tabs (Trackpad, Keyboard, Media, Numpad, Macros)
            ModeSelectorBar(
                currentMode = currentMode,
                onModeSelected = {
                    currentMode = it
                    triggerHaptic()
                }
            )

            // Notice Banner if Disconnected or Missing Permission
            if (status !is ConnectionStatus.Connected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .background(RoseError.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (status) {
                            is ConnectionStatus.Connecting -> "Connecting to ${settings.targetPlatform.displayName}..."
                            is ConnectionStatus.BluetoothOff -> "Bluetooth is OFF. Please turn on Bluetooth."
                            is ConnectionStatus.PermissionMissing -> "Bluetooth permissions required to connect."
                            is ConnectionStatus.HidUnsupported -> "HID Device Profile not supported on this phone."
                            else -> "${settings.targetPlatform.shortName} Not Connected - Tap 'Disconnected' above to pair & connect."
                        },
                        color = RoseError,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Mode Content with Animated Transition
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "modeTransition"
                ) { mode ->
                    when (mode) {
                        RemoteMode.TRACKPAD -> TrackpadScreen(
                            settings = settings,
                            isAirMouseAvailable = airMouse.isAvailable,
                            isAirMouseActive = isAirMouseActive,
                            reportRateHz = reportRateHz,
                            packetsSent = packetsSent,
                            onToggleAirMouse = {
                                if (isAirMouseActive) airMouse.stop() else airMouse.start()
                            },
                            onMouseMove = { dx, dy -> hidManager.sendMouseMove(dx, dy) },
                            onMouseClick = { button ->
                                hidManager.sendMouseClick(button)
                                triggerMouseSound()
                                triggerHaptic()
                            },
                            onMouseButtonDown = { button, isDown ->
                                hidManager.setMouseButtonDown(button, isDown)
                                if (isDown) triggerMouseSound()
                            },
                            onMouseWheel = { wheel -> hidManager.sendMouseWheel(wheel) },
                            onMultiKey = { mod, keys -> hidManager.sendMultiKeyCombo(mod, *keys) },
                            onConsumerKey = { code ->
                                hidManager.sendConsumerKey(code)
                                triggerKeyPressSound(false)
                            },
                            onKeyPress = { key, mod ->
                                hidManager.sendKeyPress(key, mod)
                                triggerKeyPressSound(false)
                            },
                            onKeyCombo = { mod, key ->
                                hidManager.sendKeyCombo(mod, key)
                                triggerKeyPressSound(false)
                            },
                            onHaptic = triggerHaptic,
                            onMouseSound = triggerMouseSound
                        )
                        RemoteMode.KEYBOARD -> KeyboardScreen(
                            targetPlatform = settings.targetPlatform,
                            stickyCtrl = hidManager.stickyCtrl,
                            stickyAlt = hidManager.stickyAlt,
                            stickyShift = hidManager.stickyShift,
                            stickyGui = hidManager.stickyGui,
                            colorTheme = settings.keyboardColorTheme,
                            fontOption = settings.keyboardFont,
                            onColorThemeChanged = { settings = settings.copy(keyboardColorTheme = it) },
                            onFontOptionChanged = { settings = settings.copy(keyboardFont = it) },
                            onToggleCtrl = { hidManager.stickyCtrl = !hidManager.stickyCtrl },
                            onToggleAlt = { hidManager.stickyAlt = !hidManager.stickyAlt },
                            onToggleShift = { hidManager.stickyShift = !hidManager.stickyShift },
                            onToggleGui = { hidManager.stickyGui = !hidManager.stickyGui },
                            onKeyDown = { key, mod ->
                                hidManager.sendKeyDown(key, mod)
                                triggerKeyPressSound(key == HidConstants.KEY_SPACE || key == HidConstants.KEY_ENTER)
                            },
                            onKeyUp = { key ->
                                hidManager.sendKeyUp(key)
                            },
                            onKeyPress = { key, mod ->
                                hidManager.sendKeyPress(key, mod)
                                triggerKeyPressSound(key == HidConstants.KEY_SPACE || key == HidConstants.KEY_ENTER)
                            },
                            onKeyCombo = { mod, key ->
                                hidManager.sendKeyCombo(mod, key)
                                triggerKeyPressSound(false)
                            },
                            onSendText = { text ->
                                hidManager.typeText(text)
                                triggerKeyPressSound(false)
                            },
                            onHaptic = triggerHaptic
                        )
                        RemoteMode.MEDIA -> MediaRemoteScreen(
                            onConsumerKey = { code ->
                                hidManager.sendConsumerKey(code)
                                triggerKeyPressSound(false)
                            },
                            onKeyPress = { key, mod ->
                                hidManager.sendKeyPress(key, mod)
                                triggerKeyPressSound(false)
                            },
                            onKeyCombo = { mod, key ->
                                hidManager.sendKeyCombo(mod, key)
                                triggerKeyPressSound(false)
                            },
                            onHaptic = triggerHaptic
                        )
                        RemoteMode.NUMPAD -> NumpadScreen(
                            onKeyPress = { key, mod ->
                                hidManager.sendKeyPress(key, mod)
                                triggerKeyPressSound(key == HidConstants.KEY_ENTER)
                            },
                            onHaptic = triggerHaptic
                        )
                        RemoteMode.MACROS -> MacroScreen(
                            onMultiKey = { mod, keys ->
                                hidManager.sendMultiKeyCombo(mod, *keys)
                                triggerKeyPressSound(false)
                            },
                            onSendText = { text ->
                                hidManager.typeText(text)
                                triggerKeyPressSound(false)
                            },
                            onHaptic = triggerHaptic
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showPairingDialog) {
        PairingDialog(
            pairedDevices = pairedDevices,
            phoneName = hidManager.bluetoothAdapter?.name ?: "PulsePad Remote",
            selectedPlatform = settings.targetPlatform,
            onPlatformSelected = { settings = settings.copy(targetPlatform = it) },
            onDeviceSelected = { address ->
                hidManager.connectToDevice(address)
                showPairingDialog = false
            },
            onMakeDiscoverable = {
                val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                }
                discoverableLauncher.launch(discoverableIntent)
            },
            onOpenBtSettings = {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                context.startActivity(intent)
            },
            onRefresh = { hidManager.refreshPairedDevices() },
            onDisconnect = {
                hidManager.disconnect()
                showPairingDialog = false
            },
            hasConnectedDevice = status is ConnectionStatus.Connected,
            onDismiss = { showPairingDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onSettingsChanged = { settings = it },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
