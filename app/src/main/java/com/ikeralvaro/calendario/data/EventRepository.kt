package com.ikeralvaro.calendario.data

import android.content.Context
import com.ikeralvaro.calendario.notify.ReminderScheduler
import com.ikeralvaro.calendario.widget.TodayWidget

class EventRepository(
    private val context: Context,
    private val dao: EventDao,
    private val scheduler: ReminderScheduler,
) {
    val events = dao.observeAll()

    suspend fun all() = dao.all()

    suspend fun get(id: Long) = dao.byId(id)

    suspend fun save(event: Event): Long {
        val id = dao.upsert(event)
        val saved = event.copy(id = if (event.id == 0L) id else event.id)
        scheduler.cancel(saved.id)
        scheduler.schedule(saved)
        TodayWidget.refresh(context)
        return saved.id
    }

    suspend fun delete(event: Event) {
        scheduler.cancel(event.id)
        dao.delete(event)
        TodayWidget.refresh(context)
    }

    suspend fun rescheduleAll() {
        dao.withReminders().forEach { scheduler.schedule(it) }
    }
}
