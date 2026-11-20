package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.Occurrence
import com.ikeralvaro.calendario.data.occurrencesOn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

@Composable
fun AgendaView(selected: LocalDate, events: List<Event>, onOpen: (Occurrence) -> Unit, onDelete: (Event) -> Unit) {
    val month = YearMonth.from(selected)
    val days = remember(events, month) {
        (1..month.lengthOfMonth()).map { month.atDay(it) }
            .map { d -> d to events.occurrencesOn(d) }
            .filter { it.second.isNotEmpty() }
    }
    if (days.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) {
            EmptyState("Mes despejado", "No hay eventos en ${month.month.getDisplayName(TextStyle.FULL, Es)}")
        }
        return
    }
    val today = LocalDate.now()
    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp)) {
        days.forEach { (d, list) ->
            item(key = d.toString()) {
                val hi = d == today
                Row(Modifier.animateItem().padding(top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier.size(48.dp).background(
                            if (hi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(14.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(d.dayOfWeek.getDisplayName(TextStyle.SHORT, Es).uppercase(Es).take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hi) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(d.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge,
                            color = if (hi) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(when (d) { today -> "Hoy"; today.plusDays(1) -> "Mañana"
                            else -> d.dayOfWeek.getDisplayName(TextStyle.FULL, Es).cap() },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (hi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
                        Text("${list.size} evento${if (list.size > 1) "s" else ""}", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(list, key = { "$d-${it.event.id}-${it.start}" }) {
                SwipeableEventCard(it, onOpen, onDelete, Modifier.animateItem().padding(bottom = 8.dp))
            }
        }
    }
}
