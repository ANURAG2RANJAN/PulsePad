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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.btremote.hid.HidConstants
import com.antigravity.btremote.theme.CyanAccent
import com.antigravity.btremote.theme.KeyBg
import com.antigravity.btremote.theme.KeyBorder
import com.antigravity.btremote.theme.SurfaceBorder
import com.antigravity.btremote.theme.SurfaceDark
import com.antigravity.btremote.theme.TextMuted
import com.antigravity.btremote.theme.TextPrimary
import com.antigravity.btremote.theme.TextSecondary

@Composable
fun NumpadScreen(
    onKeyPress: (Byte, Byte?) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NUMERIC KEYPAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "EXCEL / DATA ENTRY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 1: NumLock, /, *, Backspace
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumpadKey("NUM", onClick = { onKeyPress(HidConstants.KEY_NUM_LOCK, null); onHaptic() }, isSpecial = true, modifier = Modifier.weight(1f))
                    NumpadKey("/", onClick = { onKeyPress(HidConstants.KEY_KP_DIVIDE, null); onHaptic() }, isSpecial = true, modifier = Modifier.weight(1f))
                    NumpadKey("*", onClick = { onKeyPress(HidConstants.KEY_KP_MULTIPLY, null); onHaptic() }, isSpecial = true, modifier = Modifier.weight(1f))
                    NumpadKey("⌫", onClick = { onKeyPress(HidConstants.KEY_BACKSPACE, null); onHaptic() }, isSpecial = true, modifier = Modifier.weight(1f))
                }

                // Row 2: 7, 8, 9, -
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumpadKey("7", onClick = { onKeyPress(HidConstants.KEY_KP_7, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("8", onClick = { onKeyPress(HidConstants.KEY_KP_8, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("9", onClick = { onKeyPress(HidConstants.KEY_KP_9, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("-", onClick = { onKeyPress(HidConstants.KEY_KP_MINUS, null); onHaptic() }, isSpecial = true, modifier = Modifier.weight(1f))
                }

                // Row 3: 4, 5, 6, +
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumpadKey("4", onClick = { onKeyPress(HidConstants.KEY_KP_4, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("5", onClick = { onKeyPress(HidConstants.KEY_KP_5, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("6", onClick = { onKeyPress(HidConstants.KEY_KP_6, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("+", onClick = { onKeyPress(HidConstants.KEY_KP_PLUS, null); onHaptic() }, isSpecial = true, modifier = Modifier.weight(1f))
                }

                // Row 4: 1, 2, 3, ENTER
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumpadKey("1", onClick = { onKeyPress(HidConstants.KEY_KP_1, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("2", onClick = { onKeyPress(HidConstants.KEY_KP_2, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("3", onClick = { onKeyPress(HidConstants.KEY_KP_3, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("ENTER", onClick = { onKeyPress(HidConstants.KEY_KP_ENTER, null); onHaptic() }, isAccent = true, modifier = Modifier.weight(1f))
                }

                // Row 5: 0 (wide 2x), ., ENTER repeat or Tab
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumpadKey("0", onClick = { onKeyPress(HidConstants.KEY_KP_0, null); onHaptic() }, modifier = Modifier.weight(2f))
                    NumpadKey(".", onClick = { onKeyPress(HidConstants.KEY_KP_DOT, null); onHaptic() }, modifier = Modifier.weight(1f))
                    NumpadKey("TAB", onClick = { onKeyPress(HidConstants.KEY_TAB, null); onHaptic() }, isSpecial = true, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun NumpadKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isAccent: Boolean = false
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isAccent -> CyanAccent.copy(alpha = 0.2f)
                    isSpecial -> KeyBg.copy(alpha = 0.8f)
                    else -> KeyBg
                }
            )
            .border(
                1.dp,
                if (isAccent) CyanAccent else KeyBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 2) 13.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = if (isAccent) CyanAccent else if (isSpecial) TextSecondary else TextPrimary
        )
    }
}
