package com.ikeralvaro.calendario.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class Recurrence(val label: String) {
    NONE("No se repite"), DAILY("Cada día"), WEEKLY("Cada semana"), MONTHLY("Cada mes"), YEARLY("Cada año");
}

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val location: String = "",
    /** Epoch millis. */
    val start: Long,
    val end: Long,
    val allDay: Boolean = false,
    val color: Int = 0,
    /** Minutes before start; -1 = no reminder. */
    val reminderMinutes: Int = -1,
    /** [Recurrence] ordinal. */
    @ColumnInfo(defaultValue = "0") val recurrence: Int = 0,
    /** Last day (epoch day, inclusive) on which a repetition may start; null = forever. */
    val recurrenceEnd: Long? = null,
) {
    val rule: Recurrence get() = Recurrence.entries.getOrElse(recurrence) { Recurrence.NONE }

    fun startDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(start), zone)

    fun endDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(end), zone)

    /** Does a repetition of this event start on [day]? */
    fun startsOn(day: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Boolean {
        val first = startDateTime(zone).toLocalDate()
        if (day.isBefore(first)) return false
        recurrenceEnd?.let { if (day.toEpochDay() > it) return false }
        return when (rule) {
            Recurrence.NONE -> day == first
            Recurrence.DAILY -> true
            Recurrence.WEEKLY -> ChronoUnit.DAYS.between(first, day) % 7 == 0L
            Recurrence.MONTHLY -> day.dayOfMonth == first.dayOfMonth
            Recurrence.YEARLY -> day.dayOfMonth == first.dayOfMonth && day.month == first.month
        }
    }

    /** The concrete occurrence starting on [day] (caller must check [startsOn]). */
    fun occurrenceStartingOn(day: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Occurrence {
        val s = startDateTime(zone)
        val shift = ChronoUnit.DAYS.between(s.toLocalDate(), day)
        return Occurrence(this, s.plusDays(shift), endDateTime(zone).plusDays(shift))
    }

    /** Occurrences of this event that touch [day]. */
    fun occurrencesOn(day: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<Occurrence> {
        val s = startDateTime(zone)
        val spanDays = ChronoUnit.DAYS.between(s.toLocalDate(), endDateTime(zone).toLocalDate()).coerceIn(0, 366)
        if (rule == Recurrence.NONE) {
            val o = Occurrence(this, s, endDateTime(zone))
            return if (o.touches(day)) listOf(o) else emptyList()
        }
        val out = ArrayList<Occurrence>(1)
        for (k in spanDays downTo 0) {
            val startDay = day.minusDays(k)
            if (startsOn(startDay, zone)) {
                val o = occurrenceStartingOn(startDay, zone)
                if (o.touches(day)) out.add(o)
            }
        }
        return out
    }

    fun occursOn(day: LocalDate, zone: ZoneId = ZoneId.systemDefault()) = occurrencesOn(day, zone).isNotEmpty()

    /** First occurrence whose start is at or after [after], searching up to ~4 years ahead. */
    fun nextOccurrence(after: LocalDateTime, zone: ZoneId = ZoneId.systemDefault()): Occurrence? {
        val s = startDateTime(zone)
        if (rule == Recurrence.NONE) return if (!s.isBefore(after)) Occurrence(this, s, endDateTime(zone)) else null
        var day = maxOf(after.toLocalDate(), s.toLocalDate())
        repeat(1500) {
            if (startsOn(day, zone)) {
                val o = occurrenceStartingOn(day, zone)
                if (!o.start.isBefore(after)) return o
            }
            day = day.plusDays(1)
        }
        return null
    }
}

/** A concrete instance of an [Event] (equal to the event itself unless it repeats). */
data class Occurrence(val event: Event, val start: LocalDateTime, val end: LocalDateTime) {
    fun touches(day: LocalDate): Boolean {
        if (event.allDay) return !day.isBefore(start.toLocalDate()) && !day.isAfter(end.toLocalDate())
        if (start == end) return start.toLocalDate() == day
        return start.isBefore(day.plusDays(1).atStartOfDay()) && end.isAfter(day.atStartOfDay())
    }
}

/** All occurrences touching [day], all-day first then by start time. */
fun List<Event>.occurrencesOn(day: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<Occurrence> =
    flatMap { it.occurrencesOn(day, zone) }
        .sortedWith(compareBy<Occurrence>({ !it.event.allDay }, { it.start }, { it.event.title }))

/** The next occurrence that hasn't finished yet, within [days] days. */
fun List<Event>.upcoming(now: LocalDateTime, days: Int = 7, zone: ZoneId = ZoneId.systemDefault()): Occurrence? {
    for (i in 0..days) {
        val d = now.toLocalDate().plusDays(i.toLong())
        occurrencesOn(d, zone).filter { !it.event.allDay && it.end.isAfter(now) }
            .minByOrNull { it.start }?.let { return it }
    }
    return null
}

object EventColors {
    val palette = listOf(
        0xFFE8111E.toInt(), // rojo
        0xFF2F6FED.toInt(), // azul
        0xFF14A06B.toInt(), // verde
        0xFFF29D00.toInt(), // ámbar
        0xFF8E5CD9.toInt(), // violeta
        0xFFE0457B.toInt(), // rosa
        0xFF0EA5B7.toInt(), // turquesa
        0xFF5F6B7A.toInt(), // pizarra
    )
}
