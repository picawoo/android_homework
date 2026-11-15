package com.example.androidhomework.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.androidhomework.domain.model.UserProfile
import com.example.androidhomework.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.profileDataStore by preferencesDataStore(name = "user_profile")

class ProfileRepositoryImpl(
    private val context: Context
) : ProfileRepository {

    private object Keys {
        val FULL_NAME = stringPreferencesKey("profile_full_name")
        val POSITION = stringPreferencesKey("profile_position")
        val AVATAR_URI = stringPreferencesKey("profile_avatar_uri")
        val RESUME_URL = stringPreferencesKey("profile_resume_url")
    }

    override val profile: Flow<UserProfile> = context.profileDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            UserProfile(
                fullName = prefs[Keys.FULL_NAME].orEmpty(),
                position = prefs[Keys.POSITION].orEmpty(),
                avatarUri = prefs[Keys.AVATAR_URI],
                resumeUrl = prefs[Keys.RESUME_URL].orEmpty()
            )
        }

    override suspend fun saveProfile(profile: UserProfile) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.FULL_NAME] = profile.fullName
            prefs[Keys.POSITION] = profile.position
            prefs[Keys.RESUME_URL] = profile.resumeUrl
            if (profile.avatarUri != null) {
                prefs[Keys.AVATAR_URI] = profile.avatarUri
            } else {
                prefs.remove(Keys.AVATAR_URI)
            }
        }
    }
}