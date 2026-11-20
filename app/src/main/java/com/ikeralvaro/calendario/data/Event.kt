package com.ikeralvaro.calendario.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val location: String = "",
    /** Epoch millis, local time zone. */
    val start: Long,
    val end: Long,
    val allDay: Boolean = false,
    val color: Int = 0,
    /** Minutes before start; -1 = no reminder. */
    val reminderMinutes: Int = -1,
) {
    fun startDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(start), zone)

    fun endDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(end), zone)

    /** True if this event touches the given day. */
    fun occursOn(day: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Boolean {
        val s = startDateTime(zone)
        val e = endDateTime(zone)
        val dayStart = day.atStartOfDay()
        val dayEnd = day.plusDays(1).atStartOfDay()
        if (allDay) return !day.isBefore(s.toLocalDate()) && !day.isAfter(e.toLocalDate())
        if (s == e) return s.toLocalDate() == day
        return s.isBefore(dayEnd) && e.isAfter(dayStart)
    }
}

object EventColors {
    val palette = listOf(
        0xFF2F6FED.toInt(), // azul
        0xFFE4572E.toInt(), // coral
        0xFF2BA84A.toInt(), // verde
        0xFFF2A900.toInt(), // ámbar
        0xFF8E5CD9.toInt(), // violeta
        0xFF6B7280.toInt(), // gris
    )
}
