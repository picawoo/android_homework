package com.example.androidhomework.feature.profile.domain

import kotlinx.coroutines.flow.Flow

internal interface ProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun saveProfile(profile: UserProfile)
}