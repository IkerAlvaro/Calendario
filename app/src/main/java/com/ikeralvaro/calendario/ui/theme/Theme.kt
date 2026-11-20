package com.ikeralvaro.calendario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Accent = Color(0xFFE4572E)

private val Light = lightColorScheme(
    primary = Color(0xFF1F1F1F),
    onPrimary = Color.White,
    secondary = Accent,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF1F1F1F),
    surface = Color.White,
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFF71717A),
    outline = Color(0xFFE4E4E7),
    outlineVariant = Color(0xFFF0F0F2),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFF4F4F5),
    onPrimary = Color(0xFF121212),
    secondary = Accent,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF1E1E20),
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF2E2E32),
    outlineVariant = Color(0xFF232326),
)

private val Type = Typography(
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
)

@Composable
fun CalendarioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, typography = Type, content = content)
}
