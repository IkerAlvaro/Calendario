package com.ikeralvaro.calendario.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.Occurrence
import java.text.Normalizer
import java.time.LocalDateTime

private fun String.norm() = Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

@Composable
fun SearchScreen(events: List<Event>, onBack: () -> Unit, onOpen: (Occurrence) -> Unit, initialQuery: String = "") {
    var query by remember { mutableStateOf(initialQuery) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    BackHandler(onBack = onBack)
    val now = LocalDateTime.now()
    val results = remember(events, query) {
        val q = query.trim().norm()
        if (q.isEmpty()) emptyList()
        else events.filter { e -> listOf(e.title, e.location, e.description).any { it.norm().contains(q) } }
            .map { e -> e.nextOccurrence(now.minusHours(1)) ?: Occurrence(e, e.startDateTime(), e.endDateTime()) }
            .sortedWith(compareBy<Occurrence> { it.start.isBefore(now) }.thenBy { if (it.start.isBefore(now)) -it.start.toLocalDate().toEpochDay() else it.start.toLocalDate().toEpochDay() })
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") }
                Row(
                    Modifier.weight(1f).height(48.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f)) {
                        BasicField(query, { query = it }, "Buscar eventos",
                            MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.focusRequester(focus))
                    }
                    if (query.isNotEmpty()) IconButton(onClick = { query = "" }, Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, "Borrar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
            when {
                query.isBlank() -> EmptyState("Busca en tu calendario", "Por título, lugar o notas", Modifier.padding(top = 40.dp))
                results.isEmpty() -> EmptyState("Sin resultados", "No hay eventos con «${query.trim()}»", Modifier.padding(top = 40.dp))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(results, key = { _, it -> it.event.id }) { i, o ->
                        val showDate = i == 0 || results[i - 1].start.toLocalDate() != o.start.toLocalDate()
                        Column(Modifier.animateItem()) {
                            if (showDate) Text(relativeDay(o.start.toLocalDate()), style = MaterialTheme.typography.labelMedium,
                                color = if (o.start.isBefore(now)) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 4.dp))
                            EventCard(o, onOpen)
                        }
                    }
                }
            }
        }
    }
}
