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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.btremote.hid.HidConstants
import com.antigravity.btremote.theme.CyanAccent
import com.antigravity.btremote.theme.EmeraldSuccess
import com.antigravity.btremote.theme.PurpleAccent
import com.antigravity.btremote.theme.RoseError
import com.antigravity.btremote.theme.SurfaceBorder
import com.antigravity.btremote.theme.SurfaceDark
import com.antigravity.btremote.theme.SurfaceElevated
import com.antigravity.btremote.theme.TextMuted
import com.antigravity.btremote.theme.TextPrimary
import com.antigravity.btremote.theme.TextSecondary

data class MacroItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val action: () -> Unit
)

@Composable
fun MacroScreen(
    onMultiKey: (modifier: Byte, keys: ByteArray) -> Unit,
    onSendText: (String) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("System") }

    val categories = listOf("System", "Developer", "Web / Office", "Stream / Game")

    val systemMacros = listOf(
        MacroItem("Task Manager", "Ctrl+Shift+Esc", Icons.Default.Window, CyanAccent) {
            val mod = (HidConstants.MOD_LCTRL.toInt() or HidConstants.MOD_LSHIFT.toInt()).toByte()
            onMultiKey(mod, byteArrayOf(HidConstants.KEY_ESC))
        },
        MacroItem("Snipping Tool", "Win+Shift+S", Icons.Default.ContentCut, EmeraldSuccess) {
            val mod = (HidConstants.MOD_LGUI.toInt() or HidConstants.MOD_LSHIFT.toInt()).toByte()
            onMultiKey(mod, byteArrayOf(0x16.toByte())) // S
        },
        MacroItem("Lock PC", "Win+L", Icons.Default.Lock, RoseError) {
            onMultiKey(HidConstants.MOD_LGUI, byteArrayOf(0x0F.toByte())) // L
        },
        MacroItem("File Explorer", "Win+E", Icons.Default.Folder, PurpleAccent) {
            onMultiKey(HidConstants.MOD_LGUI, byteArrayOf(0x08.toByte())) // E
        },
        MacroItem("Emoji Panel", "Win+.", Icons.Default.InsertEmoticon, CyanAccent) {
            onMultiKey(HidConstants.MOD_LGUI, byteArrayOf(0x37.toByte())) // .
        },
        MacroItem("PC Settings", "Win+I", Icons.Default.Settings, TextSecondary) {
            onMultiKey(HidConstants.MOD_LGUI, byteArrayOf(0x0C.toByte())) // I
        }
    )

    val devMacros = listOf(
        MacroItem("Command Palette", "Ctrl+Shift+P", Icons.Default.Code, CyanAccent) {
            val mod = (HidConstants.MOD_LCTRL.toInt() or HidConstants.MOD_LSHIFT.toInt()).toByte()
            onMultiKey(mod, byteArrayOf(0x13.toByte())) // P
        },
        MacroItem("Toggle Terminal", "Ctrl+`", Icons.Default.Terminal, EmeraldSuccess) {
            onMultiKey(HidConstants.MOD_LCTRL, byteArrayOf(0x35.toByte())) // `
        },
        MacroItem("Format Code", "Shift+Alt+F", Icons.Default.Code, PurpleAccent) {
            val mod = (HidConstants.MOD_LSHIFT.toInt() or HidConstants.MOD_LALT.toInt()).toByte()
            onMultiKey(mod, byteArrayOf(0x09.toByte())) // F
        },
        MacroItem("Comment Code", "Ctrl+/", Icons.Default.Code, CyanAccent) {
            onMultiKey(HidConstants.MOD_LCTRL, byteArrayOf(0x38.toByte())) // /
        },
        MacroItem("Git Status", "git status", Icons.Default.Terminal, EmeraldSuccess) {
            onSendText("git status\n")
        },
        MacroItem("Git Pull", "git pull", Icons.Default.Terminal, PurpleAccent) {
            onSendText("git pull\n")
        }
    )

    val webMacros = listOf(
        MacroItem("New Tab", "Ctrl+T", Icons.Default.Tab, CyanAccent) {
            onMultiKey(HidConstants.MOD_LCTRL, byteArrayOf(0x17.toByte())) // T
        },
        MacroItem("Close Tab", "Ctrl+W", Icons.Default.Tab, RoseError) {
            onMultiKey(HidConstants.MOD_LCTRL, byteArrayOf(0x1A.toByte())) // W
        },
        MacroItem("Reopen Tab", "Ctrl+Shift+T", Icons.Default.Tab, EmeraldSuccess) {
            val mod = (HidConstants.MOD_LCTRL.toInt() or HidConstants.MOD_LSHIFT.toInt()).toByte()
            onMultiKey(mod, byteArrayOf(0x17.toByte())) // T
        },
        MacroItem("Hard Refresh", "Ctrl+F5", Icons.Default.Refresh, PurpleAccent) {
            onMultiKey(HidConstants.MOD_LCTRL, byteArrayOf(HidConstants.KEY_F5))
        },
        MacroItem("DevTools (F12)", "Inspect Page", Icons.Default.OpenInBrowser, CyanAccent) {
            onMultiKey(0, byteArrayOf(HidConstants.KEY_F12))
        },
        MacroItem("Search Page", "Ctrl+F", Icons.Default.Search, EmeraldSuccess) {
            onMultiKey(HidConstants.MOD_LCTRL, byteArrayOf(0x09.toByte())) // F
        }
    )

    val streamMacros = listOf(
        MacroItem("Discord Mute", "Ctrl+Shift+M", Icons.Default.MicOff, RoseError) {
            val mod = (HidConstants.MOD_LCTRL.toInt() or HidConstants.MOD_LSHIFT.toInt()).toByte()
            onMultiKey(mod, byteArrayOf(0x10.toByte())) // M
        },
        MacroItem("Discord Deafen", "Ctrl+Shift+D", Icons.Default.Headphones, PurpleAccent) {
            val mod = (HidConstants.MOD_LCTRL.toInt() or HidConstants.MOD_LSHIFT.toInt()).toByte()
            onMultiKey(mod, byteArrayOf(0x07.toByte())) // D
        },
        MacroItem("Xbox Game Bar", "Win+G", Icons.Default.Games, EmeraldSuccess) {
            onMultiKey(HidConstants.MOD_LGUI, byteArrayOf(0x0A.toByte())) // G
        },
        MacroItem("Screen Record", "Win+Alt+R", Icons.Default.PlayCircle, RoseError) {
            val mod = (HidConstants.MOD_LGUI.toInt() or HidConstants.MOD_LALT.toInt()).toByte()
            onMultiKey(mod, byteArrayOf(0x15.toByte())) // R
        },
        MacroItem("Kill Program", "Alt+F4", Icons.Default.Warning, RoseError) {
            onMultiKey(HidConstants.MOD_LALT, byteArrayOf(HidConstants.KEY_F4))
        }
    )

    val currentItems = when (selectedCategory) {
        "Developer" -> devMacros
        "Web / Office" -> webMacros
        "Stream / Game" -> streamMacros
        else -> systemMacros
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Category Pills Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            categories.forEach { cat ->
                val isSel = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSel) CyanAccent.copy(alpha = 0.2f) else Color.Transparent)
                        .then(if (isSel) Modifier.border(1.dp, CyanAccent, RoundedCornerShape(9.dp)) else Modifier)
                        .clickable {
                            selectedCategory = cat
                            onHaptic()
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) CyanAccent else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Macro Tiles Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(currentItems) { macro ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, macro.accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable {
                            macro.action()
                            onHaptic()
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(macro.accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = macro.icon,
                                contentDescription = null,
                                tint = macro.accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = macro.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = macro.subtitle,
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
