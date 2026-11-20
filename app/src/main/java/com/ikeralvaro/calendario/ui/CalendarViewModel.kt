package com.ikeralvaro.calendario.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.EventColors
import com.ikeralvaro.calendario.data.Occurrence
import com.ikeralvaro.calendario.data.Settings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

enum class ViewMode(val label: String) { MONTH("Mes"), WEEK("Semana"), DAY("Día"), AGENDA("Agenda") }

enum class Screen { MAIN, SEARCH, SETTINGS }

/** Event being edited: null = editor closed. */
data class EditorState(val event: Event, val isCopy: Boolean = false)

sealed interface UiMessage {
    data class Deleted(val event: Event) : UiMessage
    data class Saved(val title: String) : UiMessage
}

class CalendarViewModel(private val backend: CalendarBackend) : ViewModel() {
    val events: StateFlow<List<Event>> = backend.events.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val settings: StateFlow<Settings> = backend.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val _mode = MutableStateFlow(ViewMode.MONTH)
    val mode = _mode.asStateFlow()

    private val _selected = MutableStateFlow(LocalDate.now())
    val selected = _selected.asStateFlow()

    private val _editor = MutableStateFlow<EditorState?>(null)
    val editor = _editor.asStateFlow()

    private val _detail = MutableStateFlow<Occurrence?>(null)
    val detail = _detail.asStateFlow()

    private val _screen = MutableStateFlow(Screen.MAIN)
    val screen = _screen.asStateFlow()

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    fun setMode(m: ViewMode) { _mode.value = m }
    fun select(day: LocalDate) { _selected.value = day }
    fun today() { _selected.value = LocalDate.now() }
    fun open(screen: Screen) { _screen.value = screen }

    fun step(forward: Boolean) {
        val d = _selected.value
        val n = if (forward) 1L else -1L
        _selected.value = when (_mode.value) {
            ViewMode.MONTH, ViewMode.AGENDA -> d.plusMonths(n)
            ViewMode.WEEK -> d.plusWeeks(n)
            ViewMode.DAY -> d.plusDays(n)
        }
    }

    fun openDay(day: LocalDate) { _selected.value = day; _mode.value = ViewMode.DAY }

    fun newEvent(day: LocalDate = _selected.value, hour: Int? = null) {
        val h = hour ?: if (day == LocalDate.now()) (LocalTime.now().hour + 1).coerceAtMost(23) else 9
        val start = day.atTime(h, 0)
        val zone = ZoneId.systemDefault()
        _editor.value = EditorState(
            Event(
                title = "",
                start = start.atZone(zone).toInstant().toEpochMilli(),
                end = start.plusHours(1).atZone(zone).toInstant().toEpochMilli(),
                color = EventColors.palette.first(),
                reminderMinutes = settings.value.defaultReminder,
            )
        )
    }

    fun showDetail(o: Occurrence) { _detail.value = o }
    fun closeDetail() { _detail.value = null }

    fun edit(event: Event) { _detail.value = null; _editor.value = EditorState(event) }

    fun duplicate(event: Event) {
        _detail.value = null
        _editor.value = EditorState(event.copy(id = 0, title = event.title), isCopy = true)
    }

    fun closeEditor() { _editor.value = null }

    fun save(event: Event) = viewModelScope.launch {
        backend.save(event)
        _editor.value = null
        _messages.tryEmit(UiMessage.Saved(event.title.ifBlank { "Evento" }))
    }

    fun delete(event: Event) = viewModelScope.launch {
        _detail.value = null
        _editor.value = null
        backend.delete(event)
        _messages.tryEmit(UiMessage.Deleted(event))
    }

    fun restore(event: Event) = viewModelScope.launch { backend.save(event) }

    fun updateSettings(transform: (Settings) -> Settings) = viewModelScope.launch { backend.updateSettings(transform) }

    companion object {
        fun factory(backend: () -> CalendarBackend): ViewModelProvider.Factory = viewModelFactory {
            initializer { CalendarViewModel(backend()) }
        }
    }
}
