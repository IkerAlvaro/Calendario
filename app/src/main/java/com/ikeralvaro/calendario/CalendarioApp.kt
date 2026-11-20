package com.ikeralvaro.calendario

import android.app.Application
import com.ikeralvaro.calendario.data.AppDatabase
import com.ikeralvaro.calendario.data.EventRepository
import com.ikeralvaro.calendario.notify.ReminderScheduler

class CalendarioApp : Application() {
    val repository: EventRepository by lazy {
        EventRepository(AppDatabase.get(this).eventDao(), ReminderScheduler(this))
    }

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.createChannel(this)
    }
}
