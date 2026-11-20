package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.ui.theme.Accent
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun AgendaView(selected: LocalDate, events: List<Event>, onEdit: (Event) -> Unit) {
    val month = YearMonth.from(selected)
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
        .map { d -> d to events.filter { it.occursOn(d) } }
        .filter { it.second.isNotEmpty() }
    if (days.isEmpty()) {
        Text(
            "No hay eventos este mes",
            Modifier.padding(24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val today = LocalDate.now()
    LazyColumn(contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 88.dp)) {
        days.forEach { (d, list) ->
            item(key = d.toString()) {
                Text(
                    d.format(DateTimeFormatter.ofPattern("EEEE d", Es)).cap(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (d == today) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 2.dp),
                )
            }
            items(list, key = { "${d}-${it.id}" }) { EventRow(it, onEdit) }
        }
    }
}
