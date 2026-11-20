package com.ikeralvaro.calendario.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

object CalendarMath {
    /** 6x7 grid of days for a month view. */
    fun monthGrid(month: YearMonth, firstDay: DayOfWeek = DayOfWeek.MONDAY): List<LocalDate> {
        val first = month.atDay(1).with(TemporalAdjusters.previousOrSame(firstDay))
        return (0 until 42).map { first.plusDays(it.toLong()) }
    }

    fun weekStart(day: LocalDate, firstDay: DayOfWeek = DayOfWeek.MONDAY): LocalDate =
        day.with(TemporalAdjusters.previousOrSame(firstDay))

    fun weekDays(day: LocalDate, firstDay: DayOfWeek = DayOfWeek.MONDAY): List<LocalDate> {
        val s = weekStart(day, firstDay)
        return (0 until 7).map { s.plusDays(it.toLong()) }
    }

    /** Day-of-week headers starting at [firstDay]. */
    fun weekHeader(firstDay: DayOfWeek): List<DayOfWeek> = (0 until 7).map { firstDay.plus(it.toLong()) }

    /**
     * Assigns columns to overlapping timed events so they can be drawn side by side.
     * Returns pairs of (column, totalColumns) in the same order as [ranges] (start,end minutes).
     */
    fun layoutColumns(ranges: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        if (ranges.isEmpty()) return emptyList()
        val order = ranges.indices.sortedWith(compareBy({ ranges[it].first }, { -ranges[it].second }))
        val result = arrayOfNulls<Pair<Int, Int>>(ranges.size)
        var group = mutableListOf<Int>()
        val colEnds = mutableListOf<Int>()
        val colOf = IntArray(ranges.size)
        var groupEnd = Int.MIN_VALUE
        fun flush() {
            val total = colEnds.size
            group.forEach { result[it] = colOf[it] to total }
            group = mutableListOf(); colEnds.clear()
        }
        for (i in order) {
            val (s, e0) = ranges[i]
            val e = maxOf(e0, s + 15)
            if (s >= groupEnd && group.isNotEmpty()) flush()
            var col = colEnds.indexOfFirst { it <= s }
            if (col == -1) { colEnds.add(e); col = colEnds.size - 1 } else colEnds[col] = e
            colOf[i] = col
            group.add(i)
            groupEnd = if (group.size == 1) e else maxOf(groupEnd, e)
        }
        flush()
        return result.map { it!! }
    }
}
