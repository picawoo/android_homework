package com.example.androidhomework.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "book_filter_preferences")

data class FilterSettings(
    val selectedLanguage: String = "",
    val onlyWithCover: Boolean = false,
    val sortBy: SortOption = SortOption.DEFAULT
) {
    val hasActiveFilters: Boolean
        get() = selectedLanguage.isNotBlank() || onlyWithCover || sortBy != SortOption.DEFAULT
}

enum class SortOption {
    DEFAULT, NEWEST, MOST_EDITIONS
}

class FilterPreferences(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("filter_language")
        val ONLY_WITH_COVER = booleanPreferencesKey("filter_only_with_cover")
        val SORT_BY = stringPreferencesKey("filter_sort_by")
    }

    val filterSettingsFlow: Flow<FilterSettings> = context.dataStore.data.map { prefs ->
        FilterSettings(
            selectedLanguage = prefs[Keys.LANGUAGE] ?: "",
            onlyWithCover = prefs[Keys.ONLY_WITH_COVER] ?: false,
            sortBy = SortOption.valueOf(prefs[Keys.SORT_BY] ?: SortOption.DEFAULT.name)
        )
    }

    suspend fun saveFilters(settings: FilterSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = settings.selectedLanguage
            prefs[Keys.ONLY_WITH_COVER] = settings.onlyWithCover
            prefs[Keys.SORT_BY] = settings.sortBy.name
        }
    }
}