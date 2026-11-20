package com.ikeralvaro.calendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
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

private val reminderOptions = listOf(-1 to "Sin aviso", 0 to "Al empezar", 5 to "5 min antes",
    10 to "10 min antes", 30 to "30 min antes", 60 to "1 hora antes", 1440 to "1 día antes")

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
    var reminderMenu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Es)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    val valid = !end.isBefore(start)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Cerrar") }
                    Spacer(Modifier.weight(1f))
                    if (initial.id != 0L) {
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, "Eliminar") }
                    }
                    Button(
                        enabled = valid,
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
                    ) { Text("Guardar") }
                }
                Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                    BasicField(title, { title = it }, "Añade un título", TextStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground))
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Todo el día", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Switch(checked = allDay, onCheckedChange = { allDay = it })
                    }
                    DateTimeRow("Empieza", start.format(dateFmt), if (allDay) null else start.format(timeFmt),
                        { picker = "startDate" }, { picker = "startTime" })
                    DateTimeRow("Termina", end.format(dateFmt), if (allDay) null else end.format(timeFmt),
                        { picker = "endDate" }, { picker = "endTime" })
                    if (!valid) Text("El final debe ser posterior al inicio", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)

                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)
                    Text("Color", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EventColors.palette.forEach { c ->
                            Box(
                                Modifier.size(30.dp).clip(CircleShape).background(Color(c))
                                    .then(if (c == color) Modifier.border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                        .border(5.dp, Color(c), CircleShape) else Modifier)
                                    .clickable { color = c }
                            )
                        }
                    }
                    Box {
                        Row(Modifier.fillMaxWidth().clickable { reminderMenu = true }.padding(vertical = 12.dp)) {
                            Text("Aviso", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Text(reminderOptions.firstOrNull { it.first == reminder }?.second ?: "$reminder min antes",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = reminderMenu, onDismissRequest = { reminderMenu = false }) {
                            reminderOptions.forEach { (m, label) ->
                                DropdownMenuItem(text = { Text(label) }, onClick = { reminder = m; reminderMenu = false })
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)
                    BasicField(location, { location = it }, "Ubicación", MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground))
                    Spacer(Modifier.height(16.dp))
                    BasicField(description, { description = it }, "Descripción", MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground), singleLine = false)
                    Spacer(Modifier.height(40.dp))
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
private fun DateTimeRow(label: String, date: String, time: String?, onDate: () -> Unit, onTime: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(76.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(date.cap(), Modifier.weight(1f).clickable(onClick = onDate).padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium)
        if (time != null) Text(time, Modifier.clickable(onClick = onTime).padding(8.dp),
            style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BasicField(value: String, onChange: (String) -> Unit, hint: String, style: TextStyle, singleLine: Boolean = true) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = style,
        singleLine = singleLine,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.onBackground),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(hint, style = style.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            inner()
        },
    )
}
