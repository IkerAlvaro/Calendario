package com.ikeralvaro.calendario.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.ikeralvaro.calendario.data.Settings
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val Es: Locale = Locale.forLanguageTag("es-ES")

fun String.cap() = replaceFirstChar { it.titlecase(Es) }

val LocalSettings = staticCompositionLocalOf { Settings() }

fun timeFormatter(use24h: Boolean): DateTimeFormatter =
    if (use24h) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("h:mm a", Es)

@Composable @ReadOnlyComposable
fun LocalTime.hm(): String = format(timeFormatter(LocalSettings.current.use24h))

@Composable @ReadOnlyComposable
fun LocalDateTime.hm(): String = toLocalTime().hm()

@Composable @ReadOnlyComposable
fun hourLabel(h: Int): String =
    if (LocalSettings.current.use24h) "%02d:00".format(h)
    else "${if (h % 12 == 0) 12 else h % 12} ${if (h < 12) "a. m." else "p. m."}"

fun relativeDay(d: LocalDate, today: LocalDate = LocalDate.now()): String = when (d) {
    today -> "Hoy"
    today.plusDays(1) -> "Mañana"
    today.minusDays(1) -> "Ayer"
    else -> d.format(DateTimeFormatter.ofPattern(if (d.year == today.year) "EEE d MMM" else "EEE d MMM yyyy", Es)).cap()
}

fun greeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
    in 6..12 -> "Buenos días"
    in 13..20 -> "Buenas tardes"
    else -> "Buenas noches"
}

/** "Ahora", "en 25 min", "en 2 h 15 min", "en 3 días". */
fun countdown(now: LocalDateTime, start: LocalDateTime, end: LocalDateTime): String {
    if (!now.isBefore(start) && now.isBefore(end)) return "Ahora"
    val m = Duration.between(now, start).toMinutes().coerceAtLeast(0)
    return when {
        m < 1 -> "Ya"
        m < 60 -> "en $m min"
        m < 24 * 60 -> if (m % 60 == 0L) "en ${m / 60} h" else "en ${m / 60} h ${m % 60} min"
        else -> { val d = m / (24 * 60); "en $d día${if (d > 1) "s" else ""}" }
    }
}

val reminderOptions = listOf(-1 to "Ninguno", 0 to "Al empezar", 5 to "5 min", 10 to "10 min",
    30 to "30 min", 60 to "1 hora", 1440 to "1 día")

fun reminderLabel(min: Int): String = when (min) {
    -1 -> "Sin aviso"
    0 -> "Al empezar"
    else -> (reminderOptions.firstOrNull { it.first == min }?.second ?: "$min min") + " antes"
}
