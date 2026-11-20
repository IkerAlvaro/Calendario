package com.ikeralvaro.calendario.data

import com.ikeralvaro.calendario.notify.ReminderScheduler

class EventRepository(private val dao: EventDao, private val scheduler: ReminderScheduler) {
    val events = dao.observeAll()

    suspend fun get(id: Long) = dao.byId(id)

    suspend fun save(event: Event): Long {
        val id = dao.upsert(event)
        val saved = event.copy(id = if (event.id == 0L) id else event.id)
        scheduler.cancel(saved.id)
        scheduler.schedule(saved)
        return saved.id
    }

    suspend fun delete(event: Event) {
        scheduler.cancel(event.id)
        dao.delete(event)
    }

    suspend fun rescheduleAll() {
        dao.withReminders().forEach { scheduler.schedule(it) }
    }
}
