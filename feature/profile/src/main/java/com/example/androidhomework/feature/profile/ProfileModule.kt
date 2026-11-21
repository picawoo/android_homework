package com.example.androidhomework.feature.profile

import android.content.Context
import com.example.androidhomework.feature.profile.data.AvatarStorage
import com.example.androidhomework.feature.profile.data.ProfileRepositoryImpl
import com.example.androidhomework.feature.profile.data.ResumeDownloader
import com.example.androidhomework.feature.profile.domain.ProfileRepository
import com.example.androidhomework.feature.profile.notifications.ClassReminderScheduler


object ProfileModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    internal val profileRepository: ProfileRepository by lazy { ProfileRepositoryImpl(appContext) }
    internal val avatarStorage: AvatarStorage by lazy { AvatarStorage(appContext) }
    internal val resumeDownloader: ResumeDownloader by lazy { ResumeDownloader(appContext) }
    internal val classReminderScheduler: ClassReminderScheduler by lazy { ClassReminderScheduler(appContext) }
}