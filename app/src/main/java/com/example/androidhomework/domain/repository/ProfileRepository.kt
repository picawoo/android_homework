package com.example.androidhomework.domain.repository

import com.example.androidhomework.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun saveProfile(profile: UserProfile)
}