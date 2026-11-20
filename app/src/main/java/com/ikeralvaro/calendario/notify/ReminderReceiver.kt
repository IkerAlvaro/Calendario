package com.ikeralvaro.calendario.notify

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.ikeralvaro.calendario.CalendarioApp
import com.ikeralvaro.calendario.MainActivity
import com.ikeralvaro.calendario.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, 0)
        val start = intent.getLongExtra(EXTRA_START, 0)
        val app = context.applicationContext as CalendarioApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val event = app.repository.get(id) ?: return@launch
                notify(context, event.id, event.title.ifBlank { "Evento" }, event.location, start)
                // Repeating events: arm the next occurrence.
                ReminderScheduler(context).schedule(event)
            } finally {
                pending.finish()
            }
        }
    }

    private fun notify(context: Context, id: Long, title: String, location: String, start: Long) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val time = DateTimeFormatter.ofPattern("HH:mm")
            .format(Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()))
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val text = if (location.isNotBlank()) "A las $time · $location" else "Empieza a las $time"
        val n = Notification.Builder(context, ReminderScheduler.CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFFE8111E.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(Notification.CATEGORY_EVENT)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id.toInt(), n)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_START = "start"
    }
}
