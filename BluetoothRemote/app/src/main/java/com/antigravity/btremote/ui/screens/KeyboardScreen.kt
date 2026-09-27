package com.antigravity.btremote.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.btremote.hid.HidConstants
import com.antigravity.btremote.model.KeyboardColorTheme
import com.antigravity.btremote.model.KeyboardFont
import com.antigravity.btremote.model.TargetPlatform
import com.antigravity.btremote.theme.SurfaceBorder
import com.antigravity.btremote.theme.SurfaceDark
import com.antigravity.btremote.theme.SurfaceElevated
import com.antigravity.btremote.theme.TextMuted
import com.antigravity.btremote.theme.TextPrimary
import com.antigravity.btremote.theme.TextSecondary

@Composable
fun KeyboardScreen(
    targetPlatform: TargetPlatform = TargetPlatform.WINDOWS,
    stickyCtrl: Boolean,
    stickyAlt: Boolean,
    stickyShift: Boolean,
    stickyGui: Boolean,
    colorTheme: KeyboardColorTheme,
    fontOption: KeyboardFont,
    onColorThemeChanged: (KeyboardColorTheme) -> Unit,
    onFontOptionChanged: (KeyboardFont) -> Unit,
    onToggleCtrl: () -> Unit,
    onToggleAlt: () -> Unit,
    onToggleShift: () -> Unit,
    onToggleGui: () -> Unit,
    onKeyDown: (Byte, Byte?) -> Unit = { k, m -> },
    onKeyUp: (Byte) -> Unit = { _ -> },
    onKeyPress: (Byte, Byte?) -> Unit,
    onKeyCombo: (Byte, Byte) -> Unit,
    onSendText: (String) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isSystemLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var manualLandscapeOverride by remember { mutableStateOf<Boolean?>(null) }
    val isLandscape = manualLandscapeOverride ?: isSystemLandscape

    var textInput by remember { mutableStateOf("") }
    var showCustomizer by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        // Universal Top Utility Strip (Orientation Flip, Customizer, Text Input)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Orientation Toggle Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isLandscape) colorTheme.accentColor.copy(alpha = 0.2f) else SurfaceDark)
                    .border(1.dp, if (isLandscape) colorTheme.accentColor else SurfaceBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        manualLandscapeOverride = !isLandscape
                        onHaptic()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Rotate Keyboard",
                        tint = if (isLandscape) colorTheme.accentColor else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLandscape) "LANDSCAPE ${targetPlatform.shortName.uppercase()}" else "PORTRAIT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLandscape) colorTheme.accentColor else TextSecondary
                    )
                }
            }

            // Quick Modifiers Indicator (Landscape & Portrait)
            val altBadgeLabel = if (targetPlatform == TargetPlatform.APPLE) "⌥" else "ALT"
            val guiBadgeLabel = when (targetPlatform) {
                TargetPlatform.APPLE -> "⌘"
                TargetPlatform.ANDROID -> "🔍"
                TargetPlatform.SMART_TV -> "HOME"
                TargetPlatform.WINDOWS -> "WIN"
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SmallModifierBadge("CTRL", stickyCtrl, colorTheme) { onToggleCtrl(); onHaptic() }
                SmallModifierBadge(altBadgeLabel, stickyAlt, colorTheme) { onToggleAlt(); onHaptic() }
                SmallModifierBadge("SHIFT", stickyShift, colorTheme) { onToggleShift(); onHaptic() }
                SmallModifierBadge(guiBadgeLabel, stickyGui, colorTheme) { onToggleGui(); onHaptic() }
            }

            // Theme & Font Customizer Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (showCustomizer) colorTheme.accentColor.copy(alpha = 0.2f) else SurfaceDark)
                    .border(1.dp, if (showCustomizer) colorTheme.accentColor else SurfaceBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        showCustomizer = !showCustomizer
                        onHaptic()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatPaint,
                        contentDescription = "Theme",
                        tint = if (showCustomizer) colorTheme.accentColor else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = colorTheme.displayName.split(" ").first(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showCustomizer) colorTheme.accentColor else TextSecondary
                    )
                }
            }
        }

        // Customizer Panel (if expanded)
        if (showCustomizer) {
            ThemeAndFontCustomizerPanel(
                colorTheme = colorTheme,
                fontOption = fontOption,
                onColorThemeChanged = onColorThemeChanged,
                onFontOptionChanged = onFontOptionChanged,
                onHaptic = onHaptic
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Render Portrait vs Landscape Keyboard Layout
        if (isLandscape) {
            LandscapeKeyboardLayout(
                targetPlatform = targetPlatform,
                colorTheme = colorTheme,
                fontFamily = fontOption.fontFamily,
                stickyCtrl = stickyCtrl,
                stickyAlt = stickyAlt,
                stickyShift = stickyShift,
                stickyGui = stickyGui,
                onToggleCtrl = onToggleCtrl,
                onToggleAlt = onToggleAlt,
                onToggleShift = onToggleShift,
                onToggleGui = onToggleGui,
                onKeyDown = onKeyDown,
                onKeyUp = onKeyUp,
                onKeyPress = onKeyPress,
                onKeyCombo = onKeyCombo,
                onHaptic = onHaptic,
                modifier = Modifier.weight(1f)
            )
        } else {
            PortraitKeyboardLayout(
                targetPlatform = targetPlatform,
                textInput = textInput,
                onTextInputChanged = { textInput = it },
                onSendText = onSendText,
                colorTheme = colorTheme,
                fontOption = fontOption,
                stickyCtrl = stickyCtrl,
                stickyAlt = stickyAlt,
                stickyShift = stickyShift,
                stickyGui = stickyGui,
                onToggleCtrl = onToggleCtrl,
                onToggleAlt = onToggleAlt,
                onToggleShift = onToggleShift,
                onToggleGui = onToggleGui,
                onKeyDown = onKeyDown,
                onKeyUp = onKeyUp,
                onKeyPress = onKeyPress,
                onKeyCombo = onKeyCombo,
                onHaptic = onHaptic,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// -------------------------------------------------------------
// LANDSCAPE FULL DESKTOP PC KEYBOARD
// -------------------------------------------------------------
@Composable
fun LandscapeKeyboardLayout(
    targetPlatform: TargetPlatform = TargetPlatform.WINDOWS,
    colorTheme: KeyboardColorTheme,
    fontFamily: FontFamily,
    stickyCtrl: Boolean,
    stickyAlt: Boolean,
    stickyShift: Boolean,
    stickyGui: Boolean,
    onToggleCtrl: () -> Unit,
    onToggleAlt: () -> Unit,
    onToggleShift: () -> Unit,
    onToggleGui: () -> Unit,
    onKeyDown: (Byte, Byte?) -> Unit,
    onKeyUp: (Byte) -> Unit,
    onKeyPress: (Byte, Byte?) -> Unit,
    onKeyCombo: (Byte, Byte) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyH = 34.dp
    val spacing = 3.dp

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Main Alphanumeric & Function Keyboard Block (Left)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Row 0: Function Row (Esc, F1 - F12, PrtSc)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedKeyCap("ESC", onKeyDown = { onKeyDown(HidConstants.KEY_ESC, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_ESC) }, colorTheme = colorTheme, fontFamily = fontFamily, isAccent = true, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F1", onKeyDown = { onKeyDown(HidConstants.KEY_F1, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F1) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F2", onKeyDown = { onKeyDown(HidConstants.KEY_F2, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F2) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F3", onKeyDown = { onKeyDown(HidConstants.KEY_F3, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F3) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F4", onKeyDown = { onKeyDown(HidConstants.KEY_F4, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F4) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F5", onKeyDown = { onKeyDown(HidConstants.KEY_F5, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F5) }, colorTheme = colorTheme, fontFamily = fontFamily, isAccent = true, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F6", onKeyDown = { onKeyDown(HidConstants.KEY_F6, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F6) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F7", onKeyDown = { onKeyDown(HidConstants.KEY_F7, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F7) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F8", onKeyDown = { onKeyDown(HidConstants.KEY_F8, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F8) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F9", onKeyDown = { onKeyDown(HidConstants.KEY_F9, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F9) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F10", onKeyDown = { onKeyDown(HidConstants.KEY_F10, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F10) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F11", onKeyDown = { onKeyDown(HidConstants.KEY_F11, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F11) }, colorTheme = colorTheme, fontFamily = fontFamily, isAccent = true, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("F12", onKeyDown = { onKeyDown(HidConstants.KEY_F12, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F12) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("PRTSC", onKeyDown = { onKeyDown(HidConstants.KEY_PRINT_SCREEN, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_PRINT_SCREEN) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1.2f), height = keyH)
            }

            // Row 1: `~, 1 - 0, -, =, Backspace
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedKeyCap("`", onKeyDown = { onKeyDown(0x35.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x35.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(0.9f), height = keyH)
                val numRow = listOf("1" to 0x1E, "2" to 0x1F, "3" to 0x20, "4" to 0x21, "5" to 0x22, "6" to 0x23, "7" to 0x24, "8" to 0x25, "9" to 0x26, "0" to 0x27)
                numRow.forEach { (label, code) ->
                    ThemedKeyCap(label, onKeyDown = { onKeyDown(code.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(code.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                }
                ThemedKeyCap("-", onKeyDown = { onKeyDown(0x2D.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x2D.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("=", onKeyDown = { onKeyDown(0x2E.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x2E.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("⌫ BKSP", onKeyDown = { onKeyDown(HidConstants.KEY_BACKSPACE, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_BACKSPACE) }, colorTheme = colorTheme, fontFamily = fontFamily, isAccent = true, modifier = Modifier.weight(1.6f), height = keyH)
            }

            // Row 2: Tab, Q - P, [, ], \
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedKeyCap("TAB", onKeyDown = { onKeyDown(HidConstants.KEY_TAB, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_TAB) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1.3f), height = keyH)
                val qRow = listOf("Q" to 0x14, "W" to 0x1A, "E" to 0x08, "R" to 0x15, "T" to 0x17, "Y" to 0x1C, "U" to 0x18, "I" to 0x0C, "O" to 0x12, "P" to 0x13)
                qRow.forEach { (label, code) ->
                    ThemedKeyCap(label, onKeyDown = { onKeyDown(code.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(code.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                }
                ThemedKeyCap("[", onKeyDown = { onKeyDown(0x2F.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x2F.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("]", onKeyDown = { onKeyDown(0x30.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x30.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("\\", onKeyDown = { onKeyDown(0x31.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x31.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1.1f), height = keyH)
            }

            // Row 3: Caps, A - L, ;, ', Enter
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedKeyCap("CAPS", onKeyDown = { onKeyDown(HidConstants.KEY_CAPS_LOCK, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_CAPS_LOCK) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1.5f), height = keyH)
                val aRow = listOf("A" to 0x04, "S" to 0x16, "D" to 0x07, "F" to 0x09, "G" to 0x0A, "H" to 0x0B, "J" to 0x0D, "K" to 0x0E, "L" to 0x0F)
                aRow.forEach { (label, code) ->
                    ThemedKeyCap(label, onKeyDown = { onKeyDown(code.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(code.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                }
                ThemedKeyCap(";", onKeyDown = { onKeyDown(0x33.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x33.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("'", onKeyDown = { onKeyDown(0x34.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x34.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("ENTER", onKeyDown = { onKeyDown(HidConstants.KEY_ENTER, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_ENTER) }, colorTheme = colorTheme, fontFamily = fontFamily, isAccent = true, modifier = Modifier.weight(1.8f), height = keyH)
            }

            // Row 4: LShift, Z - M, ,, ., /, RShift
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedModifierKey("SHIFT", stickyShift, colorTheme, fontFamily, { onToggleShift(); onHaptic() }, modifier = Modifier.weight(1.8f), height = keyH)
                val zRow = listOf("Z" to 0x1D, "X" to 0x1B, "C" to 0x06, "V" to 0x19, "B" to 0x05, "N" to 0x11, "M" to 0x10)
                zRow.forEach { (label, code) ->
                    ThemedKeyCap(label, onKeyDown = { onKeyDown(code.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(code.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                }
                ThemedKeyCap(",", onKeyDown = { onKeyDown(0x36.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x36.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap(".", onKeyDown = { onKeyDown(0x37.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x37.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("/", onKeyDown = { onKeyDown(0x38.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(0x38.toByte()) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedModifierKey("SHIFT", stickyShift, colorTheme, fontFamily, { onToggleShift(); onHaptic() }, modifier = Modifier.weight(2f), height = keyH)
            }

            // Row 5: Ctrl, Win, Alt, Space, Alt, Win, Ctrl
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedModifierKey("CTRL", stickyCtrl, colorTheme, fontFamily, { onToggleCtrl(); onHaptic() }, modifier = Modifier.weight(1.2f), height = keyH)
                ThemedModifierKey("WIN", stickyGui, colorTheme, fontFamily, { onToggleGui(); onHaptic() }, modifier = Modifier.weight(1f), height = keyH)
                ThemedModifierKey("ALT", stickyAlt, colorTheme, fontFamily, { onToggleAlt(); onHaptic() }, modifier = Modifier.weight(1.1f), height = keyH)

                // Spacebar
                var isSpacePressed by remember { mutableStateOf(false) }
                val spaceScale by animateFloatAsState(
                    targetValue = if (isSpacePressed) 0.94f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
                    label = "spaceScale"
                )
                Box(
                    modifier = Modifier
                        .weight(5f)
                        .height(keyH)
                        .scale(spaceScale)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSpacePressed) colorTheme.accentColor.copy(alpha = 0.4f) else colorTheme.keyBg)
                        .border(1.dp, if (isSpacePressed) colorTheme.accentColor else colorTheme.keyBorder, RoundedCornerShape(6.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isSpacePressed = true
                                    onKeyDown(HidConstants.KEY_SPACE, null)
                                    onHaptic()
                                    try {
                                        tryAwaitRelease()
                                    } finally {
                                        isSpacePressed = false
                                        onKeyUp(HidConstants.KEY_SPACE)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SPACE", color = colorTheme.keyTextColor.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontFamily = fontFamily)
                }

                val altLabel = if (targetPlatform == TargetPlatform.APPLE) "⌥ OPT" else "ALT"
                val guiLabel = when (targetPlatform) {
                    TargetPlatform.APPLE -> "⌘ CMD"
                    TargetPlatform.ANDROID -> "SEARCH"
                    TargetPlatform.SMART_TV -> "HOME"
                    TargetPlatform.WINDOWS -> "WIN"
                }
                ThemedModifierKey(altLabel, stickyAlt, colorTheme, fontFamily, { onToggleAlt(); onHaptic() }, modifier = Modifier.weight(1.1f), height = keyH)
                ThemedModifierKey(guiLabel, stickyGui, colorTheme, fontFamily, { onToggleGui(); onHaptic() }, modifier = Modifier.weight(1f), height = keyH)
                ThemedModifierKey("CTRL", stickyCtrl, colorTheme, fontFamily, { onToggleCtrl(); onHaptic() }, modifier = Modifier.weight(1.2f), height = keyH)
            }
        }

        // Dedicated PC Navigation Cluster (Right Side)
        Column(
            modifier = Modifier
                .width(118.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Row 0: Ins, Home, PgUp
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedKeyCap("INS", onKeyDown = { onKeyDown(HidConstants.KEY_INSERT, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_INSERT) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("HOME", onKeyDown = { onKeyDown(HidConstants.KEY_HOME, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_HOME) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("PGUP", onKeyDown = { onKeyDown(HidConstants.KEY_PAGE_UP, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_PAGE_UP) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
            }

            // Row 1: Del, End, PgDn
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedKeyCap("DEL", onKeyDown = { onKeyDown(HidConstants.KEY_DELETE, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_DELETE) }, colorTheme = colorTheme, fontFamily = fontFamily, isAccent = true, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("END", onKeyDown = { onKeyDown(HidConstants.KEY_END, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_END) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("PGDN", onKeyDown = { onKeyDown(HidConstants.KEY_PAGE_DOWN, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_PAGE_DOWN) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
            }

            // Row 2 & 3: Quick Productivity Shortcuts in Landscape
            if (targetPlatform == TargetPlatform.APPLE) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    ThemedShortcutPill("⌘+C", colorTheme, fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x06.toByte()); onHaptic() }
                    ThemedShortcutPill("⌘+V", colorTheme, fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x19.toByte()); onHaptic() }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    ThemedShortcutPill("⌘+Tab", colorTheme, fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, HidConstants.KEY_TAB); onHaptic() }
                    ThemedShortcutPill("Spotlight", colorTheme, fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, HidConstants.KEY_SPACE); onHaptic() }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    ThemedShortcutPill("Copy (Ctrl+C)", colorTheme, fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x06.toByte()); onHaptic() }
                    ThemedShortcutPill("Paste (Ctrl+V)", colorTheme, fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x19.toByte()); onHaptic() }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    ThemedShortcutPill("Alt+Tab", colorTheme, fontFamily) { onKeyCombo(HidConstants.MOD_LALT, HidConstants.KEY_TAB); onHaptic() }
                    ThemedShortcutPill("Win+D", colorTheme, fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x07.toByte()); onHaptic() }
                }
            }

            // Row 4: Arrow UP (centered)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ThemedKeyCap("▲", onKeyDown = { onKeyDown(HidConstants.KEY_UP, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_UP) }, colorTheme = colorTheme, fontFamily = fontFamily, width = 42.dp, height = keyH)
            }

            // Row 5: Arrow LEFT, DOWN, RIGHT
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ThemedKeyCap("◀", onKeyDown = { onKeyDown(HidConstants.KEY_LEFT, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_LEFT) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("▼", onKeyDown = { onKeyDown(HidConstants.KEY_DOWN, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_DOWN) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
                ThemedKeyCap("▶", onKeyDown = { onKeyDown(HidConstants.KEY_RIGHT, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_RIGHT) }, colorTheme = colorTheme, fontFamily = fontFamily, modifier = Modifier.weight(1f), height = keyH)
            }
        }
    }
}

// -------------------------------------------------------------
// PORTRAIT COMPACT KEYBOARD
// -------------------------------------------------------------
@Composable
fun PortraitKeyboardLayout(
    targetPlatform: TargetPlatform = TargetPlatform.WINDOWS,
    textInput: String,
    onTextInputChanged: (String) -> Unit,
    onSendText: (String) -> Unit,
    colorTheme: KeyboardColorTheme,
    fontOption: KeyboardFont,
    stickyCtrl: Boolean,
    stickyAlt: Boolean,
    stickyShift: Boolean,
    stickyGui: Boolean,
    onToggleCtrl: () -> Unit,
    onToggleAlt: () -> Unit,
    onToggleShift: () -> Unit,
    onToggleGui: () -> Unit,
    onKeyDown: (Byte, Byte?) -> Unit,
    onKeyUp: (Byte) -> Unit,
    onKeyPress: (Byte, Byte?) -> Unit,
    onKeyCombo: (Byte, Byte) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Fast Text Transmitter Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceDark)
                .border(1.dp, colorTheme.keyBorder, RoundedCornerShape(14.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val targetName = when (targetPlatform) {
                TargetPlatform.WINDOWS -> "PC"
                TargetPlatform.ANDROID -> "Android"
                TargetPlatform.APPLE -> "Apple device"
                TargetPlatform.SMART_TV -> "Smart TV"
            }
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextInputChanged,
                placeholder = { Text("Type or paste text to stream to $targetName...", fontSize = 12.sp, color = TextMuted, fontFamily = fontOption.fontFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.isNotEmpty()) {
                        onSendText(textInput)
                        onTextInputChanged("")
                        onHaptic()
                    }
                }),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorTheme.accentColor,
                    unfocusedBorderColor = colorTheme.keyBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = colorTheme.accentColor
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = {
                    if (textInput.isNotEmpty()) {
                        onSendText(textInput)
                        onTextInputChanged("")
                        onHaptic()
                    }
                },
                modifier = Modifier.height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorTheme.accentColor, contentColor = colorTheme.activeKeyText),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sticky Modifiers Row
        val altLabel = if (targetPlatform == TargetPlatform.APPLE) "⌥ OPT" else "ALT"
        val guiLabel = when (targetPlatform) {
            TargetPlatform.APPLE -> "⌘ CMD"
            TargetPlatform.ANDROID -> "SEARCH"
            TargetPlatform.SMART_TV -> "HOME"
            TargetPlatform.WINDOWS -> "WIN / ⌘"
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ThemedModifierKey("CTRL", stickyCtrl, colorTheme, fontOption.fontFamily, { onToggleCtrl(); onHaptic() }, Modifier.weight(1f))
            ThemedModifierKey(altLabel, stickyAlt, colorTheme, fontOption.fontFamily, { onToggleAlt(); onHaptic() }, Modifier.weight(1f))
            ThemedModifierKey("SHIFT", stickyShift, colorTheme, fontOption.fontFamily, { onToggleShift(); onHaptic() }, Modifier.weight(1f))
            ThemedModifierKey(guiLabel, stickyGui, colorTheme, fontOption.fontFamily, { onToggleGui(); onHaptic() }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Platform Shortcuts Strip (Horizontal Scroll)
        val shortcutsScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(shortcutsScroll),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (targetPlatform) {
                TargetPlatform.APPLE -> {
                    ThemedShortcutPill("Copy (⌘+C)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x06.toByte()); onHaptic() }
                    ThemedShortcutPill("Paste (⌘+V)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x19.toByte()); onHaptic() }
                    ThemedShortcutPill("Select All (⌘+A)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x04.toByte()); onHaptic() }
                    ThemedShortcutPill("Undo (⌘+Z)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x1D.toByte()); onHaptic() }
                    ThemedShortcutPill("Cut (⌘+X)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x1B.toByte()); onHaptic() }
                    ThemedShortcutPill("Save (⌘+S)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x16.toByte()); onHaptic() }
                    ThemedShortcutPill("⌘+Tab", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, HidConstants.KEY_TAB); onHaptic() }
                    ThemedShortcutPill("Spotlight (⌘+Space)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, HidConstants.KEY_SPACE); onHaptic() }
                    ThemedShortcutPill("Close (⌘+W)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x1A.toByte()); onHaptic() }
                }
                TargetPlatform.ANDROID -> {
                    ThemedShortcutPill("Back (Esc)", colorTheme, fontOption.fontFamily) { onKeyPress(HidConstants.KEY_ESC, null); onHaptic() }
                    ThemedShortcutPill("Home (Win+H)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x0B.toByte()); onHaptic() }
                    ThemedShortcutPill("Recents (Alt+Tab)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LALT, HidConstants.KEY_TAB); onHaptic() }
                    ThemedShortcutPill("Copy (Ctrl+C)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x06.toByte()); onHaptic() }
                    ThemedShortcutPill("Paste (Ctrl+V)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x19.toByte()); onHaptic() }
                    ThemedShortcutPill("Select All (Ctrl+A)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x04.toByte()); onHaptic() }
                    ThemedShortcutPill("Search (Win)", colorTheme, fontOption.fontFamily) { onKeyPress(HidConstants.KEY_NONE, HidConstants.MOD_LGUI); onHaptic() }
                    ThemedShortcutPill("Undo (Ctrl+Z)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x1D.toByte()); onHaptic() }
                }
                TargetPlatform.SMART_TV -> {
                    ThemedShortcutPill("Back (Esc)", colorTheme, fontOption.fontFamily) { onKeyPress(HidConstants.KEY_ESC, null); onHaptic() }
                    ThemedShortcutPill("Home (Win+H)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x0B.toByte()); onHaptic() }
                    ThemedShortcutPill("Select (Enter)", colorTheme, fontOption.fontFamily) { onKeyPress(HidConstants.KEY_ENTER, null); onHaptic() }
                    ThemedShortcutPill("Play/Pause (Space)", colorTheme, fontOption.fontFamily) { onKeyPress(HidConstants.KEY_SPACE, null); onHaptic() }
                    ThemedShortcutPill("Mute", colorTheme, fontOption.fontFamily) { onKeyPress(HidConstants.KEY_MUTE, null); onHaptic() }
                }
                TargetPlatform.WINDOWS -> {
                    ThemedShortcutPill("Copy (Ctrl+C)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x06.toByte()); onHaptic() }
                    ThemedShortcutPill("Paste (Ctrl+V)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x19.toByte()); onHaptic() }
                    ThemedShortcutPill("Select All (Ctrl+A)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x04.toByte()); onHaptic() }
                    ThemedShortcutPill("Undo (Ctrl+Z)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x1D.toByte()); onHaptic() }
                    ThemedShortcutPill("Cut (Ctrl+X)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x1B.toByte()); onHaptic() }
                    ThemedShortcutPill("Save (Ctrl+S)", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LCTRL, 0x16.toByte()); onHaptic() }
                    ThemedShortcutPill("Alt+Tab", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LALT, HidConstants.KEY_TAB); onHaptic() }
                    ThemedShortcutPill("Win+D", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LGUI, 0x07.toByte()); onHaptic() }
                    ThemedShortcutPill("Alt+F4", colorTheme, fontOption.fontFamily) { onKeyCombo(HidConstants.MOD_LALT, HidConstants.KEY_F4); onHaptic() }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Function Keys Strip (Esc, F1 - F12, Del, PrtSc)
        val functionScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(functionScroll),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ThemedKeyCap("ESC", onKeyDown = { onKeyDown(HidConstants.KEY_ESC, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_ESC) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, width = 46.dp, isAccent = true)
            ThemedKeyCap("F1", onKeyDown = { onKeyDown(HidConstants.KEY_F1, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F1) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F2", onKeyDown = { onKeyDown(HidConstants.KEY_F2, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F2) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F3", onKeyDown = { onKeyDown(HidConstants.KEY_F3, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F3) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F4", onKeyDown = { onKeyDown(HidConstants.KEY_F4, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F4) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F5", onKeyDown = { onKeyDown(HidConstants.KEY_F5, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F5) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, isAccent = true)
            ThemedKeyCap("F6", onKeyDown = { onKeyDown(HidConstants.KEY_F6, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F6) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F7", onKeyDown = { onKeyDown(HidConstants.KEY_F7, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F7) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F8", onKeyDown = { onKeyDown(HidConstants.KEY_F8, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F8) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F9", onKeyDown = { onKeyDown(HidConstants.KEY_F9, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F9) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F10", onKeyDown = { onKeyDown(HidConstants.KEY_F10, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F10) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("F11", onKeyDown = { onKeyDown(HidConstants.KEY_F11, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F11) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, isAccent = true)
            ThemedKeyCap("F12", onKeyDown = { onKeyDown(HidConstants.KEY_F12, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_F12) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily)
            ThemedKeyCap("DEL", onKeyDown = { onKeyDown(HidConstants.KEY_DELETE, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_DELETE) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, width = 46.dp)
            ThemedKeyCap("PRTSC", onKeyDown = { onKeyDown(HidConstants.KEY_PRINT_SCREEN, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_PRINT_SCREEN) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, width = 54.dp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // PC On-Screen Compact Keyboards
        // Row 1: Numbers & Backspace
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val numKeys = listOf("1" to 0x1E, "2" to 0x1F, "3" to 0x20, "4" to 0x21, "5" to 0x22, "6" to 0x23, "7" to 0x24, "8" to 0x25, "9" to 0x26, "0" to 0x27)
            numKeys.forEach { (label, code) ->
                ThemedKeyCap(label, onKeyDown = { onKeyDown(code.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(code.toByte()) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1f))
            }
            ThemedKeyCap("⌫", onKeyDown = { onKeyDown(HidConstants.KEY_BACKSPACE, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_BACKSPACE) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1.3f))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2: Tab + QWERTYUIOP
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ThemedKeyCap("TAB", onKeyDown = { onKeyDown(HidConstants.KEY_TAB, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_TAB) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1.2f))
            val qRow = listOf("Q" to 0x14, "W" to 0x1A, "E" to 0x08, "R" to 0x15, "T" to 0x17, "Y" to 0x1C, "U" to 0x18, "I" to 0x0C, "O" to 0x12, "P" to 0x13)
            qRow.forEach { (label, code) ->
                ThemedKeyCap(label, onKeyDown = { onKeyDown(code.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(code.toByte()) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 3: ASDFGHJKL + Enter
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val aRow = listOf("A" to 0x04, "S" to 0x16, "D" to 0x07, "F" to 0x09, "G" to 0x0A, "H" to 0x0B, "J" to 0x0D, "K" to 0x0E, "L" to 0x0F)
            aRow.forEach { (label, code) ->
                ThemedKeyCap(label, onKeyDown = { onKeyDown(code.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(code.toByte()) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1f))
            }
            ThemedKeyCap("ENTER", onKeyDown = { onKeyDown(HidConstants.KEY_ENTER, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_ENTER) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1.6f), isAccent = true)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 4: ZXCVBNM, ., /
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val zRow = listOf("Z" to 0x1D, "X" to 0x1B, "C" to 0x06, "V" to 0x19, "B" to 0x05, "N" to 0x11, "M" to 0x10, "," to 0x36, "." to 0x37, "/" to 0x38)
            zRow.forEach { (label, code) ->
                ThemedKeyCap(label, onKeyDown = { onKeyDown(code.toByte(), null); onHaptic() }, onKeyUp = { onKeyUp(code.toByte()) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 5: Spacebar & Navigation Arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacebar
            var isSpacePressed by remember { mutableStateOf(false) }
            val spaceScale by animateFloatAsState(
                targetValue = if (isSpacePressed) 0.94f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
                label = "portraitSpaceScale"
            )
            Box(
                modifier = Modifier
                    .weight(1.8f)
                    .height(44.dp)
                    .scale(spaceScale)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSpacePressed) colorTheme.accentColor.copy(alpha = 0.4f) else colorTheme.keyBg)
                    .border(1.dp, if (isSpacePressed) colorTheme.accentColor else colorTheme.keyBorder, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isSpacePressed = true
                                onKeyDown(HidConstants.KEY_SPACE, null)
                                onHaptic()
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    isSpacePressed = false
                                    onKeyUp(HidConstants.KEY_SPACE)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("SPACE", color = colorTheme.keyTextColor.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontFamily = fontOption.fontFamily)
            }

            // Navigation Arrows cluster
            Row(
                modifier = Modifier.weight(1.2f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ThemedKeyCap("◀", onKeyDown = { onKeyDown(HidConstants.KEY_LEFT, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_LEFT) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1f), height = 44.dp)
                Column(modifier = Modifier.weight(1f)) {
                    ThemedKeyCap("▲", onKeyDown = { onKeyDown(HidConstants.KEY_UP, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_UP) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, height = 20.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    ThemedKeyCap("▼", onKeyDown = { onKeyDown(HidConstants.KEY_DOWN, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_DOWN) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, height = 20.dp)
                }
                ThemedKeyCap("▶", onKeyDown = { onKeyDown(HidConstants.KEY_RIGHT, null); onHaptic() }, onKeyUp = { onKeyUp(HidConstants.KEY_RIGHT) }, colorTheme = colorTheme, fontFamily = fontOption.fontFamily, modifier = Modifier.weight(1f), height = 44.dp)
            }
        }
    }
}

// -------------------------------------------------------------
// REUSABLE THEMED COMPONENTS
// -------------------------------------------------------------
@Composable
fun SmallModifierBadge(
    title: String,
    isActive: Boolean,
    colorTheme: KeyboardColorTheme,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) colorTheme.accentColor else SurfaceDark)
            .border(1.dp, if (isActive) colorTheme.accentColor else SurfaceBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) colorTheme.activeKeyText else TextMuted
        )
    }
}

@Composable
fun ThemeAndFontCustomizerPanel(
    colorTheme: KeyboardColorTheme,
    fontOption: KeyboardFont,
    onColorThemeChanged: (KeyboardColorTheme) -> Unit,
    onFontOptionChanged: (KeyboardFont) -> Unit,
    onHaptic: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, colorTheme.accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            // Colors
            val colorScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(colorScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyboardColorTheme.values().forEach { theme ->
                    val isSelected = theme == colorTheme
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) theme.accentColor.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (isSelected) theme.accentColor else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onColorThemeChanged(theme)
                                onHaptic()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(theme.accentColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = theme.displayName,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) theme.accentColor else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fonts
            val fontScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(fontScroll),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyboardFont.values().forEach { font ->
                    val isSelected = font == fontOption
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colorTheme.accentColor.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (isSelected) colorTheme.accentColor else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onFontOptionChanged(font)
                                onHaptic()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = font.displayName,
                            fontSize = 10.sp,
                            fontFamily = font.fontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) colorTheme.accentColor else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemedModifierKey(
    title: String,
    isActive: Boolean,
    colorTheme: KeyboardColorTheme,
    fontFamily: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 40.dp
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "modScale"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "modOffset"
    )

    Box(
        modifier = modifier
            .height(height)
            .offset(y = offsetY)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive || isPressed) colorTheme.accentColor else colorTheme.keyBg)
            .border(
                1.dp,
                if (isActive || isPressed) colorTheme.accentColor else colorTheme.keyBorder,
                RoundedCornerShape(6.dp)
            )
            .pointerInput(title) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = if (title.length > 4) 9.sp else 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            color = if (isActive || isPressed) colorTheme.activeKeyText else colorTheme.keyTextColor
        )
    }
}

@Composable
fun ThemedShortcutPill(
    label: String,
    colorTheme: KeyboardColorTheme,
    fontFamily: FontFamily,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "pillScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) colorTheme.accentColor.copy(alpha = 0.35f) else SurfaceElevated)
            .border(
                1.dp,
                if (isPressed) colorTheme.accentColor else colorTheme.keyBorder,
                RoundedCornerShape(8.dp)
            )
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    }
                )
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colorTheme.accentColor, fontFamily = fontFamily)
    }
}

@Composable
fun ThemedKeyCap(
    text: String,
    onKeyDown: () -> Unit,
    onKeyUp: () -> Unit,
    colorTheme: KeyboardColorTheme,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp? = null,
    height: androidx.compose.ui.unit.Dp = 42.dp,
    isAccent: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "keyScale"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "keyOffset"
    )

    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .height(height)
            .offset(y = offsetY)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isPressed) colorTheme.accentColor.copy(alpha = 0.5f)
                else if (isAccent) colorTheme.accentColor.copy(alpha = 0.15f)
                else colorTheme.keyBg
            )
            .border(
                1.dp,
                if (isPressed) colorTheme.accentColor
                else if (isAccent) colorTheme.accentColor.copy(alpha = 0.6f)
                else colorTheme.keyBorder,
                RoundedCornerShape(6.dp)
            )
            .pointerInput(text) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onKeyDown()
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                            onKeyUp()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 4) 9.sp else if (text.length > 2) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            color = if (isPressed) colorTheme.activeKeyText else if (isAccent) colorTheme.accentColor else colorTheme.keyTextColor
        )
    }
}

@Composable
fun ThemedKeyCap(
    text: String,
    onClick: () -> Unit,
    colorTheme: KeyboardColorTheme,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp? = null,
    height: androidx.compose.ui.unit.Dp = 42.dp,
    isAccent: Boolean = false
) {
    ThemedKeyCap(
        text = text,
        onKeyDown = onClick,
        onKeyUp = {},
        colorTheme = colorTheme,
        fontFamily = fontFamily,
        modifier = modifier,
        width = width,
        height = height,
        isAccent = isAccent
    )
}
