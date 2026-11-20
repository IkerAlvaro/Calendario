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
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.CalendarMath
import com.ikeralvaro.calendario.data.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun MonthView(
    selected: LocalDate,
    events: List<Event>,
    onSelect: (LocalDate) -> Unit,
    onOpenDay: (LocalDate) -> Unit,
    onEdit: (Event) -> Unit,
) {
    val month = YearMonth.from(selected)
    val grid = remember(month) { CalendarMath.monthGrid(month) }
    val today = LocalDate.now()
    val dayEvents = events.filter { it.occursOn(selected) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    DayOfWeek.entries.forEach {
                        val weekend = it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY
                        Text(
                            it.getDisplayName(TextStyle.SHORT, Es).take(2).uppercase(Es),
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (weekend) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                grid.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            DayCell(day, month, day == selected, day == today,
                                events.filter { it.occursOn(day) }, Modifier.weight(1f)) {
                                if (day == selected) onOpenDay(day) else onSelect(day)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.padding(start = 4.dp, top = 10.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    when (selected) {
                        today -> "Hoy"
                        today.plusDays(1) -> "Mañana"
                        today.minusDays(1) -> "Ayer"
                        else -> selected.format(DateTimeFormatter.ofPattern("EEEE d", Es)).cap()
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (dayEvents.isEmpty()) "" else "${dayEvents.size} evento${if (dayEvents.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
        if (dayEvents.isEmpty()) item { EmptyDay() }
        items(dayEvents, key = { it.id }) { EventCard(it, onEdit) }
    }
}

@Composable
private fun DayCell(
    day: LocalDate, month: YearMonth, isSel: Boolean, isToday: Boolean,
    events: List<Event>, modifier: Modifier, onClick: () -> Unit,
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
            events.take(3).forEach {
                Box(Modifier.width(7.dp).height(3.dp).clip(CircleShape).background(Color(it.color).copy(alpha = if (inMonth) 1f else 0.4f)))
            }
        }
    }
}

@Composable
fun EmptyDay() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.EventAvailable, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp)) }
        Spacer(Modifier.height(12.dp))
        Text("Día libre", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("Pulsa «Nuevo» para añadir un evento", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EventCard(event: Event, onEdit: (Event) -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    val c = Color(event.color)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onEdit(event) }
            .height(IntrinsicSize.Min),
    ) {
        Box(Modifier.width(6.dp).fillMaxHeight().background(c))
        Column(
            Modifier.width(64.dp).fillMaxHeight().background(c.copy(alpha = 0.10f)).padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (event.allDay) {
                Text("TODO", style = MaterialTheme.typography.labelSmall, color = c)
                Text("EL DÍA", style = MaterialTheme.typography.labelSmall, color = c)
            } else {
                Text(event.startDateTime().format(fmt), style = MaterialTheme.typography.labelLarge, color = c)
                Text(event.endDateTime().format(fmt), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(event.title.ifBlank { "(Sin título)" }, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (event.location.isNotBlank() || event.reminderMinutes >= 0) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (event.location.isNotBlank()) {
                        Icon(Icons.Rounded.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(event.location, style = MaterialTheme.typography.bodyMedium, maxLines = 1,
                            overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f, fill = false).padding(start = 2.dp, end = 8.dp))
                    }
                    if (event.reminderMinutes >= 0) {
                        Icon(Icons.Rounded.NotificationsActive, "Con aviso", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
