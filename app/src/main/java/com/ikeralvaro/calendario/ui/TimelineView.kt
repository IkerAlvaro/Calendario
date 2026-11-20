package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.ikeralvaro.calendario.data.Occurrence
import com.ikeralvaro.calendario.data.occurrencesOn
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle

private val HourHeight = 60.dp
private val GutterWidth = 48.dp

@Composable
fun TimelineView(
    selected: LocalDate,
    dayCount: Int,
    events: List<Event>,
    onSelectDay: (LocalDate) -> Unit,
    onOpen: (Occurrence) -> Unit,
    onCreate: (LocalDate, Int) -> Unit,
) {
    val firstDay = LocalSettings.current.firstDay
    val days = if (dayCount == 7) CalendarMath.weekDays(selected, firstDay) else listOf(selected)
    val occ = remember(events, days) { days.map { events.occurrencesOn(it) } }
    val today = LocalDate.now()
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    LaunchedEffect(Unit) {
        val h = (LocalTime.now().hour - 2).coerceIn(0, 16)
        scroll.scrollTo(with(density) { (HourHeight * h).roundToPx() })
    }
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(60_000); now = LocalTime.now() } }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            Spacer(Modifier.width(GutterWidth))
            days.forEachIndexed { i, d ->
                val isToday = d == today
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onSelectDay(d) }.padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        d.dayOfWeek.getDisplayName(if (dayCount == 1) TextStyle.FULL else TextStyle.SHORT, Es).cap(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        Modifier.padding(top = 2.dp).size(32.dp).clip(CircleShape)
                            .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            d.dayOfMonth.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color.White else MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    // event count dots under each day (week view)
                    if (dayCount == 7) Row(Modifier.height(6.dp).padding(top = 3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        occ[i].distinctBy { it.event.id }.take(3).forEach {
                            Box(Modifier.size(3.dp).clip(CircleShape).background(Color(it.event.color)))
                        }
                    }
                }
            }
        }
        val allDay = occ.map { list -> list.filter { it.event.allDay } }
        if (allDay.any { it.isNotEmpty() }) {
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 6.dp)) {
                Text("todo\nel día", fontSize = 9.sp, lineHeight = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(GutterWidth).padding(top = 2.dp))
                allDay.forEach { list ->
                    Column(Modifier.weight(1f).padding(horizontal = 1.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        list.forEach { o -> EventChip(o, Modifier.fillMaxWidth().height(22.dp), onOpen, compact = true) }
                    }
                }
            }
        }
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(scroll)
                .padding(top = 12.dp, bottom = 100.dp)
        ) {
            Column(Modifier.width(GutterWidth)) {
                (0 until 24).forEach { h ->
                    Box(Modifier.height(HourHeight).fillMaxWidth()) {
                        if (h > 0) Text(
                            hourLabel(h),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-7).dp),
                        )
                    }
                }
            }
            days.forEachIndexed { i, d ->
                DayColumn(d, dayCount == 1, d == today, now, occ[i].filter { !it.event.allDay }, onOpen, onCreate, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DayColumn(
    day: LocalDate,
    wide: Boolean,
    isToday: Boolean,
    now: LocalTime,
    occ: List<Occurrence>,
    onOpen: (Occurrence) -> Unit,
    onCreate: (LocalDate, Int) -> Unit,
    modifier: Modifier,
) {
    val dayStart = day.atStartOfDay()
    val ranges = occ.map { o ->
        val s = Duration.between(dayStart, o.start).toMinutes().coerceIn(0, 24 * 60).toInt()
        val e = Duration.between(dayStart, o.end).toMinutes().coerceIn(0, 24 * 60).toInt()
        s to e
    }
    val cols = CalendarMath.layoutColumns(ranges)
    val outline = MaterialTheme.colorScheme.outlineVariant
    val density = LocalDensity.current
    val primary = MaterialTheme.colorScheme.primary
    BoxWithConstraints(
        modifier
            .height(HourHeight * 24)
            .background(if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f) else Color.Transparent)
            .pointerInput(day) {
                fun hourAt(y: Float) = (y / with(density) { HourHeight.toPx() }).toInt().coerceIn(0, 23)
                detectTapGestures(
                    onTap = { onCreate(day, hourAt(it.y)) },
                    onLongPress = { onCreate(day, hourAt(it.y)) },
                )
            }
    ) {
        Column {
            repeat(24) {
                Box(Modifier.height(HourHeight).fillMaxWidth()) { HorizontalDivider(color = outline, thickness = 1.dp) }
            }
        }
        val w = maxWidth
        occ.forEachIndexed { i, o ->
            val (s, e) = ranges[i]
            val (col, total) = cols[i]
            // Day view: side by side. Week view (narrow): cascade so every title keeps most of the width.
            val x = if (wide || total == 1) w * (col.toFloat() / total)
                    else w * (col * minOf(0.3f, 0.6f / (total - 1)))
            val width = if (wide || total == 1) w / total else w - x
            EventChip(
                o,
                Modifier
                    .offset(x = x, y = HourHeight * (s / 60f))
                    .width(width)
                    .height(HourHeight * (maxOf(e - s, 22) / 60f))
                    .padding(1.dp),
                onOpen,
                showTime = wide,
                outlined = !wide && total > 1,
            )
        }
        if (isToday) {
            val y = HourHeight * ((now.hour * 60 + now.minute) / 60f)
            Box(Modifier.offset(y = y - 1.dp).fillMaxWidth().height(2.dp).background(primary))
            Box(Modifier.offset(x = (-4).dp, y = y - 5.dp).size(10.dp).clip(CircleShape).background(primary))
        }
    }
}

@Composable
fun EventChip(
    o: Occurrence, modifier: Modifier, onOpen: (Occurrence) -> Unit,
    compact: Boolean = false, showTime: Boolean = true, outlined: Boolean = false,
) {
    val c = Color(o.event.color)
    Column(
        modifier
            .then(if (outlined) Modifier.border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)) else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .background(c)
            .clickable { onOpen(o) }
            .padding(horizontal = if (showTime) 8.dp else 3.dp, vertical = if (compact) 2.dp else 4.dp)
    ) {
        Text(
            o.event.title.ifBlank { "(Sin título)" },
            fontSize = if (showTime) 13.sp else 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = if (compact) 1 else 4,
            softWrap = !compact,
            overflow = TextOverflow.Ellipsis,
            lineHeight = if (showTime) 16.sp else 12.sp,
        )
        if (!compact && showTime && !o.event.allDay) Text(
            "${o.start.hm()} – ${o.end.hm()}" + if (o.event.location.isNotBlank()) " · ${o.event.location}" else "",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
