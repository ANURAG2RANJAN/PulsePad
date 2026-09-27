package com.antigravity.btremote.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

sealed interface ConnectionStatus {
    object Disconnected : ConnectionStatus
    data class Connecting(val deviceName: String?) : ConnectionStatus
    data class Connected(val deviceName: String, val address: String) : ConnectionStatus
    object Disconnecting : ConnectionStatus
    data class Error(val message: String) : ConnectionStatus
    object BluetoothOff : ConnectionStatus
    object PermissionMissing : ConnectionStatus
    object HidUnsupported : ConnectionStatus
}

enum class KeyboardColorTheme(
    val displayName: String,
    val keyBg: Color,
    val keyBorder: Color,
    val keyTextColor: Color,
    val accentColor: Color,
    val accentGlow: Color,
    val activeKeyText: Color
) {
    CYBER_CYAN(
        displayName = "Cyber Cyan",
        keyBg = Color(0xFF141A28),
        keyBorder = Color(0xFF24344E),
        keyTextColor = Color(0xFFF1F5F9),
        accentColor = Color(0xFF00E5FF),
        accentGlow = Color(0x3300E5FF),
        activeKeyText = Color(0xFF080C14)
    ),
    SYNTHWAVE(
        displayName = "Synthwave",
        keyBg = Color(0xFF22132D),
        keyBorder = Color(0xFF45225C),
        keyTextColor = Color(0xFFFDF4FF),
        accentColor = Color(0xFFE879F9),
        accentGlow = Color(0x33E879F9),
        activeKeyText = Color(0xFF1A0A26)
    ),
    MATRIX_GREEN(
        displayName = "Matrix Green",
        keyBg = Color(0xFF0B1F13),
        keyBorder = Color(0xFF153F23),
        keyTextColor = Color(0xFFDCFCE7),
        accentColor = Color(0xFF22C55E),
        accentGlow = Color(0x3322C55E),
        activeKeyText = Color(0xFF061E0E)
    ),
    SOLAR_AMBER(
        displayName = "Solar Amber",
        keyBg = Color(0xFF26190B),
        keyBorder = Color(0xFF4C2F13),
        keyTextColor = Color(0xFFFEF3C7),
        accentColor = Color(0xFFF59E0B),
        accentGlow = Color(0x33F59E0B),
        activeKeyText = Color(0xFF1F1204)
    ),
    CRIMSON_OLED(
        displayName = "Crimson Red",
        keyBg = Color(0xFF220D12),
        keyBorder = Color(0xFF4A1822),
        keyTextColor = Color(0xFFFFE4E6),
        accentColor = Color(0xFFF43F5E),
        accentGlow = Color(0x33F43F5E),
        activeKeyText = Color(0xFFFFFFFF)
    ),
    NORDIC_ICE(
        displayName = "Nordic Ice",
        keyBg = Color(0xFF101C2B),
        keyBorder = Color(0xFF1F3752),
        keyTextColor = Color(0xFFE0F2FE),
        accentColor = Color(0xFF38BDF8),
        accentGlow = Color(0x3338BDF8),
        activeKeyText = Color(0xFF071424)
    ),
    MONO_STEALTH(
        displayName = "Mono Stealth",
        keyBg = Color(0xFF27272A),
        keyBorder = Color(0xFF3F3F46),
        keyTextColor = Color(0xFFFAFAFA),
        accentColor = Color(0xFFE4E4E7),
        accentGlow = Color(0x33FFFFFF),
        activeKeyText = Color(0xFF09090B)
    )
}

enum class KeyboardFont(val displayName: String, val fontFamily: FontFamily) {
    MODERN_SANS("Modern Sans", FontFamily.SansSerif),
    TERMINAL_MONO("Terminal Mono", FontFamily.Monospace),
    SERIF_CLASSIC("Classic Serif", FontFamily.Serif),
    CURSIVE_TECH("Rounded Cursive", FontFamily.Cursive)
}

enum class SoundProfile(val displayName: String, val description: String) {
    BLUE_SWITCH("Blue Clicky", "Sharp acoustic snap & resonant keycap bottom-out"),
    BROWN_SWITCH("Brown Tactile", "Gentle tactile bump with warm acoustic thock"),
    RED_SWITCH("Red Linear", "Smooth deep acoustic thock with low resonance"),
    CYBER_SYNTH("Cyber Synth", "Futuristic frequency-modulated electronic chirp"),
    OFF("Muted", "Silent operation without acoustic feedback")
}

data class RemoteSettings(
    val pointerSensitivity: Float = 1.2f,
    val scrollSensitivity: Float = 1.0f,
    val invertScroll: Boolean = false,
    val smoothDamping: Boolean = true,
    val hapticFeedback: Boolean = true,
    val keyboardColorTheme: KeyboardColorTheme = KeyboardColorTheme.CYBER_CYAN,
    val keyboardFont: KeyboardFont = KeyboardFont.MODERN_SANS,
    val soundFeedback: Boolean = true,
    val soundProfile: SoundProfile = SoundProfile.BLUE_SWITCH,
    val soundVolume: Float = 0.8f,
    val mouseClickSound: Boolean = true,
    val keyTravelAnimation: Boolean = true,
    val trackpadRipples: Boolean = true
)

data class PairedDeviceItem(
    val name: String,
    val address: String,
    val isConnected: Boolean = false
)
