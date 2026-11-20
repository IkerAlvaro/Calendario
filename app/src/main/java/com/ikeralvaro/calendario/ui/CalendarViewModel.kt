package com.ikeralvaro.calendario.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class ViewMode(val label: String) { MONTH("Mes"), WEEK("Semana"), DAY("Día"), AGENDA("Agenda") }

/** Event being edited: null = editor closed. */
data class EditorState(val event: Event)

class CalendarViewModel(private val repo: EventRepository) : ViewModel() {
    val events: StateFlow<List<Event>> =
        repo.events.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mode = MutableStateFlow(ViewMode.MONTH)
    val mode = _mode.asStateFlow()

    private val _selected = MutableStateFlow(LocalDate.now())
    val selected = _selected.asStateFlow()

    private val _editor = MutableStateFlow<EditorState?>(null)
    val editor = _editor.asStateFlow()

    fun setMode(m: ViewMode) { _mode.value = m }
    fun select(day: LocalDate) { _selected.value = day }
    fun today() { _selected.value = LocalDate.now() }

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
        val now = java.time.LocalTime.now()
        val h = hour ?: (now.hour + 1).coerceAtMost(23)
        val start = day.atTime(h, 0)
        val zone = java.time.ZoneId.systemDefault()
        _editor.value = EditorState(
            Event(
                title = "",
                start = start.atZone(zone).toInstant().toEpochMilli(),
                end = start.plusHours(1).atZone(zone).toInstant().toEpochMilli(),
                color = com.ikeralvaro.calendario.data.EventColors.palette.first(),
                reminderMinutes = 10,
            )
        )
    }

    fun edit(event: Event) { _editor.value = EditorState(event) }
    fun closeEditor() { _editor.value = null }

    fun save(event: Event) = viewModelScope.launch {
        repo.save(event); _editor.value = null
    }

    fun delete(event: Event) = viewModelScope.launch {
        repo.delete(event); _editor.value = null
    }

    companion object {
        fun factory(repo: EventRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { CalendarViewModel(repo) }
        }
    }
}
