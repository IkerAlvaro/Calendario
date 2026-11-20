package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikeralvaro.calendario.data.CalendarMath
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.ui.theme.Accent
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle

private val HourHeight = 56.dp
private val GutterWidth = 44.dp

@Composable
fun TimelineView(
    selected: LocalDate,
    dayCount: Int,
    events: List<Event>,
    onSelectDay: (LocalDate) -> Unit,
    onEdit: (Event) -> Unit,
    onCreate: (LocalDate, Int) -> Unit,
) {
    val days = if (dayCount == 7) CalendarMath.weekDays(selected) else listOf(selected)
    val today = LocalDate.now()
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    LaunchedEffect(Unit) {
        scroll.scrollTo(with(density) { (HourHeight * 7).roundToPx() })
    }
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(60_000); now = LocalTime.now() } }

    Column(Modifier.fillMaxSize()) {
        // Day headers
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Spacer(Modifier.width(GutterWidth))
            days.forEach { d ->
                val isToday = d == today
                Column(
                    Modifier.weight(1f).clickable { onSelectDay(d) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        d.dayOfWeek.getDisplayName(TextStyle.SHORT, Es).cap(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        Modifier.size(30.dp).clip(CircleShape)
                            .background(if (isToday) Accent else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            d.dayOfMonth.toString(),
                            fontWeight = FontWeight.SemiBold,
                            color = if (isToday) Color.White else MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
        // All-day strip
        val allDay = days.map { d -> events.filter { it.allDay && it.occursOn(d) } }
        if (allDay.any { it.isNotEmpty() }) {
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Spacer(Modifier.width(GutterWidth))
                allDay.forEach { list ->
                    Column(Modifier.weight(1f).padding(horizontal = 1.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        list.forEach { e -> EventChip(e, Modifier.fillMaxWidth().height(20.dp), onEdit, compact = true) }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        Row(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Column(Modifier.width(GutterWidth)) {
                (0 until 24).forEach { h ->
                    Box(Modifier.height(HourHeight).fillMaxWidth()) {
                        if (h > 0) Text(
                            "%02d:00".format(h),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-7).dp),
                        )
                    }
                }
            }
            days.forEach { d ->
                DayColumn(d, d == today, now, events.filter { !it.allDay && it.occursOn(d) }, onEdit, onCreate, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DayColumn(
    day: LocalDate,
    isToday: Boolean,
    now: LocalTime,
    events: List<Event>,
    onEdit: (Event) -> Unit,
    onCreate: (LocalDate, Int) -> Unit,
    modifier: Modifier,
) {
    val dayStart = day.atStartOfDay()
    val ranges = events.map { e ->
        val s = maxOf(java.time.Duration.between(dayStart, e.startDateTime()).toMinutes(), 0).toInt()
        val en = minOf(java.time.Duration.between(dayStart, e.endDateTime()).toMinutes(), 24 * 60).toInt()
        s to en
    }
    val cols = CalendarMath.layoutColumns(ranges)
    val outline = MaterialTheme.colorScheme.outlineVariant
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier
            .height(HourHeight * 24)
            .pointerInput(day) {
                detectTapGestures { off ->
                    val hour = (off.y / with(density) { HourHeight.toPx() }).toInt().coerceIn(0, 23)
                    onCreate(day, hour)
                }
            }
    ) {
        Column {
            repeat(24) {
                Box(Modifier.height(HourHeight).fillMaxWidth()) {
                    HorizontalDivider(color = outline)
                }
            }
        }
        val w = maxWidth
        events.forEachIndexed { i, e ->
            val (s, en) = ranges[i]
            val (col, total) = cols[i]
            val top = HourHeight * (s / 60f)
            val height = (HourHeight * ((maxOf(en - s, 20)) / 60f))
            EventChip(
                e,
                Modifier
                    .offset(x = w * (col.toFloat() / total), y = top)
                    .width(w / total)
                    .height(height)
                    .padding(1.dp),
                onEdit,
            )
        }
        if (isToday) {
            val y = HourHeight * ((now.hour * 60 + now.minute) / 60f)
            Box(Modifier.offset(y = y - 1.dp).fillMaxWidth().height(2.dp).background(Accent))
            Box(Modifier.offset(x = (-4).dp, y = y - 5.dp).size(10.dp).clip(CircleShape).background(Accent))
        }
    }
}

@Composable
fun EventChip(event: Event, modifier: Modifier, onEdit: (Event) -> Unit, compact: Boolean = false) {
    val c = Color(event.color)
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(c.copy(alpha = 0.16f))
            .clickable { onEdit(event) }
    ) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(c))
        Column(Modifier.padding(start = 6.dp, end = 2.dp, top = if (compact) 1.dp else 3.dp)) {
            Text(
                event.title.ifBlank { "(Sin título)" },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = if (compact) 1 else 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
