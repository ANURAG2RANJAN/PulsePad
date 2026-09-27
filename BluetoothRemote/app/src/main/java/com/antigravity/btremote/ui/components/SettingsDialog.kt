package com.antigravity.btremote.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.antigravity.btremote.model.KeyboardColorTheme
import com.antigravity.btremote.model.KeyboardFont
import com.antigravity.btremote.model.RemoteSettings
import com.antigravity.btremote.model.SoundProfile
import com.antigravity.btremote.theme.CyanAccent
import com.antigravity.btremote.theme.SurfaceBorder
import com.antigravity.btremote.theme.SurfaceDark
import com.antigravity.btremote.theme.SurfaceElevated
import com.antigravity.btremote.theme.TextMuted
import com.antigravity.btremote.theme.TextPrimary
import com.antigravity.btremote.theme.TextSecondary

@Composable
fun SettingsDialog(
    settings: RemoteSettings,
    onSettingsChanged: (RemoteSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Remote Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mouse Pointer Sensitivity
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pointer Sensitivity", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(String.format("%.1fx", settings.pointerSensitivity), fontSize = 13.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.pointerSensitivity,
                        onValueChange = { onSettingsChanged(settings.copy(pointerSensitivity = it)) },
                        valueRange = 0.5f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scroll Sensitivity
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Scroll Speed", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(String.format("%.1fx", settings.scrollSensitivity), fontSize = 13.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.scrollSensitivity,
                        onValueChange = { onSettingsChanged(settings.copy(scrollSensitivity = it)) },
                        valueRange = 0.5f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Invert Scroll Direction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Invert Scroll Direction", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Reverse mouse wheel up/down", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = settings.invertScroll,
                        onCheckedChange = { onSettingsChanged(settings.copy(invertScroll = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = SurfaceElevated)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Haptic Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Haptic Feedback", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Vibrate on taps and button clicks", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = settings.hapticFeedback,
                        onCheckedChange = { onSettingsChanged(settings.copy(hapticFeedback = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = SurfaceElevated)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Acoustic Mechanical Keyboard Sound
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Mechanical Key Sound", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Acoustic switch sound on typing", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = settings.soundFeedback,
                        onCheckedChange = { onSettingsChanged(settings.copy(soundFeedback = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = SurfaceElevated)
                    )
                }

                if (settings.soundFeedback) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("SWITCH ACOUSTIC PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    val soundScroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(soundScroll),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SoundProfile.values().forEach { profile ->
                            val isSel = profile == settings.soundProfile
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) CyanAccent.copy(alpha = 0.2f) else SurfaceElevated)
                                    .border(1.dp, if (isSel) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                                    .clickable { onSettingsChanged(settings.copy(soundProfile = profile)) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = profile.displayName,
                                    fontSize = 11.sp,
                                    color = if (isSel) CyanAccent else TextSecondary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    // Sound Volume
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Acoustic Volume", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("${(settings.soundVolume * 100).toInt()}%", fontSize = 12.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.soundVolume,
                            onValueChange = { onSettingsChanged(settings.copy(soundVolume = it)) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    // Mouse Click Sound
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Mouse Click Sound", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Microswitch click on taps & buttons", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = settings.mouseClickSound,
                            onCheckedChange = { onSettingsChanged(settings.copy(mouseClickSound = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = SurfaceElevated)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Animation Effects
                Text("ANIMATIONS & VISUALS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("3D Key Travel Spring", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Mechanical key recoil & glow flash", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = settings.keyTravelAnimation,
                        onCheckedChange = { onSettingsChanged(settings.copy(keyTravelAnimation = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = SurfaceElevated)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Trackpad Sonar Ripples", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Concentric pulse waves on touch", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = settings.trackpadRipples,
                        onCheckedChange = { onSettingsChanged(settings.copy(trackpadRipples = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = SurfaceElevated)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Keyboard Color Theme
                Text("KEYBOARD COLOR THEME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val themeScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(themeScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeyboardColorTheme.values().forEach { theme ->
                        val isSelected = theme == settings.keyboardColorTheme
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) theme.accentColor.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(1.dp, if (isSelected) theme.accentColor else SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { onSettingsChanged(settings.copy(keyboardColorTheme = theme)) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(theme.accentColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = theme.displayName,
                                    fontSize = 11.sp,
                                    color = if (isSelected) theme.accentColor else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Keyboard Font
                Text("KEYBOARD FONT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val fontScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(fontScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KeyboardFont.values().forEach { font ->
                        val isSelected = font == settings.keyboardFont
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(1.dp, if (isSelected) CyanAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { onSettingsChanged(settings.copy(keyboardFont = font)) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = font.displayName,
                                fontSize = 11.sp,
                                fontFamily = font.fontFamily,
                                color = if (isSelected) CyanAccent else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
