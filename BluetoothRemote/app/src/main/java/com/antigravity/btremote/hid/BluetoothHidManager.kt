package com.antigravity.btremote.hid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
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

    private val profileListener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID_DEVICE profile proxy connected")
                hidDevice = proxy as BluetoothHidDevice
                registerHidApp()
                refreshPairedDevices()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID_DEVICE profile proxy disconnected")
                hidDevice = null
                isAppRegistered = false
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
            if (registered && pluggedDevice != null) {
                currentDevice = pluggedDevice
                _status.value = ConnectionStatus.Connected(
                    deviceName = pluggedDevice.name ?: "Connected PC",
                    address = pluggedDevice.address
                )
            }
        }

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d(TAG, "onConnectionStateChanged: state=$state, device=${device?.name}")
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
                    currentDevice = null
                    activeKeys.clear()
                    currentMouseButtonMask = 0x00
                    _status.value = ConnectionStatus.Disconnected
                    refreshPairedDevices()
                }
            }
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

    fun init() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            _status.value = ConnectionStatus.Error("Bluetooth not available on this device")
            return
        }
        if (!adapter.isEnabled) {
            _status.value = ConnectionStatus.BluetoothOff
            return
        }

        try {
            val supported = adapter.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
            if (!supported) {
                _status.value = ConnectionStatus.HidUnsupported
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission missing", e)
            _status.value = ConnectionStatus.PermissionMissing
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get HID profile proxy", e)
            _status.value = ConnectionStatus.Error(e.message ?: "Failed to initialize HID profile")
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerHidApp() {
        val hid = hidDevice ?: return
        try {
            val sdp = BluetoothHidDeviceAppSdpSettings(
                "PulsePad PC Remote",
                "Bluetooth PC Keyboard & Mouse Remote",
                "PulsePad",
                BluetoothHidDevice.SUBCLASS1_COMBO,
                HidConstants.HID_REPORT_DESCRIPTOR
            )
            // Use null for inQos and outQos to let Android Bluetooth stack negotiate optimal standard QoS
            hid.registerApp(sdp, null, null, executor, hidCallback)
            Log.d(TAG, "registerApp called successfully with standard Bluetooth HID QoS")
        } catch (e: SecurityException) {
            _status.value = ConnectionStatus.PermissionMissing
        } catch (e: Exception) {
            Log.e(TAG, "Error registering HID app", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val adapter = bluetoothAdapter ?: return
        try {
            val bonded = adapter.bondedDevices ?: emptySet()
            val list = bonded.map { dev ->
                PairedDeviceItem(
                    name = dev.name ?: "Unknown Device",
                    address = dev.address,
                    isConnected = dev.address == currentDevice?.address
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
        val hid = hidDevice ?: run {
            _status.value = ConnectionStatus.Error("HID Profile not ready")
            return
        }
        try {
            val device = adapter.getRemoteDevice(address)
            _status.value = ConnectionStatus.Connecting(device.name ?: "PC")
            val success = hid.connect(device)
            Log.d(TAG, "Connecting to ${device.name} ($address): success=$success")
        } catch (e: SecurityException) {
            _status.value = ConnectionStatus.PermissionMissing
        } catch (e: Exception) {
            _status.value = ConnectionStatus.Error("Failed to connect: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        val hid = hidDevice ?: return
        val dev = currentDevice ?: return
        try {
            hid.disconnect(dev)
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
        val hid = hidDevice ?: return
        try {
            hid.unregisterApp()
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hid)
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up HID", e)
        }
        hidDevice = null
    }
}
