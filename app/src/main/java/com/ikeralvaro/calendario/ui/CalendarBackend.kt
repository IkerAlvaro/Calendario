package com.ikeralvaro.calendario.ui

import com.ikeralvaro.calendario.CalendarioApp
import com.ikeralvaro.calendario.data.Event
import com.ikeralvaro.calendario.data.Settings
import kotlinx.coroutines.flow.Flow

/** What the UI needs from the data layer (a fake one is used for screenshot tests). */
interface CalendarBackend {
    val events: Flow<List<Event>>
    val settings: Flow<Settings>
    suspend fun save(event: Event): Long
    suspend fun delete(event: Event)
    suspend fun updateSettings(transform: (Settings) -> Settings)
}

class AppBackend(private val app: CalendarioApp) : CalendarBackend {
    override val events = app.repository.events
    override val settings = app.settings.settings
    override suspend fun save(event: Event) = app.repository.save(event)
    override suspend fun delete(event: Event) = app.repository.delete(event)
    override suspend fun updateSettings(transform: (Settings) -> Settings) = app.settings.update(transform)
}
