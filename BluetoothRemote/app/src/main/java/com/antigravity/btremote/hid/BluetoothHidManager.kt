package com.antigravity.btremote.hid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.antigravity.btremote.model.ConnectionStatus
import com.antigravity.btremote.model.PairedDeviceItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class BluetoothHidManager(private val context: Context) {
    private val TAG = "BluetoothHidManager"
    private val executor = Executors.newSingleThreadExecutor()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    var hidDevice: BluetoothHidDevice? = null
        private set
    var currentDevice: BluetoothDevice? = null
        private set
    var isAppRegistered: Boolean = false
        private set

    private val _status = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<PairedDeviceItem>>(emptyList())
    val pairedDevices: StateFlow<List<PairedDeviceItem>> = _pairedDevices.asStateFlow()

    // Scientific Telemetry & Diagnostics
    private val _telemetryPacketsSent = MutableStateFlow(0L)
    val telemetryPacketsSent: StateFlow<Long> = _telemetryPacketsSent.asStateFlow()

    private val _telemetryReportHz = MutableStateFlow(0)
    val telemetryReportHz: StateFlow<Int> = _telemetryReportHz.asStateFlow()

    private var packetWindowCount = 0
    private var lastHzCalculatedTimeMs = System.currentTimeMillis()

    // Sticky modifier keys for keyboard
    var stickyCtrl = false
    var stickyAlt = false
    var stickyShift = false
    var stickyGui = false

    // Mouse button state (for drag & drop)
    var currentMouseButtonMask: Byte = 0x00

    // Set of currently pressed keys (for true 6-Key Rollover)
    val activeKeys: MutableSet<Byte> = java.util.Collections.synchronizedSet(mutableSetOf<Byte>())

    private fun recordTelemetry() {
        _telemetryPacketsSent.value++
        packetWindowCount++
        val now = System.currentTimeMillis()
        val dt = now - lastHzCalculatedTimeMs
        if (dt >= 1000) {
            _telemetryReportHz.value = ((packetWindowCount * 1000L) / dt).toInt()
            packetWindowCount = 0
            lastHzCalculatedTimeMs = now
        }
    }

    private var isReceiverRegistered = false
    private val bluetoothReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            Log.d(TAG, "Bluetooth broadcast: $action")
            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_ON -> {
                            Log.d(TAG, "Bluetooth turned ON, re-initializing HID")
                            _status.value = ConnectionStatus.Disconnected
                            init()
                        }
                        BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF -> {
                            Log.d(TAG, "Bluetooth turned OFF")
                            _status.value = ConnectionStatus.BluetoothOff
                            currentDevice = null
                            isAppRegistered = false
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val dev = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    val prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
                    Log.d(TAG, "Bond state changed for ${dev?.name}: $prevBondState -> $bondState")
                    refreshPairedDevices()
                    if (bondState == BluetoothDevice.BOND_BONDED && dev != null) {
                        if (currentDevice == null) {
                            Log.d(TAG, "Newly paired device bonded, auto-connecting to ${dev.name}")
                            scope.launch {
                                delay(800)
                                connectToDevice(dev.address)
                            }
                        }
                    } else if (bondState == BluetoothDevice.BOND_NONE && dev != null) {
                        if (currentDevice?.address == dev.address) {
                            currentDevice = null
                            _status.value = ConnectionStatus.Disconnected
                        }
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val dev = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    Log.d(TAG, "ACL Connected: ${dev?.name} (${dev?.address})")
                    checkConnectedDevices()
                    refreshPairedDevices()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val dev = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    Log.d(TAG, "ACL Disconnected: ${dev?.name}")
                    if (dev != null && dev.address == currentDevice?.address) {
                        currentDevice = null
                        activeKeys.clear()
                        currentMouseButtonMask = 0x00
                        _status.value = ConnectionStatus.Disconnected
                    }
                    refreshPairedDevices()
                }
            }
        }
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(bluetoothReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    context.registerReceiver(bluetoothReceiver, filter)
                }
                isReceiverRegistered = true
                Log.d(TAG, "Bluetooth BroadcastReceiver registered")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register Bluetooth BroadcastReceiver", e)
            }
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(bluetoothReceiver)
                isReceiverRegistered = false
                Log.d(TAG, "Bluetooth BroadcastReceiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister Bluetooth BroadcastReceiver", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun checkConnectedDevices() {
        val hid = hidDevice ?: return
        try {
            val connected = hid.connectedDevices
            if (!connected.isNullOrEmpty()) {
                val dev = connected.first()
                currentDevice = dev
                _status.value = ConnectionStatus.Connected(
                    deviceName = dev.name ?: "Connected PC",
                    address = dev.address
                )
                Log.d(TAG, "Found active connected HID device: ${dev.name} (${dev.address})")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission missing checking connected devices", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking connected devices", e)
        }
    }

    private val profileListener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID_DEVICE profile proxy connected")
                hidDevice = proxy as BluetoothHidDevice
                isInitializing = false
                registerHidApp()
                checkConnectedDevices()
                refreshPairedDevices()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID_DEVICE profile proxy disconnected")
                hidDevice = null
                isAppRegistered = false
                isInitializing = false
                activeKeys.clear()
                currentMouseButtonMask = 0x00
                _status.value = ConnectionStatus.Disconnected
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        @SuppressLint("MissingPermission")
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "onAppStatusChanged: registered=$registered, device=${pluggedDevice?.name}")
            isAppRegistered = registered
            if (registered) {
                checkConnectedDevices()
                if (pluggedDevice != null) {
                    currentDevice = pluggedDevice
                    _status.value = ConnectionStatus.Connected(
                        deviceName = pluggedDevice.name ?: "Connected PC",
                        address = pluggedDevice.address
                    )
                }
            } else {
                Log.w(TAG, "onAppStatusChanged: HID App not registered")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d(TAG, "onConnectionStateChanged: state=$state, device=${device?.name} (${device?.address})")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    currentDevice = device
                    _status.value = ConnectionStatus.Connected(
                        deviceName = device?.name ?: "Connected PC",
                        address = device?.address ?: ""
                    )
                    refreshPairedDevices()
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _status.value = ConnectionStatus.Connecting(device?.name ?: "PC")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    _status.value = ConnectionStatus.Disconnecting
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (currentDevice?.address == device?.address) {
                        currentDevice = null
                        activeKeys.clear()
                        currentMouseButtonMask = 0x00
                        _status.value = ConnectionStatus.Disconnected
                    }
                    refreshPairedDevices()
                }
            }
        }

        override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
            Log.d(TAG, "onSetProtocol: protocol=$protocol")
            hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice?) {
            Log.d(TAG, "onVirtualCableUnplug: $device")
            currentDevice = null
            activeKeys.clear()
            currentMouseButtonMask = 0x00
            _status.value = ConnectionStatus.Disconnected
            refreshPairedDevices()
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            Log.d(TAG, "onGetReport: id=$id, type=$type")
            when (id.toInt()) {
                HidConstants.REPORT_ID_KEYBOARD -> {
                    val report = ByteArray(8)
                    report[0] = getEffectiveModifiers()
                    synchronized(activeKeys) {
                        activeKeys.take(6).forEachIndexed { i, k ->
                            report[2 + i] = k
                        }
                    }
                    hidDevice?.replyReport(device, type, id, report)
                }
                HidConstants.REPORT_ID_MOUSE -> {
                    val report = byteArrayOf(currentMouseButtonMask, 0, 0, 0)
                    hidDevice?.replyReport(device, type, id, report)
                }
                HidConstants.REPORT_ID_CONSUMER -> {
                    hidDevice?.replyReport(device, type, id, ByteArray(2))
                }
                0 -> {
                    val report = ByteArray(8)
                    report[0] = getEffectiveModifiers()
                    hidDevice?.replyReport(device, type, HidConstants.REPORT_ID_KEYBOARD.toByte(), report)
                }
                else -> {
                    hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_INVALID_RPT_ID)
                }
            }
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
        }
    }

    private var isInitializing = false

    fun init() {
        registerReceiver()
        val adapter = bluetoothAdapter
        if (adapter == null) {
            _status.value = ConnectionStatus.Error("Bluetooth not available on this device")
            return
        }
        if (!adapter.isEnabled) {
            _status.value = ConnectionStatus.BluetoothOff
            return
        }

        if (hidDevice != null && isAppRegistered) {
            Log.d(TAG, "HID Device already registered, checking connected devices")
            checkConnectedDevices()
            refreshPairedDevices()
            return
        }

        if (isInitializing) {
            Log.d(TAG, "HID Device initialization already in progress, skipping duplicate")
            return
        }

        isInitializing = true
        try {
            val supported = adapter.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
            if (!supported) {
                _status.value = ConnectionStatus.HidUnsupported
                isInitializing = false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission missing", e)
            _status.value = ConnectionStatus.PermissionMissing
            isInitializing = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get HID profile proxy", e)
            _status.value = ConnectionStatus.Error(e.message ?: "Failed to initialize HID profile")
            isInitializing = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerHidApp() {
        val hid = hidDevice ?: return
        if (isAppRegistered) {
            Log.d(TAG, "App is already registered as HID device")
            return
        }
        try {
            val sdp = BluetoothHidDeviceAppSdpSettings(
                "PulsePad PC Remote",
                "Bluetooth PC Keyboard & Mouse Remote",
                "PulsePad",
                BluetoothHidDevice.SUBCLASS1_COMBO,
                HidConstants.HID_REPORT_DESCRIPTOR
            )
            val success = hid.registerApp(sdp, null, null, executor, hidCallback)
            Log.d(TAG, "registerApp called: success=$success")
        } catch (e: SecurityException) {
            _status.value = ConnectionStatus.PermissionMissing
        } catch (e: Exception) {
            Log.e(TAG, "Error registering HID app", e)
        }
    }

    private fun detectPlatformFromName(name: String): com.antigravity.btremote.model.TargetPlatform {
        val lower = name.lowercase()
        return when {
            lower.contains("ipad") || lower.contains("iphone") || lower.contains("macbook") || lower.contains("apple") || lower.contains("mac") -> com.antigravity.btremote.model.TargetPlatform.APPLE
            lower.contains("tv") || lower.contains("fire") || lower.contains("chromecast") || lower.contains("shield") || lower.contains("bravia") || lower.contains("roku") -> com.antigravity.btremote.model.TargetPlatform.SMART_TV
            lower.contains("android") || lower.contains("galaxy") || lower.contains("tab") || lower.contains("pixel") || lower.contains("vivo") || lower.contains("oppo") || lower.contains("xiaomi") || lower.contains("redmi") || lower.contains("realme") || lower.contains("oneplus") -> com.antigravity.btremote.model.TargetPlatform.ANDROID
            else -> com.antigravity.btremote.model.TargetPlatform.WINDOWS
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val adapter = bluetoothAdapter ?: return
        try {
            val bonded = adapter.bondedDevices ?: emptySet()
            val connectedAddress = currentDevice?.address ?: hidDevice?.connectedDevices?.firstOrNull()?.address
            val list = bonded.map { dev ->
                val devName = dev.name ?: "Unknown Device"
                PairedDeviceItem(
                    name = devName,
                    address = dev.address,
                    isConnected = dev.address == connectedAddress,
                    platform = detectPlatformFromName(devName)
                )
            }
            _pairedDevices.value = list
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission missing for bonded devices", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) {
            _status.value = ConnectionStatus.BluetoothOff
            return
        }
        val hid = hidDevice
        if (hid == null) {
            _status.value = ConnectionStatus.Error("HID Profile not ready. Initializing...")
            init()
            return
        }

        try {
            val device = adapter.getRemoteDevice(address)
            
            // Check if already connected
            val isAlreadyConnected = currentDevice?.address == address || 
                hid.connectedDevices?.any { it.address == address } == true
            if (isAlreadyConnected) {
                currentDevice = device
                _status.value = ConnectionStatus.Connected(device.name ?: "Connected PC", address)
                refreshPairedDevices()
                return
            }

            // Check bond state
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                _status.value = ConnectionStatus.Connecting("Pairing with ${device.name ?: "PC"}...")
                val bonding = device.createBond()
                Log.d(TAG, "Device not bonded, initiating createBond(): $bonding")
                return
            }

            _status.value = ConnectionStatus.Connecting(device.name ?: "PC")
            val success = hid.connect(device)
            Log.d(TAG, "Connecting to ${device.name} ($address): success=$success")
            
            if (!success) {
                // If connect returns false, check if device was actually already connected
                if (hid.connectedDevices?.any { it.address == address } == true) {
                    currentDevice = device
                    _status.value = ConnectionStatus.Connected(device.name ?: "Connected PC", address)
                    refreshPairedDevices()
                    return
                }

                _status.value = ConnectionStatus.Error("Connection rejected. Ensure PC has Bluetooth ON and is paired.")
                scope.launch {
                    delay(3500)
                    if (_status.value is ConnectionStatus.Error) {
                        _status.value = ConnectionStatus.Disconnected
                    }
                }
                return
            }

            // Connection watchdog timeout: if still Connecting after 12s, abort
            scope.launch {
                delay(12000)
                if (_status.value is ConnectionStatus.Connecting) {
                    Log.w(TAG, "Connection attempt timed out for $address")
                    _status.value = ConnectionStatus.Error("Connection timed out. Open Windows Bluetooth Settings and click Connect.")
                    delay(3500)
                    if (_status.value is ConnectionStatus.Error) {
                        _status.value = ConnectionStatus.Disconnected
                    }
                }
            }
        } catch (e: SecurityException) {
            _status.value = ConnectionStatus.PermissionMissing
        } catch (e: Exception) {
            _status.value = ConnectionStatus.Error("Failed to connect: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: hid.connectedDevices?.firstOrNull() ?: return
        try {
            _status.value = ConnectionStatus.Disconnecting
            val success = hid.disconnect(dev)
            Log.d(TAG, "hid.disconnect: success=$success")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }

    // --- MOUSE EMULATION ---

    @SuppressLint("MissingPermission")
    fun sendMouseMove(dx: Int, dy: Int, wheel: Int = 0) {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return

        val clampedDx = dx.coerceIn(-127, 127).toByte()
        val clampedDy = dy.coerceIn(-127, 127).toByte()
        val clampedWheel = wheel.coerceIn(-127, 127).toByte()

        val report = byteArrayOf(currentMouseButtonMask, clampedDx, clampedDy, clampedWheel)
        hid.sendReport(dev, HidConstants.REPORT_ID_MOUSE, report)
        recordTelemetry()
    }

    @SuppressLint("MissingPermission")
    fun sendMouseClick(buttonMask: Byte) {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return

        scope.launch {
            // Button down
            val reportDown = byteArrayOf(buttonMask, 0, 0, 0)
            hid.sendReport(dev, HidConstants.REPORT_ID_MOUSE, reportDown)
            recordTelemetry()

            delay(25) // Hold click for 25ms for clean PC debounce

            // Button up
            val reportUp = byteArrayOf(0, 0, 0, 0)
            hid.sendReport(dev, HidConstants.REPORT_ID_MOUSE, reportUp)
            recordTelemetry()
        }
    }

    @SuppressLint("MissingPermission")
    fun setMouseButtonDown(buttonMask: Byte, isDown: Boolean) {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return

        currentMouseButtonMask = if (isDown) {
            (currentMouseButtonMask.toInt() or buttonMask.toInt()).toByte()
        } else {
            (currentMouseButtonMask.toInt() and buttonMask.toInt().inv()).toByte()
        }

        val report = byteArrayOf(currentMouseButtonMask, 0, 0, 0)
        hid.sendReport(dev, HidConstants.REPORT_ID_MOUSE, report)
        recordTelemetry()
    }

    @SuppressLint("MissingPermission")
    fun sendMouseWheel(wheelDelta: Int) {
        sendMouseMove(0, 0, wheelDelta)
    }

    // --- KEYBOARD EMULATION ---

    fun getEffectiveModifiers(): Byte {
        var mods = 0
        if (stickyCtrl) mods = mods or HidConstants.MOD_LCTRL.toInt()
        if (stickyAlt) mods = mods or HidConstants.MOD_LALT.toInt()
        if (stickyShift) mods = mods or HidConstants.MOD_LSHIFT.toInt()
        if (stickyGui) mods = mods or HidConstants.MOD_LGUI.toInt()
        return mods.toByte()
    }

    @SuppressLint("MissingPermission")
    fun sendKeyboardReport() {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return
        val report = ByteArray(8)
        report[0] = getEffectiveModifiers()
        report[1] = 0 // reserved
        synchronized(activeKeys) {
            activeKeys.take(6).forEachIndexed { i, k ->
                report[2 + i] = k
            }
        }
        hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, report)
        recordTelemetry()
    }

    private val keyPressTimestamps = java.util.concurrent.ConcurrentHashMap<Byte, Long>()

    fun sendKeyDown(key: Byte, customModifier: Byte? = null) {
        if (key == HidConstants.KEY_NONE) return
        keyPressTimestamps[key] = System.currentTimeMillis()
        synchronized(activeKeys) {
            activeKeys.add(key)
        }
        sendKeyboardReport()
    }

    fun sendKeyUp(key: Byte) {
        scope.launch {
            val pressedTime = keyPressTimestamps.remove(key) ?: 0L
            val elapsed = System.currentTimeMillis() - pressedTime
            if (elapsed < 28) {
                delay(28 - elapsed)
            }
            synchronized(activeKeys) {
                activeKeys.remove(key)
            }
            sendKeyboardReport()
        }
    }

    fun releaseAllKeys() {
        synchronized(activeKeys) {
            activeKeys.clear()
        }
        sendKeyboardReport()
    }

    @SuppressLint("MissingPermission")
    fun sendKeyPress(key: Byte, customModifier: Byte? = null) {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return

        scope.launch {
            val mod = customModifier ?: getEffectiveModifiers()
            val reportDown = byteArrayOf(mod, 0, key, 0, 0, 0, 0, 0)
            hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, reportDown)
            recordTelemetry()

            // Essential 30ms hold duration so PC debouncing registers the keystroke
            delay(30)

            val reportUp = byteArrayOf(if (customModifier != null) 0 else getEffectiveModifiers(), 0, 0, 0, 0, 0, 0, 0)
            hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, reportUp)
            recordTelemetry()
        }
    }

    @SuppressLint("MissingPermission")
    fun sendKeyCombo(modifier: Byte, key: Byte) {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return

        scope.launch {
            val reportDown = byteArrayOf(modifier, 0, key, 0, 0, 0, 0, 0)
            hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, reportDown)
            recordTelemetry()

            delay(35) // Hold combo for 35ms

            val reportUp = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
            hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, reportUp)
            recordTelemetry()
        }
    }

    @SuppressLint("MissingPermission")
    fun sendMultiKeyCombo(modifier: Byte, vararg keys: Byte) {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return

        scope.launch {
            val reportDown = ByteArray(8)
            reportDown[0] = modifier
            keys.take(6).forEachIndexed { i, k ->
                reportDown[2 + i] = k
            }
            hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, reportDown)
            recordTelemetry()

            delay(40) // Hold multi-key combo for 40ms

            val reportUp = ByteArray(8)
            hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, reportUp)
            recordTelemetry()
        }
    }

    @SuppressLint("MissingPermission")
    fun typeText(text: String, onProgress: ((Int, Int) -> Unit)? = null) {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return

        scope.launch {
            for ((index, c) in text.withIndex()) {
                val mapping = HidConstants.charToHid(c)
                if (mapping != null) {
                    val reportDown = byteArrayOf(mapping.first, 0, mapping.second, 0, 0, 0, 0, 0)
                    hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, reportDown)
                    delay(20)
                    val reportUp = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
                    hid.sendReport(dev, HidConstants.REPORT_ID_KEYBOARD, reportUp)
                    delay(20)
                }
                onProgress?.invoke(index + 1, text.length)
            }
        }
    }

    // --- CONSUMER (MEDIA) EMULATION ---

    @SuppressLint("MissingPermission")
    fun sendConsumerKey(code: Short) {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return

        scope.launch {
            val lsb = (code.toInt() and 0xFF).toByte()
            val msb = ((code.toInt() shr 8) and 0xFF).toByte()

            val reportDown = byteArrayOf(lsb, msb)
            hid.sendReport(dev, HidConstants.REPORT_ID_CONSUMER, reportDown)

            delay(35)

            val reportUp = byteArrayOf(0, 0)
            hid.sendReport(dev, HidConstants.REPORT_ID_CONSUMER, reportUp)
        }
    }

    @SuppressLint("MissingPermission")
    fun cleanup() {
        unregisterReceiver()
        val hid = hidDevice ?: return
        try {
            hid.unregisterApp()
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hid)
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up HID", e)
        }
        hidDevice = null
        isAppRegistered = false
        isInitializing = false
    }
}
