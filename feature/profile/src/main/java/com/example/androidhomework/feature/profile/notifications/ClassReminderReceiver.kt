package com.example.androidhomework.feature.profile.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class ClassReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val fullName = intent.getStringExtra(ClassReminderScheduler.EXTRA_FULL_NAME).orEmpty()
        val time = intent.getStringExtra(ClassReminderScheduler.EXTRA_TIME).orEmpty()

        NotificationHelper.showClassReminder(context, fullName, time)
    }
}