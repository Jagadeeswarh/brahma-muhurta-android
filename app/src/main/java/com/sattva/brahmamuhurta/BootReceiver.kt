package com.sattva.brahmamuhurta

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(AlarmScheduler.PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(AlarmScheduler.ENABLED, false)) AlarmScheduler.scheduleNext(context)
    }
}
