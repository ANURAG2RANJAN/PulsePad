package com.antigravity.btremote.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.btremote.model.ConnectionStatus
import com.antigravity.btremote.theme.AmberWarning
import com.antigravity.btremote.theme.CyanAccent
import com.antigravity.btremote.theme.EmeraldSuccess
import com.antigravity.btremote.theme.RoseError
import com.antigravity.btremote.theme.SurfaceBorder
import com.antigravity.btremote.theme.SurfaceDark
import com.antigravity.btremote.theme.SurfaceElevated
import com.antigravity.btremote.theme.TextMuted
import com.antigravity.btremote.theme.TextPrimary
import com.antigravity.btremote.theme.TextSecondary

@Composable
fun TopStatusBar(
    status: ConnectionStatus,
    targetPlatform: com.antigravity.btremote.model.TargetPlatform,
    onPlatformChanged: (com.antigravity.btremote.model.TargetPlatform) -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title & Brand
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PULSEPAD",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Interactive Target Platform Badge
                val (platformEmoji, platformTag) = when (targetPlatform) {
                    com.antigravity.btremote.model.TargetPlatform.WINDOWS -> Pair("💻", "WIN")
                    com.antigravity.btremote.model.TargetPlatform.ANDROID -> Pair("🤖", "AND")
                    com.antigravity.btremote.model.TargetPlatform.APPLE -> Pair("🍎", "IOS")
                    com.antigravity.btremote.model.TargetPlatform.SMART_TV -> Pair("📺", "TV")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyanAccent.copy(alpha = 0.18f))
                        .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .clickable {
                            val next = when (targetPlatform) {
                                com.antigravity.btremote.model.TargetPlatform.WINDOWS -> com.antigravity.btremote.model.TargetPlatform.ANDROID
                                com.antigravity.btremote.model.TargetPlatform.ANDROID -> com.antigravity.btremote.model.TargetPlatform.APPLE
                                com.antigravity.btremote.model.TargetPlatform.APPLE -> com.antigravity.btremote.model.TargetPlatform.SMART_TV
                                com.antigravity.btremote.model.TargetPlatform.SMART_TV -> com.antigravity.btremote.model.TargetPlatform.WINDOWS
                            }
                            onPlatformChanged(next)
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = platformEmoji, fontSize = 9.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = platformTag,
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                CyberAudioVisualizer()
            }
        }

        // Connection Status & Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Status Pill Button
            val (statusText, statusColor, showPulse) = when (status) {
                is ConnectionStatus.Connected -> Triple(status.deviceName, EmeraldSuccess, false)
                is ConnectionStatus.Connecting -> Triple("Connecting...", AmberWarning, true)
                is ConnectionStatus.Disconnecting -> Triple("Disconnecting...", AmberWarning, true)
                is ConnectionStatus.BluetoothOff -> Triple("BT Disabled", RoseError, false)
                is ConnectionStatus.PermissionMissing -> Triple("No Permission", RoseError, false)
                is ConnectionStatus.HidUnsupported -> Triple("HID Unsupported", RoseError, false)
                is ConnectionStatus.Error -> Triple("Error", RoseError, false)
                ConnectionStatus.Disconnected -> Triple("Disconnected", TextMuted, false)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onConnectClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (showPulse) statusColor.copy(alpha = pulseAlpha) else statusColor
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = if (status is ConnectionStatus.Connected) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(if (status is ConnectionStatus.Connected) 110.dp else 90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Connect / Device Icon Button
            IconButton(
                onClick = onConnectClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = when (status) {
                        is ConnectionStatus.Connected -> Icons.Default.BluetoothConnected
                        is ConnectionStatus.BluetoothOff -> Icons.Default.BluetoothDisabled
                        else -> Icons.Default.Bluetooth
                    },
                    contentDescription = "Bluetooth Devices",
                    tint = if (status is ConnectionStatus.Connected) CyanAccent else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Settings Icon Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Remote Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CyberAudioVisualizer(
    modifier: Modifier = Modifier,
    accentColor: Color = CyanAccent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioEq")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.45f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(340, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(220, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(390, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "h5"
    )

    Row(
        modifier = modifier
            .height(13.dp)
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { fraction ->
            val barHeight = (13.dp * fraction).coerceAtLeast(3.dp)
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(1.dp))
                    .background(accentColor.copy(alpha = 0.9f))
            )
        }
    }
}
