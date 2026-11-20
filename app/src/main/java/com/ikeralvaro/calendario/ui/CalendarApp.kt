package com.ikeralvaro.calendario.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ikeralvaro.calendario.R
import com.ikeralvaro.calendario.data.CalendarMath
import com.ikeralvaro.calendario.data.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun CalendarApp(
    vm: CalendarViewModel,
    banner: @Composable () -> Unit = {},
    privacyOptionsRequired: Boolean = false,
    onPrivacyOptions: () -> Unit = {},
) {
    val events by vm.events.collectAsStateWithLifecycle()
    val mode by vm.mode.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val editor by vm.editor.collectAsStateWithLifecycle()
    val detail by vm.detail.collectAsStateWithLifecycle()
    val screen by vm.screen.collectAsStateWithLifecycle()
    val settings = LocalSettings.current
    val haptic = LocalHapticFeedback.current
    val snackbar = remember { SnackbarHostState() }
    val requestNotifications = rememberNotificationPermission()

    var forward by remember { mutableStateOf(true) }
    val step: (Boolean) -> Unit = { f ->
        forward = f
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        vm.step(f)
    }

    LaunchedEffect(vm) {
        vm.messages.collect { msg ->
            when (msg) {
                is UiMessage.Deleted -> {
                    val r = snackbar.showSnackbar(
                        "«${msg.event.title.ifBlank { "Evento" }}» eliminado",
                        actionLabel = "Deshacer", duration = SnackbarDuration.Short,
                    )
                    if (r == SnackbarResult.ActionPerformed) vm.restore(msg.event)
                }
                is UiMessage.Saved -> snackbar.showSnackbar("«${msg.title}» guardado", duration = SnackbarDuration.Short)
            }
        }
    }

    var fabExpanded by remember { mutableStateOf(true) }
    val nested = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -6f) fabExpanded = false else if (available.y > 6f) fabExpanded = true
                return Offset.Zero
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { Header(mode, selected, vm, step) },
            bottomBar = banner,
            snackbarHost = {
                SnackbarHost(snackbar) {
                    Snackbar(it, shape = RoundedCornerShape(14.dp),
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        actionColor = MaterialTheme.colorScheme.primary)
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { vm.newEvent() },
                    expanded = fabExpanded,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    icon = { Icon(Icons.Rounded.Add, "Nuevo evento") },
                    text = { Text("Nuevo", style = MaterialTheme.typography.labelLarge) },
                )
            },
        ) { padding ->
            var drag by remember { mutableFloatStateOf(0f) }
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .nestedScroll(nested)
                    .pointerInput(mode) {
                        detectHorizontalDragGestures(
                            onDragStart = { drag = 0f },
                            onDragEnd = { if (drag > 120) step(false) else if (drag < -120) step(true) },
                        ) { _, amount -> drag += amount }
                    }
            ) {
                AnimatedContent(
                    targetState = mode to periodKey(mode, selected, settings.firstDay),
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
                        ViewMode.MONTH -> MonthView(
                            selected, events,
                            onSelect = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.select(it) },
                            onOpenDay = vm::openDay, onOpen = vm::showDetail, onDelete = { vm.delete(it) },
                        )
                        ViewMode.WEEK -> TimelineView(selected, 7, events, onSelectDay = vm::openDay, onOpen = vm::showDetail, onCreate = vm::newEvent)
                        ViewMode.DAY -> TimelineView(selected, 1, events, onSelectDay = vm::select, onOpen = vm::showDetail, onCreate = vm::newEvent)
                        ViewMode.AGENDA -> AgendaView(selected, events, onOpen = vm::showDetail, onDelete = { vm.delete(it) })
                    }
                }
            }
        }

        AnimatedVisibility(screen == Screen.SEARCH, enter = fadeIn() + slideInVertically { it / 8 }, exit = fadeOut()) {
            SearchScreen(events, onBack = { vm.open(Screen.MAIN) }, onOpen = vm::showDetail)
        }
        AnimatedVisibility(screen == Screen.SETTINGS, enter = slideInHorizontally { it } + fadeIn(), exit = slideOutHorizontally { it } + fadeOut()) {
            SettingsScreen(settings, onChange = { vm.updateSettings(it) }, onBack = { vm.open(Screen.MAIN) },
                privacyOptionsRequired = privacyOptionsRequired, onPrivacyOptions = onPrivacyOptions)
        }
        AnimatedVisibility(
            visible = editor != null,
            enter = slideInVertically { it / 4 } + fadeIn(),
            exit = slideOutVertically { it / 4 } + fadeOut(),
        ) {
            var last by remember { mutableStateOf(editor) }
            if (editor != null) last = editor
            val st = last ?: return@AnimatedVisibility
            key(st) {
                EventEditor(
                    initial = st.event, isCopy = st.isCopy,
                    onDismiss = vm::closeEditor,
                    onSave = { e: Event -> vm.save(e); if (e.reminderMinutes >= 0) requestNotifications() },
                    onDelete = { vm.delete(it) },
                )
            }
        }
    }

    detail?.let { o ->
        EventDetailSheet(o, onDismiss = vm::closeDetail, onEdit = { vm.edit(o.event) },
            onDuplicate = { vm.duplicate(o.event) }, onDelete = { vm.delete(o.event) })
    }
}

/** Returns a function that asks for the notification permission (Android 13+) if needed. */
@Composable
private fun rememberNotificationPermission(): () -> Unit {
    if (Build.VERSION.SDK_INT < 33 || LocalActivityResultRegistryOwner.current == null) return {}
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private fun periodKey(mode: ViewMode, d: LocalDate, firstDay: java.time.DayOfWeek): String = when (mode) {
    ViewMode.MONTH, ViewMode.AGENDA -> "${d.year}-${d.monthValue}"
    ViewMode.WEEK -> CalendarMath.weekStart(d, firstDay).toString()
    ViewMode.DAY -> d.toString()
}

@Composable
private fun Header(mode: ViewMode, selected: LocalDate, vm: CalendarViewModel, step: (Boolean) -> Unit) {
    val today = LocalDate.now()
    val title = when (mode) {
        ViewMode.DAY -> selected.format(DateTimeFormatter.ofPattern("d MMMM", Es)).cap()
        else -> selected.month.getDisplayName(TextStyle.FULL, Es).cap()
    }
    val year = if (mode == ViewMode.DAY) selected.dayOfWeek.getDisplayName(TextStyle.SHORT, Es).cap() else selected.year.toString()
    val isToday = selected == today
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 10.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.logo), null,
                Modifier.size(42.dp).shadow(8.dp, RoundedCornerShape(11.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${greeting()} · ${today.format(DateTimeFormatter.ofPattern("EEE d", Es))}".uppercase(Es),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1,
                )
                Text(
                    buildAnnotatedString {
                        append(title)
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)) {
                            append(" $year")
                        }
                    },
                    style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground, maxLines = 1,
                )
            }
            HeaderIcon(Icons.Rounded.Search, "Buscar") { vm.open(Screen.SEARCH) }
            HeaderIcon(Icons.Rounded.Settings, "Ajustes") { vm.open(Screen.SETTINGS) }
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
private fun HeaderIcon(icon: ImageVector, desc: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(icon, desc, tint = MaterialTheme.colorScheme.onBackground) }
}

@Composable
private fun RoundIcon(icon: ImageVector, desc: String, onClick: () -> Unit) {
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
