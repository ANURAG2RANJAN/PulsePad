package com.antigravity.btremote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.btremote.theme.CyanAccent
import com.antigravity.btremote.theme.SurfaceBorder
import com.antigravity.btremote.theme.SurfaceDark
import com.antigravity.btremote.theme.SurfaceElevated
import com.antigravity.btremote.theme.TextMuted
import com.antigravity.btremote.theme.TextPrimary

enum class RemoteMode(val title: String, val icon: ImageVector) {
    TRACKPAD("Trackpad", Icons.Default.Mouse),
    KEYBOARD("Keyboard", Icons.Default.Keyboard),
    MEDIA("Media", Icons.Default.PlayCircleOutline),
    NUMPAD("Numpad", Icons.Default.Dialpad),
    MACROS("Macros", Icons.Default.Widgets)
}

@Composable
fun ModeSelectorBar(
    currentMode: RemoteMode,
    onModeSelected: (RemoteMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RemoteMode.values().forEach { mode ->
                val isSelected = mode == currentMode
                val bgAnim by animateColorAsState(
                    targetValue = if (isSelected) SurfaceElevated else SurfaceDark,
                    animationSpec = tween(200),
                    label = "tabBg"
                )
                val tintAnim by animateColorAsState(
                    targetValue = if (isSelected) CyanAccent else TextMuted,
                    animationSpec = tween(200),
                    label = "tabTint"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgAnim)
                        .then(
                            if (isSelected) Modifier.border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            else Modifier
                        )
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = mode.icon,
                            contentDescription = mode.title,
                            tint = tintAnim,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = mode.title,
                            color = if (isSelected) TextPrimary else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
