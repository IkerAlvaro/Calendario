package com.ikeralvaro.calendario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Accent = Color(0xFFE8111E)
val AccentSoft = Color(0xFFFFE5E7)

private val Light = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = AccentSoft,
    onPrimaryContainer = Color(0xFF8A0610),
    secondary = Color(0xFF1C1C1E),
    onSecondary = Color.White,
    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFEDEDF0),
    onSurfaceVariant = Color(0xFF7C7C84),
    outline = Color(0xFFE3E3E8),
    outlineVariant = Color(0xFFF0F0F3),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFFF3B45),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A0A0F),
    onPrimaryContainer = Color(0xFFFFD6D9),
    secondary = Color(0xFFF2F2F7),
    onSecondary = Color(0xFF121214),
    background = Color(0xFF0E0E10),
    onBackground = Color(0xFFF2F2F7),
    surface = Color(0xFF1A1A1D),
    onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF26262A),
    onSurfaceVariant = Color(0xFF9A9AA3),
    outline = Color(0xFF2C2C31),
    outlineVariant = Color(0xFF222226),
)

private val Type = Typography(
    displaySmall = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun CalendarioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = Type,
        shapes = AppShapes,
        content = content,
    )
}
