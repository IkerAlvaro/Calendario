package com.ikeralvaro.calendario.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ikeralvaro.calendario.CalendarioApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val app = context.applicationContext as CalendarioApp
        CoroutineScope(Dispatchers.IO).launch {
            try { app.repository.rescheduleAll() } finally { pending.finish() }
        }
    }
}
