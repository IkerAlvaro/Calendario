package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.EventColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val reminderOptions = listOf(-1 to "Ninguno", 0 to "Al empezar", 5 to "5 min",
    10 to "10 min", 30 to "30 min", 60 to "1 hora", 1440 to "1 día")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditor(initial: Event, onDismiss: () -> Unit, onSave: (Event) -> Unit, onDelete: (Event) -> Unit) {
    val zone = ZoneId.systemDefault()
    var title by remember { mutableStateOf(initial.title) }
    var description by remember { mutableStateOf(initial.description) }
    var location by remember { mutableStateOf(initial.location) }
    var allDay by remember { mutableStateOf(initial.allDay) }
    var start by remember { mutableStateOf(initial.startDateTime(zone)) }
    var end by remember { mutableStateOf(initial.endDateTime(zone)) }
    var color by remember { mutableIntStateOf(initial.color.takeIf { it != 0 } ?: EventColors.palette.first()) }
    var reminder by remember { mutableIntStateOf(initial.reminderMinutes) }
    var picker by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Es)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    val valid = !end.isBefore(start)

    val accent by androidx.compose.animation.animateColorAsState(Color(color), label = "accent")
    androidx.activity.compose.BackHandler(onBack = onDismiss)
    run {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().imePadding()) {
                // Cabecera con el color del evento
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(accent, accent.copy(alpha = 0.85f))))
                        .statusBarsPadding()
                        .padding(start = 8.dp, end = 12.dp, bottom = 20.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "Cerrar", tint = Color.White) }
                        Text(if (initial.id == 0L) "Nuevo evento" else "Editar evento",
                            style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
                        if (initial.id != 0L) {
                            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Rounded.DeleteOutline, "Eliminar", tint = Color.White) }
                        }
                        Button(
                            enabled = valid,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = accent),
                            onClick = {
                                val s = if (allDay) start.toLocalDate().atStartOfDay() else start
                                val e = if (allDay) end.toLocalDate().atTime(23, 59) else end
                                onSave(
                                    initial.copy(
                                        title = title.trim(), description = description.trim(), location = location.trim(),
                                        allDay = allDay, color = color, reminderMinutes = reminder,
                                        start = s.atZone(zone).toInstant().toEpochMilli(),
                                        end = e.atZone(zone).toInstant().toEpochMilli(),
                                    )
                                )
                            },
                        ) { Text("Guardar", fontWeight = FontWeight.Bold) }
                    }
                    Box(Modifier.padding(horizontal = 12.dp)) {
                        BasicField(title, { title = it }, "Añade un título",
                            TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            hintColor = Color.White.copy(alpha = 0.6f), cursor = Color.White)
                    }
                }
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Section {
                        IconRow(Icons.Rounded.Schedule) {
                            Text("Todo el día", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Switch(checked = allDay, onCheckedChange = { allDay = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = accent))
                        }
                        DateTimeRow("Empieza", start.format(dateFmt), if (allDay) null else start.format(timeFmt),
                            { picker = "startDate" }, { picker = "startTime" })
                        DateTimeRow("Termina", end.format(dateFmt), if (allDay) null else end.format(timeFmt),
                            { picker = "endDate" }, { picker = "endTime" })
                        if (!valid) Text("El final debe ser posterior al inicio", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 40.dp))
                    }
                    Section {
                        IconRow(Icons.Rounded.Palette) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                EventColors.palette.forEach { c ->
                                    Box(
                                        Modifier.size(32.dp).clip(CircleShape).background(Color(c)).clickable { color = c },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (c == color) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    Section {
                        IconRow(Icons.Rounded.NotificationsNone) {
                            Text("Aviso", style = MaterialTheme.typography.bodyLarge)
                        }
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()).padding(start = 40.dp, top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            reminderOptions.forEach { (m, label) ->
                                val sel = m == reminder
                                Box(
                                    Modifier.clip(CircleShape)
                                        .background(if (sel) accent else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { reminder = m }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(label, style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    Section {
                        IconRow(Icons.Rounded.LocationOn) {
                            BasicField(location, { location = it }, "Añadir ubicación", MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface))
                        }
                        HorizontalDivider(Modifier.padding(start = 40.dp, top = 6.dp, bottom = 6.dp), color = MaterialTheme.colorScheme.outline)
                        IconRow(Icons.AutoMirrored.Rounded.Notes) {
                            BasicField(description, { description = it }, "Añadir descripción", MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), singleLine = false)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    when (picker) {
        "startDate", "endDate" -> {
            val isStart = picker == "startDate"
            val current = if (isStart) start.toLocalDate() else end.toLocalDate()
            val state = rememberDatePickerState(
                initialSelectedDateMillis = current.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let {
                            val d = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                            if (isStart) {
                                val dur = java.time.Duration.between(start, end)
                                start = d.atTime(start.toLocalTime()); end = start.plus(dur)
                            } else end = d.atTime(end.toLocalTime())
                        }
                        picker = null
                    }) { Text("Aceptar") }
                },
                dismissButton = { TextButton(onClick = { picker = null }) { Text("Cancelar") } },
            ) { DatePicker(state) }
        }
        "startTime", "endTime" -> {
            val isStart = picker == "startTime"
            val t = if (isStart) start.toLocalTime() else end.toLocalTime()
            val state = rememberTimePickerState(t.hour, t.minute, is24Hour = true)
            AlertDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(onClick = {
                        val nt = LocalTime.of(state.hour, state.minute)
                        if (isStart) {
                            val dur = java.time.Duration.between(start, end)
                            start = start.toLocalDate().atTime(nt); end = start.plus(dur)
                        } else end = end.toLocalDate().atTime(nt)
                        picker = null
                    }) { Text("Aceptar") }
                },
                dismissButton = { TextButton(onClick = { picker = null }) { Text("Cancelar") } },
                text = { TimePicker(state) },
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("¿Eliminar evento?") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete(initial) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Section(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp),
        content = content,
    )
}

@Composable
private fun IconRow(icon: ImageVector, content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(18.dp))
        content()
    }
}

@Composable
private fun DateTimeRow(label: String, date: String, time: String?, onDate: () -> Unit, onTime: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 40.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable(onClick = onDate).padding(vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(date.cap(), style = MaterialTheme.typography.titleMedium)
        }
        if (time != null) Text(time, Modifier.clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onTime)
            .padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BasicField(
    value: String, onChange: (String) -> Unit, hint: String, style: TextStyle, singleLine: Boolean = true,
    hintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant, cursor: Color = MaterialTheme.colorScheme.primary,
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = style,
        singleLine = singleLine,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(cursor),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(hint, style = style.copy(color = hintColor))
            inner()
        },
    )
}
