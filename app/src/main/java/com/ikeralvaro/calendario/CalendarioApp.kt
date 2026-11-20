package com.ikeralvaro.calendario

import android.app.Application
import com.ikeralvaro.calendario.data.AppDatabase
import com.ikeralvaro.calendario.data.EventRepository
import com.ikeralvaro.calendario.data.SettingsRepository
import com.ikeralvaro.calendario.notify.ReminderScheduler

class CalendarioApp : Application() {
    val repository: EventRepository by lazy {
        EventRepository(this, AppDatabase.get(this).eventDao(), ReminderScheduler(this))
    }
    val settings: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.createChannel(this)
    }
}
