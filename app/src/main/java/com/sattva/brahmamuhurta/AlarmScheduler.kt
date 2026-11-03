package com.sattva.brahmamuhurta

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId

object AlarmScheduler {
    const val PREFS = "brahma_prefs"
    const val ENABLED = "alarm_enabled"
    const val OFFSET = "wake_offset_minutes"
    const val LAT = "latitude"
    const val LON = "longitude"
    const val EXTRA_TEST = "is_test_alarm"
    private const val DAILY_REQUEST = 7101
    private const val TEST_REQUEST = 7102

    fun canSchedule(context: Context): Boolean {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()
    }

    fun scheduleNext(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(ENABLED, false) || !canSchedule(context)) return
        val zone = ZoneId.systemDefault()
        val lat = prefs.getFloat(LAT, 17.385f).toDouble()
        val lon = prefs.getFloat(LON, 78.4867f).toDouble()
        val offset = prefs.getInt(OFFSET, 96).coerceIn(48, 96).toLong()
        var date = LocalDate.now(zone)
        var time = SolarCalculator.events(date, lat, lon, zone).sunrise?.minusMinutes(offset)
        if (time == null || !time.atZone(zone).toInstant().isAfter(java.time.Instant.now().plusSeconds(5))) {
            date = date.plusDays(1)
            time = SolarCalculator.events(date, lat, lon, zone).sunrise?.minusMinutes(offset)
        }
        val trigger = time?.atZone(zone)?.toInstant()?.toEpochMilli() ?: return
        val intent = alarmIntent(context, DAILY_REQUEST, false)
        val show = PendingIntent.getActivity(context, 7103, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, show), intent)
    }

    fun test(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val show = PendingIntent.getActivity(context, 7103, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val trigger = System.currentTimeMillis() + 10_000L
        manager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, show), alarmIntent(context, TEST_REQUEST, true))
    }

    private fun alarmIntent(context: Context, requestCode: Int, test: Boolean): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(EXTRA_TEST, test)
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun cancel(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(alarmIntent(context, DAILY_REQUEST, false))
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(ENABLED, false).apply()
    }
}
