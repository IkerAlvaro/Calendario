package com.ikeralvaro.calendario.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ikeralvaro.calendario.R
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
    val haptic = LocalHapticFeedback.current
    var forward by remember { mutableStateOf(true) }
    val step: (Boolean) -> Unit = { f ->
        forward = f
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        vm.step(f)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { Header(mode, selected, vm, step) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.newEvent() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Nuevo", style = MaterialTheme.typography.labelLarge) },
            )
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
                        onDragEnd = { if (drag > 120) step(false) else if (drag < -120) step(true) },
                    ) { _, amount -> drag += amount }
                }
        ) {
            AnimatedContent(
                targetState = mode to periodKey(mode, selected),
                transitionSpec = {
                    if (initialState.first != targetState.first) fadeIn() togetherWith fadeOut()
                    else {
                        val dir = if (forward) 1 else -1
                        (slideInHorizontally { it / 3 * dir } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it / 3 * dir } + fadeOut())
                    }
                },
                label = "content",
            ) { (m, _) ->
                when (m) {
                    ViewMode.MONTH -> MonthView(selected, events,
                        onSelect = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.select(it) },
                        onOpenDay = vm::openDay, onEdit = vm::edit)
                    ViewMode.WEEK -> TimelineView(selected, 7, events, onSelectDay = vm::openDay, onEdit = vm::edit, onCreate = vm::newEvent)
                    ViewMode.DAY -> TimelineView(selected, 1, events, onSelectDay = vm::select, onEdit = vm::edit, onCreate = vm::newEvent)
                    ViewMode.AGENDA -> AgendaView(selected, events, onEdit = vm::edit)
                }
            }
        }
    }

    AnimatedVisibility(
        visible = editor != null,
        enter = slideInVertically { it / 4 } + fadeIn(),
        exit = slideOutVertically { it / 4 } + fadeOut(),
    ) {
        var last by remember { mutableStateOf(editor) }
        if (editor != null) last = editor
        val st = last ?: return@AnimatedVisibility
        androidx.compose.runtime.key(st.event.id, st.event.start) { EventEditor(initial = st.event, onDismiss = vm::closeEditor, onSave = { vm.save(it) }, onDelete = { vm.delete(it) }) }
    }
}

private fun periodKey(mode: ViewMode, d: LocalDate): String = when (mode) {
    ViewMode.MONTH, ViewMode.AGENDA -> "${d.year}-${d.monthValue}"
    ViewMode.WEEK -> com.ikeralvaro.calendario.data.CalendarMath.weekStart(d).toString()
    ViewMode.DAY -> d.toString()
}

@Composable
private fun Header(mode: ViewMode, selected: LocalDate, vm: CalendarViewModel, step: (Boolean) -> Unit) {
    val (title, subtitle) = when (mode) {
        ViewMode.DAY -> selected.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Es)) to
            selected.dayOfWeek.getDisplayName(TextStyle.FULL, Es).cap() + " · ${selected.year}"
        else -> selected.month.getDisplayName(TextStyle.FULL, Es).cap() to selected.year.toString()
    }
    val isToday = selected == LocalDate.now()
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.logo), null,
                Modifier.size(40.dp).shadow(6.dp, RoundedCornerShape(10.dp), clip = false),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SegmentedModes(mode, vm::setMode, Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            RoundIcon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Anterior") { step(false) }
            AnimatedVisibility(!isToday) {
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { vm.today() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Hoy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            RoundIcon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Siguiente") { step(true) }
        }
    }
}

@Composable
private fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, desc, tint = MaterialTheme.colorScheme.onBackground) }
}

@Composable
private fun SegmentedModes(mode: ViewMode, onSelect: (ViewMode) -> Unit, modifier: Modifier) {
    BoxWithConstraints(
        modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp)
    ) {
        val segW = maxWidth / ViewMode.entries.size
        val x by animateDpAsState(segW * mode.ordinal, spring(dampingRatio = 0.8f, stiffness = 500f), label = "seg")
        Box(
            Modifier.offset(x = x).width(segW).fillMaxHeight()
                .shadow(2.dp, RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
        )
        Row(Modifier.fillMaxSize()) {
            ViewMode.entries.forEach { m ->
                val active = m == mode
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .clickable(remember { MutableInteractionSource() }, indication = null) { onSelect(m) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        m.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
