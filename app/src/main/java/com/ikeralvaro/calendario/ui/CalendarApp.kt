package com.ikeralvaro.calendario.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val Es: Locale = Locale.forLanguageTag("es-ES")

fun String.cap() = replaceFirstChar { it.titlecase(Es) }

@Composable
fun CalendarApp(vm: CalendarViewModel) {
    val events by vm.events.collectAsStateWithLifecycle()
    val mode by vm.mode.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val editor by vm.editor.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { Header(mode, selected, vm) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.newEvent() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) { Icon(Icons.Filled.Add, contentDescription = "Nuevo evento") }
        },
    ) { padding ->
        var drag by remember { mutableFloatStateOf(0f) }
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .pointerInput(mode) {
                    detectHorizontalDragGestures(
                        onDragStart = { drag = 0f },
                        onDragEnd = {
                            if (drag > 120) vm.step(false) else if (drag < -120) vm.step(true)
                        },
                    ) { _, amount -> drag += amount }
                }
        ) {
            AnimatedContent(targetState = mode, label = "mode") { m ->
                when (m) {
                    ViewMode.MONTH -> MonthView(selected, events, onSelect = vm::select, onOpenDay = vm::openDay, onEdit = vm::edit)
                    ViewMode.WEEK -> TimelineView(selected, 7, events, onSelectDay = vm::openDay, onEdit = vm::edit, onCreate = vm::newEvent)
                    ViewMode.DAY -> TimelineView(selected, 1, events, onSelectDay = vm::select, onEdit = vm::edit, onCreate = vm::newEvent)
                    ViewMode.AGENDA -> AgendaView(selected, events, onEdit = vm::edit)
                }
            }
        }
    }

    editor?.let { st ->
        EventEditor(
            initial = st.event,
            onDismiss = vm::closeEditor,
            onSave = { vm.save(it) },
            onDelete = { vm.delete(it) },
        )
    }
}

@Composable
private fun Header(mode: ViewMode, selected: LocalDate, vm: CalendarViewModel) {
    val title = when (mode) {
        ViewMode.DAY -> selected.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Es)).cap()
        else -> selected.month.getDisplayName(TextStyle.FULL, Es).cap() +
            if (selected.year != LocalDate.now().year) " ${selected.year}" else ""
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            IconButton(onClick = { vm.step(false) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Anterior") }
            TextButton(onClick = vm::today) { Text("Hoy", color = MaterialTheme.colorScheme.onBackground) }
            IconButton(onClick = { vm.step(true) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Siguiente") }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(3.dp)
        ) {
            ViewMode.entries.forEach { m ->
                val active = m == mode
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { vm.setMode(m) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        m.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
