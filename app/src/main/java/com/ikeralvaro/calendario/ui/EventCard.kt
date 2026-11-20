package com.ikeralvaro.calendario.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.Occurrence
import com.ikeralvaro.calendario.data.Recurrence

@Composable
fun EventCard(o: Occurrence, onOpen: (Occurrence) -> Unit, modifier: Modifier = Modifier) {
    val event = o.event
    val c = Color(event.color)
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onOpen(o) }
            .height(IntrinsicSize.Min),
    ) {
        Box(Modifier.width(6.dp).fillMaxHeight().background(c))
        Column(
            Modifier.width(72.dp).fillMaxHeight().background(c.copy(alpha = 0.10f)).padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (event.allDay) {
                Text("TODO", style = MaterialTheme.typography.labelSmall, color = c)
                Text("EL DÍA", style = MaterialTheme.typography.labelSmall, color = c)
            } else {
                Text(o.start.hm(), style = MaterialTheme.typography.labelLarge, color = c, maxLines = 1)
                Text(o.end.hm(), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(event.title.ifBlank { "(Sin título)" }, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val hasMeta = event.location.isNotBlank() || event.reminderMinutes >= 0 || event.rule != Recurrence.NONE
            if (hasMeta) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val tint = MaterialTheme.colorScheme.onSurfaceVariant
                    if (event.location.isNotBlank()) {
                        Row(Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocationOn, null, Modifier.size(14.dp), tint = tint)
                            Text(event.location, style = MaterialTheme.typography.bodyMedium, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, color = tint, modifier = Modifier.padding(start = 2.dp))
                        }
                    }
                    if (event.rule != Recurrence.NONE) Icon(Icons.Rounded.Repeat, "Se repite", Modifier.size(14.dp), tint = tint)
                    if (event.reminderMinutes >= 0) Icon(Icons.Rounded.NotificationsActive, "Con aviso", Modifier.size(14.dp), tint = tint)
                }
            }
        }
    }
}

/** [EventCard] that can be swiped right-to-left to delete. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableEventCard(o: Occurrence, onOpen: (Occurrence) -> Unit, onDelete: (Event) -> Unit, modifier: Modifier = Modifier) {
    val state = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.4f })
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete(o.event)
            state.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val active = state.targetValue == SwipeToDismissBoxValue.EndToStart
            val scale by animateFloatAsState(if (active) 1.15f else 0.85f, label = "trash")
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (active) 1f else 0.75f))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Rounded.DeleteOutline, "Eliminar", tint = Color.White, modifier = Modifier.scale(scale))
            }
        },
    ) { EventCard(o, onOpen) }
}

@Composable
fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(68.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.EventAvailable, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) }
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
