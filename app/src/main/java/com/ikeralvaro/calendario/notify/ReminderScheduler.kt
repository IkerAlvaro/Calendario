package com.ikeralvaro.calendario.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ikeralvaro.calendario.data.Event
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    private val alarms = context.getSystemService(AlarmManager::class.java)

    /** Schedules the reminder of the next occurrence whose alert time is still in the future. */
    fun schedule(event: Event) {
        if (event.reminderMinutes < 0) return
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        // The alert of an occurrence fires reminderMinutes before it starts.
        val occ = event.nextOccurrence(now.plusMinutes(event.reminderMinutes.toLong()).plusSeconds(1), zone) ?: return
        val at = occ.start.minusMinutes(event.reminderMinutes.toLong()).atZone(zone).toInstant().toEpochMilli()
        val startMillis = occ.start.atZone(zone).toInstant().toEpochMilli()
        val pi = pending(event.id, startMillis)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()
        if (canExact) alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        else alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }

    fun cancel(id: Long) = alarms.cancel(pending(id, 0))

    private fun pending(id: Long, start: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_ID, id)
            .putExtra(ReminderReceiver.EXTRA_START, start)
        return PendingIntent.getBroadcast(
            context, id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL = "reminders"
        fun createChannel(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "Recordatorios", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Avisos antes de que empiecen tus eventos"
                }
            )
        }
    }
}
