package com.example.androidhomework.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.androidhomework.domain.model.formatClassTime
import java.time.LocalTime
import java.time.ZonedDateTime


class ClassReminderScheduler(private val context: Context) {

    class Result(
        val isToday: Boolean,
        val isExact: Boolean
    )

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(fullName: String, time: LocalTime): Result {
        NotificationHelper.ensureChannel(context)

        val now = ZonedDateTime.now()
        var trigger = now.with(time)
        var isToday = true
        if (!trigger.isAfter(now)) {
            trigger = trigger.plusDays(1)
            isToday = false
        }

        val pendingIntent = reminderPendingIntent(fullName, formatClassTime(time))
        val triggerAtMillis = trigger.toInstant().toEpochMilli()

        val isExact = scheduleAlarm(triggerAtMillis, pendingIntent)
        return Result(isToday = isToday, isExact = isExact)
    }

    private fun scheduleAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent): Boolean {
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()

        if (canScheduleExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
                return true
            } catch (e: SecurityException) {
            }
        }

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        return false
    }

    private fun reminderPendingIntent(fullName: String, time: String): PendingIntent {
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            putExtra(EXTRA_FULL_NAME, fullName)
            putExtra(EXTRA_TIME, time)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_FULL_NAME = "extra_full_name"
        const val EXTRA_TIME = "extra_time"
        private const val REQUEST_CODE = 2001
    }
}