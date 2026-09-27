package com.antigravity.btremote.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.btremote.hid.HidConstants
import com.antigravity.btremote.model.RemoteSettings
import com.antigravity.btremote.model.TargetPlatform
import com.antigravity.btremote.theme.AmberWarning
import com.antigravity.btremote.theme.CyanAccent
import com.antigravity.btremote.theme.EmeraldSuccess
import com.antigravity.btremote.theme.KeyBg
import com.antigravity.btremote.theme.KeyBorder
import com.antigravity.btremote.theme.PurpleAccent
import com.antigravity.btremote.theme.SurfaceBorder
import com.antigravity.btremote.theme.SurfaceDark
import com.antigravity.btremote.theme.SurfaceElevated
import com.antigravity.btremote.theme.TextMuted
import com.antigravity.btremote.theme.TextPrimary
import com.antigravity.btremote.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrackpadScreen(
    settings: RemoteSettings,
    isAirMouseAvailable: Boolean,
    isAirMouseActive: Boolean,
    reportRateHz: Int,
    packetsSent: Long,
    onToggleAirMouse: () -> Unit,
    onMouseMove: (dx: Int, dy: Int) -> Unit,
    onMouseClick: (button: Byte) -> Unit,
    onMouseButtonDown: (button: Byte, isDown: Boolean) -> Unit,
    onMouseWheel: (wheelDelta: Int) -> Unit,
    onMultiKey: (modifier: Byte, keys: ByteArray) -> Unit,
    onConsumerKey: (Short) -> Unit = {},
    onKeyPress: (Byte, Byte?) -> Unit = { _, _ -> },
    onKeyCombo: (Byte, Byte) -> Unit = { _, _ -> },
    onHaptic: () -> Unit,
    onMouseSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isDraggingMode by remember { mutableStateOf(false) }
    var touchPos by remember { mutableStateOf<Offset?>(null) }
    var threeFingerStart by remember { mutableStateOf<Offset?>(null) }
    var threeFingerTriggered by remember { mutableStateOf(false) }
    var currentDpiMode by remember { mutableStateOf("Normal") }

    // Physical button press states
    var leftPressed by remember { mutableStateOf(false) }
    var midPressed by remember { mutableStateOf(false) }
    var rightPressed by remember { mutableStateOf(false) }

    val leftScale by animateFloatAsState(if (leftPressed) 0.93f else 1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "leftScale")
    val leftOffsetY by animateDpAsState(if (leftPressed) 3.dp else 0.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "leftOffset")

    val midScale by animateFloatAsState(if (midPressed) 0.93f else 1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "midScale")
    val midOffsetY by animateDpAsState(if (midPressed) 3.dp else 0.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "midOffset")

    val rightScale by animateFloatAsState(if (rightPressed) 0.93f else 1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "rightScale")
    val rightOffsetY by animateDpAsState(if (rightPressed) 3.dp else 0.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "rightOffset")

    val infiniteTransition = rememberInfiniteTransition(label = "gyroGimbal")
    val gimbalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "gimbalRotation"
    )

    var touchDownTime by remember { mutableStateOf(0L) }
    var hasMovedSignificant by remember { mutableStateOf(false) }

    val effectiveSensitivity = when (currentDpiMode) {
        "Precision" -> settings.pointerSensitivity * 0.6f
        "Fast" -> settings.pointerSensitivity * 1.8f
        else -> settings.pointerSensitivity
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Scientific Telemetry & Air Mouse Status Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Telemetry Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${reportRateHz.coerceAtLeast(0)} Hz",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${packetsSent} pkts",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            // Air Mouse (Gyro) Toggle Button
            if (isAirMouseAvailable) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAirMouseActive) PurpleAccent.copy(alpha = 0.25f) else SurfaceDark)
                        .border(1.dp, if (isAirMouseActive) PurpleAccent else SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            onToggleAirMouse()
                            onHaptic()
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Air Mouse",
                            tint = if (isAirMouseActive) PurpleAccent else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAirMouseActive) "AIR MOUSE: ON" else "AIR MOUSE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAirMouseActive) PurpleAccent else TextMuted
                        )
                    }
                }
            }

            // DPI Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                listOf("Precision", "Normal", "Fast").forEach { mode ->
                    val isSel = currentDpiMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) CyanAccent.copy(alpha = 0.2f) else Color.Transparent)
                            .then(if (isSel) Modifier.border(1.dp, CyanAccent, RoundedCornerShape(6.dp)) else Modifier)
                            .pointerInput(mode) {
                                detectTapGestures {
                                    currentDpiMode = mode
                                    onHaptic()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = mode,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) CyanAccent else TextMuted
                        )
                    }
                }
            }
        }

        // Gesture Hints
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.TouchApp, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            val hintText = if (isAirMouseActive) {
                "Air Mouse Active — Tilt phone in 3D air to move cursor"
            } else {
                when (settings.targetPlatform) {
                    TargetPlatform.ANDROID -> "Android: 1-Tap Left • Right Click = Back • Mid Click = Home"
                    TargetPlatform.APPLE -> "Apple: 1-Tap Select • 2-Tap Secondary • AssistiveTouch for pointer"
                    TargetPlatform.SMART_TV -> "Smart TV: Touchpad moves pointer • Quick Bar buttons control TV"
                    TargetPlatform.WINDOWS -> "1-Tap: Left • 2-Tap: Right • 3-Swipe: Window Switch / Desktop"
                }
            }
            Text(
                text = hintText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isAirMouseActive) PurpleAccent else TextSecondary
            )
        }

        // Main Touchpad Area with Side Scroll Strip
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Touch Surface
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .border(
                        1.5.dp,
                        when {
                            isAirMouseActive -> PurpleAccent.copy(alpha = 0.7f)
                            isDraggingMode -> AmberWarning
                            else -> CyanAccent.copy(alpha = 0.35f)
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                touchPos = Offset(motionEvent.x, motionEvent.y)
                                threeFingerTriggered = false
                                touchDownTime = System.currentTimeMillis()
                                hasMovedSignificant = false
                            }
                            MotionEvent.ACTION_POINTER_DOWN -> {
                                if (motionEvent.pointerCount == 3) {
                                    val avgX = (motionEvent.getX(0) + motionEvent.getX(1) + motionEvent.getX(2)) / 3f
                                    val avgY = (motionEvent.getY(0) + motionEvent.getY(1) + motionEvent.getY(2)) / 3f
                                    threeFingerStart = Offset(avgX, avgY)
                                    threeFingerTriggered = false
                                }
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val pointerCount = motionEvent.pointerCount
                                if (pointerCount == 1) {
                                    val currentX = motionEvent.x
                                    val currentY = motionEvent.y
                                    val last = touchPos
                                    if (last != null) {
                                        val dx = ((currentX - last.x) * effectiveSensitivity).roundToInt()
                                        val dy = ((currentY - last.y) * effectiveSensitivity).roundToInt()
                                        if (abs(dx) > 0 || abs(dy) > 0) {
                                            hasMovedSignificant = true
                                            onMouseMove(dx, dy)
                                        }
                                    }
                                    touchPos = Offset(currentX, currentY)
                                } else if (pointerCount == 2) {
                                    // 2-finger scroll
                                    val y1 = motionEvent.getY(0)
                                    val last = touchPos
                                    if (last != null) {
                                        val deltaY = (y1 - last.y) * settings.scrollSensitivity
                                        val wheel = (deltaY / 12f).roundToInt()
                                        if (wheel != 0) {
                                            val dir = if (settings.invertScroll) -wheel else wheel
                                            onMouseWheel(dir)
                                        }
                                    }
                                    touchPos = Offset(motionEvent.getX(0), y1)
                                } else if (pointerCount == 3 && !threeFingerTriggered) {
                                    // 3-Finger Native OS Gestures
                                    val start = threeFingerStart
                                    if (start != null) {
                                        val avgX = (motionEvent.getX(0) + motionEvent.getX(1) + motionEvent.getX(2)) / 3f
                                        val avgY = (motionEvent.getY(0) + motionEvent.getY(1) + motionEvent.getY(2)) / 3f
                                        val dX = avgX - start.x
                                        val dY = avgY - start.y

                                        if (dY < -90f && abs(dY) > abs(dX)) {
                                            // 3-Finger Swipe Up: Task View (Win + Tab)
                                            threeFingerTriggered = true
                                            onMultiKey(HidConstants.MOD_LGUI, byteArrayOf(HidConstants.KEY_TAB))
                                            onHaptic()
                                        } else if (dY > 90f && abs(dY) > abs(dX)) {
                                            // 3-Finger Swipe Down: Show Desktop (Win + D)
                                            threeFingerTriggered = true
                                            onMultiKey(HidConstants.MOD_LGUI, byteArrayOf(0x07.toByte()))
                                            onHaptic()
                                        } else if (dX < -100f && abs(dX) > abs(dY)) {
                                            // 3-Finger Swipe Left: Switch Desktop Left (Win + Ctrl + Left)
                                            threeFingerTriggered = true
                                            val mod = (HidConstants.MOD_LGUI.toInt() or HidConstants.MOD_LCTRL.toInt()).toByte()
                                            onMultiKey(mod, byteArrayOf(HidConstants.KEY_LEFT))
                                            onHaptic()
                                        } else if (dX > 100f && abs(dX) > abs(dY)) {
                                            // 3-Finger Swipe Right: Switch Desktop Right (Win + Ctrl + Right)
                                            threeFingerTriggered = true
                                            val mod = (HidConstants.MOD_LGUI.toInt() or HidConstants.MOD_LCTRL.toInt()).toByte()
                                            onMultiKey(mod, byteArrayOf(HidConstants.KEY_RIGHT))
                                            onHaptic()
                                        }
                                    }
                                }
                            }
                            MotionEvent.ACTION_POINTER_UP -> {
                                if (motionEvent.pointerCount == 2 && !threeFingerTriggered) {
                                    onMouseClick(HidConstants.MOUSE_BTN_RIGHT)
                                    onHaptic()
                                    onMouseSound()
                                }
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                val pressDuration = System.currentTimeMillis() - touchDownTime
                                if (!hasMovedSignificant && pressDuration < 280 && !threeFingerTriggered && motionEvent.pointerCount == 1) {
                                    onMouseClick(HidConstants.MOUSE_BTN_LEFT)
                                    onHaptic()
                                    onMouseSound()
                                }
                                touchPos = null
                                threeFingerStart = null
                                threeFingerTriggered = false
                                if (isDraggingMode) {
                                    isDraggingMode = false
                                    onMouseButtonDown(HidConstants.MOUSE_BTN_LEFT, false)
                                    onHaptic()
                                }
                            }
                        }
                        true
                    },
                contentAlignment = Alignment.Center
            ) {
                // Subtle Grid Canvas with Radar Ripple
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 46.dp.toPx()
                    val dotColor = if (isAirMouseActive) Color(0x22E879F9) else Color(0x1A00E5FF)
                    var x = step
                    while (x < size.width) {
                        var y = step
                        while (y < size.height) {
                            drawCircle(dotColor, radius = 1.5.dp.toPx(), center = Offset(x, y))
                            y += step
                        }
                        x += step
                    }

                    // Rotating Aerospace Gimbal HUD when Air Mouse is active
                    if (isAirMouseActive) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        rotate(gimbalRotation, pivot = center) {
                            drawCircle(
                                color = PurpleAccent.copy(alpha = 0.22f),
                                radius = 65.dp.toPx(),
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                            val r = 65.dp.toPx()
                            val tickLen = 12.dp.toPx()
                            drawLine(PurpleAccent.copy(alpha = 0.7f), Offset(center.x - r, center.y), Offset(center.x - r + tickLen, center.y), 2.dp.toPx())
                            drawLine(PurpleAccent.copy(alpha = 0.7f), Offset(center.x + r, center.y), Offset(center.x + r - tickLen, center.y), 2.dp.toPx())
                            drawLine(PurpleAccent.copy(alpha = 0.7f), Offset(center.x, center.y - r), Offset(center.x, center.y - r + tickLen), 2.dp.toPx())
                            drawLine(PurpleAccent.copy(alpha = 0.7f), Offset(center.x, center.y + r), Offset(center.x, center.y + r - tickLen), 2.dp.toPx())
                        }
                    }

                    // Animated Concentric Sonar Ripple Waves on Touch
                    touchPos?.let { pos ->
                        val pulseColor = if (isDraggingMode) AmberWarning else if (isAirMouseActive) PurpleAccent else CyanAccent
                        drawCircle(
                            color = pulseColor.copy(alpha = 0.18f),
                            radius = 36.dp.toPx(),
                            center = pos
                        )
                        drawCircle(
                            color = pulseColor.copy(alpha = 0.55f),
                            radius = 22.dp.toPx(),
                            center = pos,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.95f),
                            radius = 4.5.dp.toPx(),
                            center = pos
                        )
                    }
                }

                // Air Mouse / Drag Badges
                if (isAirMouseActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PurpleAccent.copy(alpha = 0.15f))
                            .border(1.dp, PurpleAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GYROSCOPIC 6-DOF TRACKING", color = PurpleAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Tilt phone to direct mouse cursor", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                } else if (isDraggingMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberWarning.copy(alpha = 0.2f))
                            .border(1.dp, AmberWarning, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Holding Left Button (Drag Mode)", color = AmberWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Dedicated Fast Vertical Scroll Strip
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onHaptic() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val delta = (dragAmount.y * settings.scrollSensitivity / 8f).roundToInt()
                                if (delta != 0) {
                                    val dir = if (settings.invertScroll) delta else -delta
                                    onMouseWheel(dir)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Scroll", tint = CyanAccent.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SCROLL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Platform-tailored OS Quick Actions Bar
        OsQuickActionBar(
            platform = settings.targetPlatform,
            onConsumerKey = onConsumerKey,
            onKeyPress = onKeyPress,
            onKeyCombo = onKeyCombo,
            onHaptic = onHaptic
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Physical Bottom Mouse Click Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Click (Primary)
            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
                    .offset(y = leftOffsetY)
                    .scale(leftScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (leftPressed) CyanAccent.copy(alpha = 0.35f) else KeyBg)
                    .border(1.dp, if (leftPressed) CyanAccent else KeyBorder, RoundedCornerShape(16.dp))
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                leftPressed = true
                                onMouseButtonDown(HidConstants.MOUSE_BTN_LEFT, true)
                                onHaptic()
                                onMouseSound()
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                leftPressed = false
                                onMouseButtonDown(HidConstants.MOUSE_BTN_LEFT, false)
                            }
                        }
                        true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LEFT CLICK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (leftPressed) CyanAccent else TextPrimary
                )
            }

            // Middle Click (Wheel Click)
            Box(
                modifier = Modifier
                    .weight(0.20f)
                    .fillMaxHeight()
                    .offset(y = midOffsetY)
                    .scale(midScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (midPressed) PurpleAccent.copy(alpha = 0.35f) else KeyBg)
                    .border(1.dp, if (midPressed) PurpleAccent else KeyBorder, RoundedCornerShape(16.dp))
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                midPressed = true
                                onMouseButtonDown(HidConstants.MOUSE_BTN_MIDDLE, true)
                                onHaptic()
                                onMouseSound()
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                midPressed = false
                                onMouseButtonDown(HidConstants.MOUSE_BTN_MIDDLE, false)
                            }
                        }
                        true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MID",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (midPressed) PurpleAccent else TextSecondary
                )
            }

            // Right Click
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .offset(y = rightOffsetY)
                    .scale(rightScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (rightPressed) CyanAccent.copy(alpha = 0.35f) else KeyBg)
                    .border(1.dp, if (rightPressed) CyanAccent else KeyBorder, RoundedCornerShape(16.dp))
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                rightPressed = true
                                onMouseButtonDown(HidConstants.MOUSE_BTN_RIGHT, true)
                                onHaptic()
                                onMouseSound()
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                rightPressed = false
                                onMouseButtonDown(HidConstants.MOUSE_BTN_RIGHT, false)
                            }
                        }
                        true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RIGHT CLICK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (rightPressed) CyanAccent else TextPrimary
                )
            }
        }
    }
}

@Composable
fun OsQuickActionBar(
    platform: TargetPlatform,
    onConsumerKey: (Short) -> Unit,
    onKeyPress: (Byte, Byte?) -> Unit,
    onKeyCombo: (Byte, Byte) -> Unit,
    onHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (platform) {
            TargetPlatform.WINDOWS -> {
                QuickActionButton("⊞ Start", Modifier.weight(1f)) {
                    onKeyPress(HidConstants.KEY_NONE, HidConstants.MOD_LGUI)
                    onHaptic()
                }
                QuickActionButton("Desktop", Modifier.weight(1f)) {
                    onKeyCombo(HidConstants.MOD_LGUI, 0x07.toByte()) // Win+D
                    onHaptic()
                }
                QuickActionButton("Task View", Modifier.weight(1.1f)) {
                    onKeyCombo(HidConstants.MOD_LGUI, HidConstants.KEY_TAB) // Win+Tab
                    onHaptic()
                }
                QuickActionButton("Action Ctr", Modifier.weight(1.1f)) {
                    onKeyCombo(HidConstants.MOD_LGUI, 0x04.toByte()) // Win+A
                    onHaptic()
                }
                QuickActionButton("Snip", Modifier.weight(0.9f)) {
                    onKeyCombo((HidConstants.MOD_LGUI.toInt() or HidConstants.MOD_LSHIFT.toInt()).toByte(), 0x16.toByte()) // Win+Shift+S
                    onHaptic()
                }
            }
            TargetPlatform.ANDROID -> {
                QuickActionButton("◀ Back", Modifier.weight(1f), isAccent = true) {
                    onConsumerKey(HidConstants.CONSUMER_AC_BACK)
                    onHaptic()
                }
                QuickActionButton("⌂ Home", Modifier.weight(1f), isAccent = true) {
                    onConsumerKey(HidConstants.CONSUMER_AC_HOME)
                    onHaptic()
                }
                QuickActionButton("▢ Recents", Modifier.weight(1.1f)) {
                    onKeyCombo(HidConstants.MOD_LALT, HidConstants.KEY_TAB)
                    onHaptic()
                }
                QuickActionButton("🔍 Search", Modifier.weight(1f)) {
                    onConsumerKey(HidConstants.CONSUMER_AC_SEARCH)
                    onHaptic()
                }
                QuickActionButton("⚡ Power", Modifier.weight(0.9f)) {
                    onConsumerKey(HidConstants.CONSUMER_POWER)
                    onHaptic()
                }
            }
            TargetPlatform.APPLE -> {
                QuickActionButton("⌂ Home", Modifier.weight(1f), isAccent = true) {
                    onConsumerKey(HidConstants.CONSUMER_AC_HOME)
                    onHaptic()
                }
                QuickActionButton("🔍 Spotlight", Modifier.weight(1.2f)) {
                    onKeyCombo(HidConstants.MOD_LGUI, HidConstants.KEY_SPACE) // ⌘+Space
                    onHaptic()
                }
                QuickActionButton("⇥ Switcher", Modifier.weight(1.1f)) {
                    onKeyCombo(HidConstants.MOD_LGUI, HidConstants.KEY_TAB) // ⌘+Tab
                    onHaptic()
                }
                QuickActionButton("📸 Screen", Modifier.weight(1f)) {
                    onKeyCombo((HidConstants.MOD_LGUI.toInt() or HidConstants.MOD_LSHIFT.toInt()).toByte(), 0x20.toByte()) // ⌘+Shift+3
                    onHaptic()
                }
                QuickActionButton("🔒 Lock", Modifier.weight(0.9f)) {
                    onConsumerKey(HidConstants.CONSUMER_POWER)
                    onHaptic()
                }
            }
            TargetPlatform.SMART_TV -> {
                QuickActionButton("◀ Back", Modifier.weight(1f), isAccent = true) {
                    onConsumerKey(HidConstants.CONSUMER_AC_BACK)
                    onHaptic()
                }
                QuickActionButton("⌂ Home", Modifier.weight(1f), isAccent = true) {
                    onConsumerKey(HidConstants.CONSUMER_AC_HOME)
                    onHaptic()
                }
                QuickActionButton("☰ Menu", Modifier.weight(1f)) {
                    onConsumerKey(HidConstants.CONSUMER_MENU)
                    onHaptic()
                }
                QuickActionButton("🔇 Mute", Modifier.weight(0.9f)) {
                    onConsumerKey(HidConstants.CONSUMER_MUTE)
                    onHaptic()
                }
                QuickActionButton("⚡ Power", Modifier.weight(0.9f)) {
                    onConsumerKey(HidConstants.CONSUMER_POWER)
                    onHaptic()
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isAccent) CyanAccent.copy(alpha = 0.18f) else SurfaceElevated)
            .border(
                1.dp,
                if (isAccent) CyanAccent.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isAccent) CyanAccent else TextPrimary,
            maxLines = 1
        )
    }
}
