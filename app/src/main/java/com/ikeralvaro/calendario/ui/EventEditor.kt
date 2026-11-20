package com.ikeralvaro.calendario.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.EventColors
import com.ikeralvaro.calendario.data.Recurrence
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class Picker { START_DATE, START_TIME, END_DATE, END_TIME, REPEAT_UNTIL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditor(
    initial: Event,
    isCopy: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    onDelete: (Event) -> Unit,
) {
    val zone = ZoneId.systemDefault()
    var title by remember { mutableStateOf(initial.title) }
    var description by remember { mutableStateOf(initial.description) }
    var location by remember { mutableStateOf(initial.location) }
    var allDay by remember { mutableStateOf(initial.allDay) }
    var start by remember { mutableStateOf(initial.startDateTime(zone)) }
    var end by remember { mutableStateOf(initial.endDateTime(zone)) }
    var color by remember { mutableIntStateOf(initial.color.takeIf { it != 0 } ?: EventColors.palette.first()) }
    var reminder by remember { mutableIntStateOf(initial.reminderMinutes) }
    var rule by remember { mutableStateOf(initial.rule) }
    var until by remember { mutableStateOf(initial.recurrenceEnd?.let { LocalDate.ofEpochDay(it) }) }
    var picker by remember { mutableStateOf<Picker?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val isNew = initial.id == 0L

    val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Es)
    val valid = !end.isBefore(start) && (until == null || !until!!.isBefore(start.toLocalDate()))

    val accent by animateColorAsState(Color(color), label = "accent")
    val titleFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (isNew && !isCopy) runCatching { titleFocus.requestFocus() } }
    BackHandler(onBack = onDismiss)

    fun save() {
        val s = if (allDay) start.toLocalDate().atStartOfDay() else start
        val e = if (allDay) end.toLocalDate().atTime(23, 59) else end
        onSave(
            initial.copy(
                title = title.trim(), description = description.trim(), location = location.trim(),
                allDay = allDay, color = color, reminderMinutes = reminder,
                start = s.atZone(zone).toInstant().toEpochMilli(),
                end = e.atZone(zone).toInstant().toEpochMilli(),
                recurrence = rule.ordinal,
                recurrenceEnd = if (rule == Recurrence.NONE) null else until?.toEpochDay(),
            )
        )
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(accent, lerp(accent, Color.Black, 0.22f))))
                    .statusBarsPadding()
                    .padding(start = 8.dp, end = 12.dp, bottom = 22.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "Cerrar", tint = Color.White) }
                    Text(
                        when { isCopy -> "Duplicar evento"; isNew -> "Nuevo evento"; else -> "Editar evento" },
                        style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f),
                    )
                    if (!isNew) IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Rounded.DeleteOutline, "Eliminar", tint = Color.White)
                    }
                    Button(
                        enabled = valid,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = accent),
                        onClick = ::save,
                    ) { Text("Guardar", fontWeight = FontWeight.Bold) }
                }
                Box(Modifier.padding(horizontal = 12.dp)) {
                    BasicField(
                        title, { title = it }, "Añade un título",
                        TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        hintColor = Color.White.copy(alpha = 0.6f), cursor = Color.White,
                        modifier = Modifier.focusRequester(titleFocus),
                    )
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
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = accent,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            ))
                    }
                    DateTimeRow("Empieza", start.format(dateFmt), if (allDay) null else start.hm(),
                        { picker = Picker.START_DATE }, { picker = Picker.START_TIME })
                    DateTimeRow("Termina", end.format(dateFmt), if (allDay) null else end.hm(),
                        { picker = Picker.END_DATE }, { picker = Picker.END_TIME })
                    AnimatedVisibility(!allDay) {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()).padding(start = 40.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val cur = Duration.between(start, end).toMinutes()
                            listOf(15L to "15 min", 30L to "30 min", 60L to "1 h", 90L to "1 h 30", 120L to "2 h").forEach { (m, l) ->
                                ChoiceChip(l, cur == m, accent) { end = start.plusMinutes(m) }
                            }
                        }
                    }
                    if (end.isBefore(start)) Text("El final debe ser posterior al inicio", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 40.dp, top = 4.dp))
                }
                Section {
                    IconRow(Icons.Rounded.Repeat) { Text("Repetir", style = MaterialTheme.typography.bodyLarge) }
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).padding(start = 40.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Recurrence.entries.forEach { r -> ChoiceChip(if (r == Recurrence.NONE) "Nunca" else r.label, r == rule, accent) { rule = r } }
                    }
                    AnimatedVisibility(rule != Recurrence.NONE) {
                        Row(Modifier.fillMaxWidth().padding(start = 40.dp, top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { picker = Picker.REPEAT_UNTIL }.padding(vertical = 6.dp)) {
                                Text("Hasta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(until?.format(dateFmt)?.cap() ?: "Para siempre", style = MaterialTheme.typography.titleMedium)
                            }
                            if (until != null) IconButton(onClick = { until = null }) {
                                Icon(Icons.Rounded.Close, "Quitar fecha de fin", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Section {
                    IconRow(Icons.Rounded.Palette) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            EventColors.palette.forEach { c ->
                                Box(
                                    Modifier.size(34.dp).clip(CircleShape).background(Color(c)).clickable { color = c },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (c == color) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                Section {
                    IconRow(Icons.Rounded.NotificationsNone) { Text("Aviso", style = MaterialTheme.typography.bodyLarge) }
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).padding(start = 40.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        reminderOptions.forEach { (m, label) -> ChoiceChip(label, m == reminder, accent) { reminder = m } }
                    }
                }
                Section {
                    IconRow(Icons.Rounded.LocationOn) {
                        BasicField(location, { location = it }, "Añadir ubicación",
                            MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface))
                    }
                    HorizontalDivider(Modifier.padding(start = 40.dp, top = 6.dp, bottom = 6.dp), color = MaterialTheme.colorScheme.outline)
                    IconRow(Icons.AutoMirrored.Rounded.Notes) {
                        BasicField(description, { description = it }, "Añadir descripción",
                            MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), singleLine = false)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    when (val p = picker) {
        Picker.START_DATE, Picker.END_DATE, Picker.REPEAT_UNTIL -> {
            val current = when (p) {
                Picker.START_DATE -> start.toLocalDate()
                Picker.END_DATE -> end.toLocalDate()
                else -> until ?: start.toLocalDate().plusMonths(1)
            }
            val state = rememberDatePickerState(
                initialSelectedDateMillis = current.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let {
                            val d = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                            when (p) {
                                Picker.START_DATE -> {
                                    val dur = Duration.between(start, end)
                                    start = d.atTime(start.toLocalTime()); end = start.plus(dur)
                                }
                                Picker.END_DATE -> end = d.atTime(end.toLocalTime())
                                else -> until = d
                            }
                        }
                        picker = null
                    }) { Text("Aceptar") }
                },
                dismissButton = { TextButton(onClick = { picker = null }) { Text("Cancelar") } },
            ) { DatePicker(state) }
        }
        Picker.START_TIME, Picker.END_TIME -> {
            val isStart = p == Picker.START_TIME
            val t = if (isStart) start.toLocalTime() else end.toLocalTime()
            val state = rememberTimePickerState(t.hour, t.minute, is24Hour = LocalSettings.current.use24h)
            AlertDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(onClick = {
                        val nt = LocalTime.of(state.hour, state.minute)
                        if (isStart) {
                            val dur = Duration.between(start, end)
                            start = start.toLocalDate().atTime(nt); end = start.plus(dur)
                        } else end = end.toLocalDate().atTime(nt)
                        picker = null
                    }) { Text("Aceptar") }
                },
                dismissButton = { TextButton(onClick = { picker = null }) { Text("Cancelar") } },
                text = { TimePicker(state) },
            )
        }
        null -> Unit
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Rounded.DeleteOutline, null) },
            title = { Text("¿Eliminar evento?") },
            text = { if (initial.rule != Recurrence.NONE) Text("Se eliminarán todas las repeticiones.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete(initial) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
fun ChoiceChip(label: String, selected: Boolean, accent: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) accent else MaterialTheme.colorScheme.surfaceVariant, label = "chip")
    Box(
        Modifier.clip(CircleShape).background(bg).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun Section(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp),
        content = content,
    )
}

@Composable
fun IconRow(icon: ImageVector, content: @Composable RowScope.() -> Unit) {
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
fun BasicField(
    value: String, onChange: (String) -> Unit, hint: String, style: TextStyle, singleLine: Boolean = true,
    hintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant, cursor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = style,
        singleLine = singleLine,
        cursorBrush = SolidColor(cursor),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(hint, style = style.copy(color = hintColor))
            inner()
        },
    )
}
