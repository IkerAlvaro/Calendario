package com.ikeralvaro.calendario.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.CalendarMath
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.Occurrence
import com.ikeralvaro.calendario.data.occurrencesOn
import com.ikeralvaro.calendario.data.upcoming
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun MonthView(
    selected: LocalDate,
    events: List<Event>,
    onSelect: (LocalDate) -> Unit,
    onOpenDay: (LocalDate) -> Unit,
    onOpen: (Occurrence) -> Unit,
    onDelete: (Event) -> Unit,
) {
    val firstDay = LocalSettings.current.firstDay
    val month = YearMonth.from(selected)
    val grid = remember(month, firstDay) { CalendarMath.monthGrid(month, firstDay) }
    val byDay = remember(events, grid) { grid.associateWith { events.occurrencesOn(it) } }
    val today = LocalDate.now()
    val dayEvents = remember(events, selected) { events.occurrencesOn(selected) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(30_000); now = LocalDateTime.now() } }
    val next = remember(events, now.withSecond(0).withNano(0)) { events.upcoming(now) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (next != null) item(key = "next") {
            NextEventCard(next, now, Modifier.animateItem()) { onOpen(next) }
        }
        item(key = "grid") {
            Column(
                Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    CalendarMath.weekHeader(firstDay).forEach {
                        val weekend = it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY
                        Text(
                            it.getDisplayName(TextStyle.SHORT, Es).take(2).uppercase(Es),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (weekend) MaterialTheme.colorScheme.primary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                grid.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            DayCell(day, month, day == selected, day == today, byDay[day].orEmpty(), Modifier.weight(1f)) {
                                if (day == selected) onOpenDay(day) else onSelect(day)
                            }
                        }
                    }
                }
            }
        }
        item(key = "dayTitle") {
            Row(Modifier.padding(start = 4.dp, top = 10.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    when (selected) {
                        today, today.plusDays(1), today.minusDays(1) -> relativeDay(selected, today)
                        else -> selected.format(DateTimeFormatter.ofPattern("EEEE d", Es)).cap()
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.width(8.dp))
                if (dayEvents.isNotEmpty()) Text(
                    "${dayEvents.size} evento${if (dayEvents.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
        if (dayEvents.isEmpty()) item(key = "empty") {
            EmptyState("Día libre", "Pulsa «Nuevo» para añadir un evento", Modifier.animateItem())
        }
        items(dayEvents, key = { "${it.event.id}-${it.start}" }) {
            SwipeableEventCard(it, onOpen, onDelete, Modifier.animateItem())
        }
    }
}

@Composable
private fun NextEventCard(o: Occurrence, now: LocalDateTime, modifier: Modifier, onClick: () -> Unit) {
    val c = Color(o.event.color)
    val dark = lerp(c, Color.Black, 0.25f)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(24.dp), ambientColor = c, spotColor = c)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(c, dark)))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PRÓXIMO", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f))
            Box(
                Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.22f)).padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(countdown(now, o.start, o.end), style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(o.event.title.ifBlank { "(Sin título)" }, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Schedule, null, Modifier.size(15.dp), tint = Color.White.copy(alpha = 0.85f))
            Text("${relativeDay(o.start.toLocalDate())} · ${o.start.hm()} – ${o.end.hm()}",
                style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.padding(start = 5.dp))
            if (o.event.location.isNotBlank()) {
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Rounded.LocationOn, null, Modifier.size(15.dp), tint = Color.White.copy(alpha = 0.85f))
                Text(o.event.location, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 3.dp))
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate, month: YearMonth, isSel: Boolean, isToday: Boolean,
    occ: List<Occurrence>, modifier: Modifier, onClick: () -> Unit,
) {
    val inMonth = day.month == month.month
    val bg by animateColorAsState(
        when {
            isToday -> MaterialTheme.colorScheme.primary
            isSel -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        }, label = "bg",
    )
    Column(
        modifier.height(54.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(bg)
                .then(if (isSel && isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isToday || isSel) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isToday -> MaterialTheme.colorScheme.onPrimary
                    isSel -> MaterialTheme.colorScheme.primary
                    inMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
            )
        }
        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            occ.distinctBy { it.event.id }.take(3).forEach {
                Box(Modifier.width(7.dp).height(3.dp).clip(CircleShape)
                    .background(Color(it.event.color).copy(alpha = if (inMonth) 1f else 0.4f)))
            }
        }
    }
}
