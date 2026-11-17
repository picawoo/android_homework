package com.example.androidhomework.ui.viewmodel

import android.net.Uri
import android.util.Patterns
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidhomework.data.files.AvatarStorage
import com.example.androidhomework.di.AppContainer
import com.example.androidhomework.domain.model.UserProfile
import com.example.androidhomework.domain.model.formatClassTime
import com.example.androidhomework.domain.model.parseClassTime
import com.example.androidhomework.notifications.ClassReminderScheduler
import com.example.androidhomework.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoaded: Boolean = false,
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val classTime: String = "",
    val avatarUri: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedMessage: String? = null
) {
    val resumeUrlError: String?
        get() {
            val url = normalizeResumeUrl(resumeUrl)
            return when {
                url.isEmpty() -> null
                url.startsWith("http://", ignoreCase = true) -> "Only https:// links are supported"
                !isValidResumeUrl(url) -> "Enter a valid link, e.g. https://example.com/cv.pdf"
                else -> null
            }
        }

    val classTimeError: String?
        get() = when {
            classTime.isBlank() -> "Class time is required (HH:mm)"
            parseClassTime(classTime) == null -> "Enter time in HH:mm format, e.g. 09:30"
            else -> null
        }

    val canSave: Boolean
        get() = isLoaded && !isSaving && resumeUrlError == null && classTimeError == null
}

internal fun normalizeResumeUrl(raw: String): String {
    val trimmed = raw.trim()
    return if (trimmed.isEmpty() || "://" in trimmed) trimmed else "https://$trimmed"
}

private fun isValidResumeUrl(url: String): Boolean =
    URLUtil.isHttpsUrl(url) && Patterns.WEB_URL.matcher(url).matches()

class EditProfileViewModel(
    private val repository: ProfileRepository,
    private val avatarStorage: AvatarStorage,
    private val reminderScheduler: ClassReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    private var originalAvatarUri: String? = null
    private var saved = false

    init {
        viewModelScope.launch {
            val profile = repository.profile.first()
            originalAvatarUri = profile.avatarUri
            _uiState.value = EditProfileUiState(
                isLoaded = true,
                fullName = profile.fullName,
                position = profile.position,
                resumeUrl = profile.resumeUrl,
                classTime = profile.classTime,
                avatarUri = profile.avatarUri
            )
        }
    }

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value) }

    fun onPositionChange(value: String) = _uiState.update { it.copy(position = value) }

    fun onResumeUrlChange(value: String) = _uiState.update { it.copy(resumeUrl = value) }

    fun onClassTimeChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == ':' }.take(5)
        _uiState.update { it.copy(classTime = filtered) }
    }

    fun onClassTimePicked(hour: Int, minute: Int) {
        _uiState.update { it.copy(classTime = formatClassTime(java.time.LocalTime.of(hour, minute))) }
    }

    fun createCameraTarget(): Uri = avatarStorage.createCameraTarget()

    fun onCameraResult(target: Uri, success: Boolean) {
        if (success) {
            replaceAvatar(target.toString())
        } else {
            avatarStorage.delete(target.toString())
        }
    }

    fun onGalleryPicked(source: Uri?) {
        if (source == null) return
        viewModelScope.launch {
            val imported = avatarStorage.import(source)
            if (imported != null) {
                replaceAvatar(imported.toString())
            } else {
                _messages.send("Couldn't load the selected photo")
            }
        }
    }

    fun onRemoveAvatar() = replaceAvatar(null)

    private fun replaceAvatar(newUri: String?) {
        deletePendingAvatar()
        _uiState.update { it.copy(avatarUri = newUri) }
    }

    private fun deletePendingAvatar() {
        val current = _uiState.value.avatarUri
        if (current != null && current != originalAvatarUri) avatarStorage.delete(current)
    }

    fun onDoneClick() {
        val state = _uiState.value
        if (!state.canSave) return
        val classTime = parseClassTime(state.classTime) ?: return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.saveProfile(
                UserProfile(
                    fullName = state.fullName.trim(),
                    position = state.position.trim(),
                    avatarUri = state.avatarUri,
                    resumeUrl = normalizeResumeUrl(state.resumeUrl),
                    classTime = formatClassTime(classTime)
                )
            )
            avatarStorage.deleteAllExcept(state.avatarUri)

            val reminder = reminderScheduler.schedule(state.fullName.trim(), classTime)

            saved = true
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isSaved = true,
                    savedMessage = reminder.toMessage(formatClassTime(classTime))
                )
            }
        }
    }

    override fun onCleared() {
        if (!saved) deletePendingAvatar()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EditProfileViewModel(
                    repository = AppContainer.profileRepository,
                    avatarStorage = AppContainer.avatarStorage,
                    reminderScheduler = AppContainer.classReminderScheduler
                )
            }
        }
    }
}

private fun ClassReminderScheduler.Result.toMessage(time: String): String {
    val day = if (isToday) "today" else "tomorrow"
    val base = "Reminder set for $day at $time"
    return if (isExact) base else "$base. It may arrive a few minutes late"
}