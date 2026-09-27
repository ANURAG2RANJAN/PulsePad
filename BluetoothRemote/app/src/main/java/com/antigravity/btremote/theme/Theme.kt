package com.antigravity.btremote.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CustomDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = KeyActiveText,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = PurpleAccent,
    onSecondary = TextPrimary,
    tertiary = EmeraldSuccess,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder
)

@Composable
fun BluetoothRemoteTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomDarkColorScheme,
        typography = Typography,
        content = content
    )
}
