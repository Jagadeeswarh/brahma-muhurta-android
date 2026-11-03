package com.sattva.brahmamuhurta

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isTest = intent.getBooleanExtra(AlarmScheduler.EXTRA_TEST, false)
        if (!isTest) AlarmScheduler.scheduleNext(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL, "Brahma Muhurta alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Wake-up alarm and test alarm"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
        val full = PendingIntent.getActivity(
            context, if (isTest) 9202 else 9201,
            Intent(context, AlarmActivity::class.java)
                .putExtra(AlarmScheduler.EXTRA_TEST, isTest)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (isTest) "Alarm test" else "Brahma Muhurta")
            .setContentText(if (isTest) "Your alarm is working. Tap to open the alarm screen." else "A quiet morning awaits you.")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(full, true)
            .setContentIntent(full)
            .setAutoCancel(true)
            .build()
        try { NotificationManagerCompat.from(context).notify(if (isTest) 502 else 501, notification) }
        catch (_: SecurityException) { }
    }
    companion object { const val CHANNEL = "brahma_muhurta_alarm" }
}
