package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.Event
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

@Composable
fun AgendaView(selected: LocalDate, events: List<Event>, onEdit: (Event) -> Unit) {
    val month = YearMonth.from(selected)
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
        .map { d -> d to events.filter { it.occursOn(d) } }
        .filter { it.second.isNotEmpty() }
    if (days.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) { EmptyDay() }
        return
    }
    val today = LocalDate.now()
    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp)) {
        days.forEach { (d, list) ->
            item(key = d.toString()) {
                val hi = d == today
                Row(Modifier.padding(top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).background(
                            if (hi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
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
            items(list, key = { "${d}-${it.id}" }) {
                Box(Modifier.padding(bottom = 8.dp)) { EventCard(it, onEdit) }
            }
        }
    }
}
