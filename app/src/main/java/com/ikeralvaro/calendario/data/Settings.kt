package com.ikeralvaro.calendario.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

enum class ThemeMode(val label: String) { SYSTEM("Sistema"), LIGHT("Claro"), DARK("Oscuro") }

data class Settings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val firstDay: DayOfWeek = DayOfWeek.MONDAY,
    val defaultReminder: Int = 10,
    val use24h: Boolean = true,
)

private val Context.dataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {
    private val kTheme = intPreferencesKey("theme")
    private val kFirstDay = intPreferencesKey("first_day")
    private val kReminder = intPreferencesKey("default_reminder")
    private val k24h = booleanPreferencesKey("use_24h")

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            theme = ThemeMode.entries.getOrElse(p[kTheme] ?: 0) { ThemeMode.SYSTEM },
            firstDay = DayOfWeek.of(p[kFirstDay] ?: 1),
            defaultReminder = p[kReminder] ?: 10,
            use24h = p[k24h] ?: true,
        )
    }

    suspend fun update(transform: (Settings) -> Settings) {
        context.dataStore.edit { p ->
            val cur = Settings(
                theme = ThemeMode.entries.getOrElse(p[kTheme] ?: 0) { ThemeMode.SYSTEM },
                firstDay = DayOfWeek.of(p[kFirstDay] ?: 1),
                defaultReminder = p[kReminder] ?: 10,
                use24h = p[k24h] ?: true,
            )
            val n = transform(cur)
            p[kTheme] = n.theme.ordinal
            p[kFirstDay] = n.firstDay.value
            p[kReminder] = n.defaultReminder
            p[k24h] = n.use24h
        }
    }
}
