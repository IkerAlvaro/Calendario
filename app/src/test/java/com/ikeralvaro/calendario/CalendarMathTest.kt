package com.ikeralvaro.calendario

import com.ikeralvaro.calendario.data.CalendarMath
import com.ikeralvaro.calendario.data.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

class CalendarMathTest {
    @Test fun monthGridStartsOnMondayAndHas42Days() {
        val g = CalendarMath.monthGrid(YearMonth.of(2026, 11))
        assertEquals(42, g.size)
        assertEquals(DayOfWeek.MONDAY, g.first().dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 26), g.first())
        assertTrue(g.contains(LocalDate.of(2026, 11, 30)))
    }

    @Test fun overlappingEventsGetSeparateColumns() {
        val r = CalendarMath.layoutColumns(listOf(60 to 120, 90 to 150, 200 to 260))
        assertEquals(0 to 2, r[0]); assertEquals(1 to 2, r[1]); assertEquals(0 to 1, r[2])
    }

    @Test fun multiDayEventOccursOnEachDay() {
        val z = ZoneOffset.UTC
        fun ms(dt: LocalDateTime) = dt.toInstant(z).toEpochMilli()
        val e = Event(title = "x", start = ms(LocalDateTime.of(2026, 1, 1, 22, 0)), end = ms(LocalDateTime.of(2026, 1, 2, 2, 0)))
        assertTrue(e.occursOn(LocalDate.of(2026, 1, 1), z))
        assertTrue(e.occursOn(LocalDate.of(2026, 1, 2), z))
        assertFalse(e.occursOn(LocalDate.of(2026, 1, 3), z))
    }
}
