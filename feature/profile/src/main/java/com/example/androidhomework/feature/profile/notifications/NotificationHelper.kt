package com.example.androidhomework.feature.profile.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.androidhomework.feature.profile.R

internal object NotificationHelper {

    const val CHANNEL_ID = "class_reminders"
    private const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Class reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders about the start of your favorite class"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showClassReminder(context: Context, fullName: String, time: String) {
        val manager = context.getSystemService(NotificationManager::class.java)

        if (!manager.areNotificationsEnabled()) return
        ensureChannel(context)

        val message = if (fullName.isBlank()) {
            "Your favorite class starts at $time"
        } else {
            "$fullName, your favorite class starts at $time"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_class_reminder)
            .setContentTitle("Class time")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
            builder.setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }

        manager.notify(NOTIFICATION_ID, builder.build())
    }
}