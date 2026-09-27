package com.antigravity.btremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.antigravity.btremote.model.PairedDeviceItem
import com.antigravity.btremote.model.TargetPlatform
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
fun PairingDialog(
    pairedDevices: List<PairedDeviceItem>,
    phoneName: String = "This Phone",
    selectedPlatform: TargetPlatform = TargetPlatform.WINDOWS,
    onPlatformSelected: (TargetPlatform) -> Unit = {},
    onDeviceSelected: (String) -> Unit,
    onMakeDiscoverable: () -> Unit,
    onOpenBtSettings: () -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
    hasConnectedDevice: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                imageVector = Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bluetooth Devices",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Connect to PC, Android, Apple & TV",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Device Platform Selector Tabs
                Text(
                    text = "TARGET DEVICE TYPE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TargetPlatform.values().forEach { platform ->
                        val isSelected = platform == selectedPlatform
                        val emoji = when (platform) {
                            TargetPlatform.WINDOWS -> "💻"
                            TargetPlatform.ANDROID -> "🤖"
                            TargetPlatform.APPLE -> "🍎"
                            TargetPlatform.SMART_TV -> "📺"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) CyanAccent else SurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onPlatformSelected(platform) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = emoji, fontSize = 14.sp)
                                Text(
                                    text = platform.shortName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CyanAccent else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Disconnect option if currently connected
                if (hasConnectedDevice) {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.5f))
                    ) {
                        Text("Disconnect Current Device", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Paired Devices List
                Text(
                    text = "PAIRED DEVICES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (pairedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No paired devices found.\nMake phone discoverable below to pair with your PC.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(pairedDevices) { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (device.isConnected) CyanAccent.copy(alpha = 0.1f) else SurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (device.isConnected) CyanAccent.copy(alpha = 0.4f) else SurfaceBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onDeviceSelected(device.address) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val devPlatformEmoji = when (device.platform) {
                                        TargetPlatform.WINDOWS -> "💻"
                                        TargetPlatform.ANDROID -> "🤖"
                                        TargetPlatform.APPLE -> "🍎"
                                        TargetPlatform.SMART_TV -> "📺"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = devPlatformEmoji, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = device.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = device.platform.shortName,
                                                fontSize = 9.sp,
                                                color = CyanAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = device.address,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                if (device.isConnected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EmeraldSuccess.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Connected",
                                            color = EmeraldSuccess,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyanAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Connect",
                                            color = CyanAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Make Discoverable & Bluetooth Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onMakeDiscoverable,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Make Discoverable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenBtSettings,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("BT Settings", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic OS-specific pairing guide
                val guideInstructions = when (selectedPlatform) {
                    TargetPlatform.WINDOWS -> "1. Tap 'Make Discoverable' above.\n2. On PC: Settings -> Bluetooth & devices -> 'Add device'.\n3. Select '$phoneName'. PulsePad pairs directly as wireless Keyboard & Mouse!"
                    TargetPlatform.ANDROID -> "1. Tap 'Make Discoverable' above.\n2. On target Android phone/tablet: Settings -> Bluetooth -> 'Pair new device'.\n3. Select '$phoneName' & tap 'Pair'. Native mouse cursor appears immediately on trackpad touch!"
                    TargetPlatform.APPLE -> "1. Tap 'Make Discoverable' above.\n2. On iPad/iPhone/Mac: Settings -> Bluetooth -> Tap '$phoneName'.\n3. iPhone pointer: Settings -> Accessibility -> Touch -> enable 'AssistiveTouch'. Native on iPad/Mac!"
                    TargetPlatform.SMART_TV -> "1. Tap 'Make Discoverable' above.\n2. On Smart TV / Box: Settings -> Remotes & Accessories -> 'Add accessory'.\n3. Select '$phoneName'. PulsePad acts as universal TV remote & D-pad!"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💡 Pairing Guide: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            Text(selectedPlatform.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This Phone BT Name: $phoneName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = guideInstructions,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
