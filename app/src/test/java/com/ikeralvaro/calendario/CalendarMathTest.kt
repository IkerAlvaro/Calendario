package com.ikeralvaro.calendario

import com.ikeralvaro.calendario.data.CalendarMath
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.Recurrence
import com.ikeralvaro.calendario.data.occurrencesOn
import com.ikeralvaro.calendario.data.upcoming
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

class CalendarMathTest {
    private val z = ZoneOffset.UTC
    private fun ms(dt: LocalDateTime) = dt.toInstant(z).toEpochMilli()
    private fun ev(start: LocalDateTime, end: LocalDateTime, rule: Recurrence = Recurrence.NONE, until: LocalDate? = null, allDay: Boolean = false) =
        Event(id = 1, title = "x", start = ms(start), end = ms(end), allDay = allDay, recurrence = rule.ordinal, recurrenceEnd = until?.toEpochDay())

    @Test fun monthGridStartsOnMondayAndHas42Days() {
        val g = CalendarMath.monthGrid(YearMonth.of(2026, 11))
        assertEquals(42, g.size)
        assertEquals(DayOfWeek.MONDAY, g.first().dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 26), g.first())
        assertTrue(g.contains(LocalDate.of(2026, 11, 30)))
    }

    @Test fun monthGridCanStartOnSunday() {
        val g = CalendarMath.monthGrid(YearMonth.of(2026, 11), DayOfWeek.SUNDAY)
        assertEquals(LocalDate.of(2026, 11, 1), g.first())
        assertEquals(DayOfWeek.SUNDAY, CalendarMath.weekHeader(DayOfWeek.SUNDAY).first())
    }

    @Test fun overlappingEventsGetSeparateColumns() {
        val r = CalendarMath.layoutColumns(listOf(60 to 120, 90 to 150, 200 to 260))
        assertEquals(0 to 2, r[0]); assertEquals(1 to 2, r[1]); assertEquals(0 to 1, r[2])
    }

    @Test fun multiDayEventOccursOnEachDay() {
        val e = ev(LocalDateTime.of(2026, 1, 1, 22, 0), LocalDateTime.of(2026, 1, 2, 2, 0))
        assertTrue(e.occursOn(LocalDate.of(2026, 1, 1), z))
        assertTrue(e.occursOn(LocalDate.of(2026, 1, 2), z))
        assertFalse(e.occursOn(LocalDate.of(2026, 1, 3), z))
    }

    @Test fun eventEndingAtMidnightDoesNotSpillOver() {
        val e = ev(LocalDateTime.of(2026, 1, 1, 23, 0), LocalDateTime.of(2026, 1, 2, 0, 0))
        assertFalse(e.occursOn(LocalDate.of(2026, 1, 2), z))
    }

    @Test fun dailyRepeatsUntilEndDate() {
        val e = ev(LocalDateTime.of(2026, 3, 10, 9, 0), LocalDateTime.of(2026, 3, 10, 10, 0), Recurrence.DAILY, LocalDate.of(2026, 3, 15))
        assertFalse(e.occursOn(LocalDate.of(2026, 3, 9), z))
        assertTrue(e.occursOn(LocalDate.of(2026, 3, 10), z))
        assertTrue(e.occursOn(LocalDate.of(2026, 3, 15), z))
        assertFalse(e.occursOn(LocalDate.of(2026, 3, 16), z))
        val o = e.occurrencesOn(LocalDate.of(2026, 3, 12), z).single()
        assertEquals(LocalDateTime.of(2026, 3, 12, 9, 0), o.start)
        assertEquals(LocalDateTime.of(2026, 3, 12, 10, 0), o.end)
    }

    @Test fun weeklyRepeatsOnSameWeekday() {
        val e = ev(LocalDateTime.of(2026, 11, 2, 18, 0), LocalDateTime.of(2026, 11, 2, 19, 0), Recurrence.WEEKLY) // Monday
        assertTrue(e.occursOn(LocalDate.of(2026, 11, 9), z))
        assertTrue(e.occursOn(LocalDate.of(2027, 1, 4), z))
        assertFalse(e.occursOn(LocalDate.of(2026, 11, 10), z))
    }

    @Test fun monthlySkipsMonthsWithoutThatDay() {
        val e = ev(LocalDateTime.of(2026, 1, 31, 8, 0), LocalDateTime.of(2026, 1, 31, 9, 0), Recurrence.MONTHLY)
        assertTrue(e.occursOn(LocalDate.of(2026, 3, 31), z))
        assertFalse(e.occursOn(LocalDate.of(2026, 2, 28), z))
        assertFalse(e.occursOn(LocalDate.of(2026, 4, 30), z))
    }

    @Test fun yearlyBirthday() {
        val e = ev(LocalDateTime.of(2020, 5, 20, 0, 0), LocalDateTime.of(2020, 5, 20, 23, 59), Recurrence.YEARLY, allDay = true)
        assertTrue(e.occursOn(LocalDate.of(2031, 5, 20), z))
        assertFalse(e.occursOn(LocalDate.of(2031, 5, 21), z))
    }

    @Test fun repeatingMultiDayEventCoversFollowingDay() {
        val e = ev(LocalDateTime.of(2026, 6, 5, 22, 0), LocalDateTime.of(2026, 6, 6, 2, 0), Recurrence.WEEKLY)
        assertTrue(e.occursOn(LocalDate.of(2026, 6, 13), z)) // tail of the 12th's occurrence
        assertFalse(e.occursOn(LocalDate.of(2026, 6, 11), z))
    }

    @Test fun nextOccurrenceFindsFutureRepetition() {
        val e = ev(LocalDateTime.of(2026, 1, 1, 9, 0), LocalDateTime.of(2026, 1, 1, 10, 0), Recurrence.WEEKLY)
        val n = e.nextOccurrence(LocalDateTime.of(2026, 1, 9, 12, 0), z)
        assertEquals(LocalDateTime.of(2026, 1, 15, 9, 0), n!!.start)
        val once = ev(LocalDateTime.of(2026, 1, 1, 9, 0), LocalDateTime.of(2026, 1, 1, 10, 0))
        assertNull(once.nextOccurrence(LocalDateTime.of(2026, 1, 2, 0, 0), z))
    }

    @Test fun occurrencesSortedAllDayFirst() {
        val a = ev(LocalDateTime.of(2026, 2, 2, 15, 0), LocalDateTime.of(2026, 2, 2, 16, 0)).copy(id = 1)
        val b = ev(LocalDateTime.of(2026, 2, 2, 9, 0), LocalDateTime.of(2026, 2, 2, 10, 0)).copy(id = 2)
        val c = ev(LocalDateTime.of(2026, 2, 2, 0, 0), LocalDateTime.of(2026, 2, 2, 23, 59), allDay = true).copy(id = 3)
        assertEquals(listOf(3L, 2L, 1L), listOf(a, b, c).occurrencesOn(LocalDate.of(2026, 2, 2), z).map { it.event.id })
    }

    @Test fun upcomingSkipsFinishedEvents() {
        val past = ev(LocalDateTime.of(2026, 2, 2, 8, 0), LocalDateTime.of(2026, 2, 2, 9, 0)).copy(id = 1)
        val ongoing = ev(LocalDateTime.of(2026, 2, 2, 11, 0), LocalDateTime.of(2026, 2, 2, 13, 0)).copy(id = 2)
        val u = listOf(past, ongoing).upcoming(LocalDateTime.of(2026, 2, 2, 12, 0), zone = z)
        assertNotNull(u); assertEquals(2L, u!!.event.id)
    }
}
