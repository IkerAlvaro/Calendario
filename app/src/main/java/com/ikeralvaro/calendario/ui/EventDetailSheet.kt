package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.data.Occurrence
import com.ikeralvaro.calendario.data.Recurrence
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(o: Occurrence, onDismiss: () -> Unit, onEdit: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        EventDetailContent(o, onEdit, onDuplicate, onDelete)
    }
}

@Composable
fun EventDetailContent(o: Occurrence, onEdit: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    val e = o.event
    val c = Color(e.color)
    val dateFmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Es)
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.padding(top = 8.dp).size(16.dp).clip(RoundedCornerShape(5.dp)).background(c))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(e.title.ifBlank { "(Sin título)" }, style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                val sameDay = o.start.toLocalDate() == o.end.toLocalDate()
                Text(
                    when {
                        e.allDay && sameDay -> o.start.format(dateFmt).cap()
                        e.allDay -> "${o.start.format(dateFmt).cap()} – ${o.end.format(dateFmt)}"
                        else -> o.start.format(dateFmt).cap()
                    },
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!e.allDay) Text(
                    if (sameDay) "${o.start.hm()} – ${o.end.hm()}" else "${o.start.hm()} – ${relativeDay(o.end.toLocalDate(), LocalDate.now())} ${o.end.hm()}",
                    style = MaterialTheme.typography.titleMedium, color = c,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        if (e.rule != Recurrence.NONE) DetailRow(Icons.Rounded.Repeat,
            e.rule.label + (e.recurrenceEnd?.let { " hasta el " + LocalDate.ofEpochDay(it).format(DateTimeFormatter.ofPattern("d MMM yyyy", Es)) } ?: ""))
        DetailRow(Icons.Rounded.NotificationsNone, reminderLabel(e.reminderMinutes))
        if (e.location.isNotBlank()) DetailRow(Icons.Rounded.LocationOn, e.location)
        if (e.description.isNotBlank()) DetailRow(Icons.AutoMirrored.Rounded.Notes, e.description)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onEdit, modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
            ) { Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Editar", fontWeight = FontWeight.Bold) }
            FilledTonalButton(onClick = onDuplicate, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Rounded.ContentCopy, "Duplicar", Modifier.size(18.dp))
            }
            FilledTonalButton(
                onClick = onDelete, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
            ) { Icon(Icons.Rounded.DeleteOutline, "Eliminar", Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
