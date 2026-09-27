package com.antigravity.btremote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.btremote.hid.HidConstants
import com.antigravity.btremote.theme.CyanAccent
import com.antigravity.btremote.theme.EmeraldSuccess
import com.antigravity.btremote.theme.KeyBg
import com.antigravity.btremote.theme.KeyBorder
import com.antigravity.btremote.theme.PurpleAccent
import com.antigravity.btremote.theme.RoseError
import com.antigravity.btremote.theme.SurfaceBorder
import com.antigravity.btremote.theme.SurfaceDark
import com.antigravity.btremote.theme.SurfaceElevated
import com.antigravity.btremote.theme.TextMuted
import com.antigravity.btremote.theme.TextPrimary
import com.antigravity.btremote.theme.TextSecondary

@Composable
fun MediaRemoteScreen(
    onConsumerKey: (Short) -> Unit,
    onKeyPress: (Byte, Byte?) -> Unit,
    onKeyCombo: (Byte, Byte) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- MEDIA CONTROLS CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MEDIA PLAYBACK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Playback Buttons Row: Prev, Play/Pause, Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prev
                    MediaCircleButton(
                        icon = Icons.Default.FastRewind,
                        label = "Prev",
                        size = 56.dp,
                        onClick = { onConsumerKey(HidConstants.CONSUMER_SCAN_PREV); onHaptic() }
                    )

                    // Big Play/Pause
                    MediaCircleButton(
                        icon = Icons.Default.PlayArrow,
                        label = "Play/Pause",
                        size = 76.dp,
                        isHighlight = true,
                        onClick = { onConsumerKey(HidConstants.CONSUMER_PLAY_PAUSE); onHaptic() }
                    )

                    // Next
                    MediaCircleButton(
                        icon = Icons.Default.FastForward,
                        label = "Next",
                        size = 56.dp,
                        onClick = { onConsumerKey(HidConstants.CONSUMER_SCAN_NEXT); onHaptic() }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Volume Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VolumeButton(
                        icon = Icons.Default.VolumeDown,
                        label = "Vol -",
                        onClick = { onConsumerKey(HidConstants.CONSUMER_VOLUME_DOWN); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                    VolumeButton(
                        icon = Icons.Default.VolumeMute,
                        label = "Mute",
                        tint = RoseError,
                        onClick = { onConsumerKey(HidConstants.CONSUMER_MUTE); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                    VolumeButton(
                        icon = Icons.Default.VolumeUp,
                        label = "Vol +",
                        onClick = { onConsumerKey(HidConstants.CONSUMER_VOLUME_UP); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Launchers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VolumeButton(
                        icon = Icons.Default.Calculate,
                        label = "Calc",
                        onClick = { onConsumerKey(HidConstants.CONSUMER_CALCULATOR); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                    VolumeButton(
                        icon = Icons.Default.Home,
                        label = "Browser",
                        onClick = { onConsumerKey(HidConstants.CONSUMER_BROWSER_HOME); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                    VolumeButton(
                        icon = Icons.Default.Stop,
                        label = "Stop",
                        onClick = { onConsumerKey(HidConstants.CONSUMER_STOP); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PRESENTATION MODE CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Slideshow, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRESENTATION CONTROLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAccent,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Giant Previous & Next Slide Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Prev Slide
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KeyBg)
                            .border(1.dp, KeyBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                onKeyPress(HidConstants.KEY_LEFT, null)
                                onHaptic()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(28.dp))
                            Text("PREV SLIDE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    // Next Slide (Highlight)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PurpleAccent.copy(alpha = 0.2f))
                            .border(1.5.dp, PurpleAccent, RoundedCornerShape(16.dp))
                            .clickable {
                                onKeyPress(HidConstants.KEY_RIGHT, null)
                                onHaptic()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("NEXT SLIDE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Icon(Icons.Default.NavigateNext, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Presentation Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VolumeButton(
                        icon = null,
                        label = "Start (F5)",
                        onClick = { onKeyPress(HidConstants.KEY_F5, null); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                    VolumeButton(
                        icon = null,
                        label = "From Slide (⇧F5)",
                        onClick = { onKeyCombo(HidConstants.MOD_LSHIFT, HidConstants.KEY_F5); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                    VolumeButton(
                        icon = null,
                        label = "Black (B)",
                        onClick = { onKeyPress(0x05.toByte(), null); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                    VolumeButton(
                        icon = null,
                        label = "Exit (Esc)",
                        onClick = { onKeyPress(HidConstants.KEY_ESC, null); onHaptic() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaCircleButton(
    icon: ImageVector,
    label: String,
    size: androidx.compose.ui.unit.Dp,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isHighlight) CyanAccent else KeyBg)
            .border(
                1.5.dp,
                if (isHighlight) CyanAccent else KeyBorder,
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isHighlight) SurfaceDark else TextPrimary,
            modifier = Modifier.size(if (isHighlight) 38.dp else 26.dp)
        )
    }
}

@Composable
fun VolumeButton(
    icon: ImageVector?,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = tint)
        }
    }
}
