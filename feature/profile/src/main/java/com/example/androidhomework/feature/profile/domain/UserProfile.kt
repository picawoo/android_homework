package com.example.androidhomework.feature.profile.domain

internal data class UserProfile(
    val fullName: String = "",
    val position: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = "",
    val classTime: String = ""
)