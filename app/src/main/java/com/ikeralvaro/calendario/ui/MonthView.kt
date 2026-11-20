package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.CalendarMath
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.ui.theme.Accent
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
    Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            DayOfWeek.entries.forEach {
                Text(
                    it.getDisplayName(TextStyle.NARROW, Es),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        grid.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    val dayEvents = events.filter { it.occursOn(day) }
                    val inMonth = day.month == month.month
                    val isSel = day == selected
                    val isToday = day == today
                    Column(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .clickable { if (isSel) onOpenDay(day) else onSelect(day) }
                            .padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSel -> MaterialTheme.colorScheme.primary
                                        isToday -> Accent.copy(alpha = 0.12f)
                                        else -> Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isToday || isSel) FontWeight.SemiBold else FontWeight.Normal,
                                color = when {
                                    isSel -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> Accent
                                    inMonth -> MaterialTheme.colorScheme.onBackground
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                            )
                        }
                        Row(Modifier.padding(top = 3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            dayEvents.take(3).forEach {
                                Box(Modifier.size(5.dp).clip(CircleShape).background(Color(it.color)))
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider(Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outline)
        val dayEvents = events.filter { it.occursOn(selected) }
        Text(
            selected.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Es)).cap(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp),
        )
        if (dayEvents.isEmpty()) {
            Text(
                "Sin eventos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp),
            )
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
            items(dayEvents, key = { it.id }) { EventRow(it, onEdit) }
        }
    }
}

@Composable
fun EventRow(event: Event, onEdit: (Event) -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .clickable { onEdit(event) }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(4.dp).height(36.dp).clip(CircleShape).background(Color(event.color)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(event.title.ifBlank { "(Sin título)" }, style = MaterialTheme.typography.titleMedium)
            val sub = buildString {
                append(if (event.allDay) "Todo el día" else "${event.startDateTime().format(fmt)} – ${event.endDateTime().format(fmt)}")
                if (event.location.isNotBlank()) append(" · ${event.location}")
            }
            Text(sub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
